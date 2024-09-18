package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LiveOak0Compiler {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java ProgramName inputFile outputFile");
            System.exit(1);
        }

        String inFileName = args[0];
        String outFileName = args[1];

        try {
            String samCode = compiler(inFileName);
            System.out.println(samCode);
            try (
                BufferedWriter writer = new BufferedWriter(
                    new FileWriter(outFileName)
                )
            ) {
                writer.write(samCode);
            }
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // maps identifier -> variable
    public static Map<String, Variable> variables = new HashMap<
        String,
        Variable
    >();

    static String compiler(String fileName) {
        //returns SaM code for program in file
        try {
            System.out.println("COMPILING...");
            SamTokenizer f = new SamTokenizer(
                fileName,
                SamTokenizer.TokenizerOptions.PROCESS_STRINGS
            );
            String pgm = getProgram(f);
            return pgm;
        } catch (CompilerException e) {
            System.out.println("COMPILE ERROR: " + e.toString());
            return "STOP\n";
        } catch (Exception e) {
            System.out.println("SOMETHING WENT WRONG: " + e.toString());
            return "STOP\n";
        }
    }

    static String getProgram(SamTokenizer f) throws CompilerException {
        String pgm = "";

        // LiveOak-0
        while (f.peekAtKind() != TokenType.EOF) {
            pgm += getBody(f);
        }

        System.out.println("SAM CODE:");
        System.out.println(pgm);

        return pgm;
    }

    /** LiveOak 0
     **/
    static String getBody(SamTokenizer f) throws CompilerException {
        String sam = "";

        // while start with "int | bool | String"
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl will store variable in Hashmap: identifier -> { type: TokenType, relative_address: int }
            sam += parseVarDecl(f);
        }
        CompilerUtils.printHashmap(variables);

        // check EOF
        if (f.peekAtKind() == TokenType.EOF) {
            return sam;
        }

        // Then, get Block
        sam += getBlock(f);

        return sam;
    }

    static String parseVarDecl(SamTokenizer f) throws CompilerException {
        String sam = "";

        // typeString = int | bool | String
        String typeString = f.getWord();
        Type varType = Type.fromString(typeString);

        // typeString != int | bool | String
        if (varType == null) {
            throw new TypeErrorException(
                "Invalid type: " + typeString,
                f.lineNo()
            );
        }

        // while varName = a | b | c | ...
        while (f.peekAtKind() == TokenType.WORD) {
            String varName = f.getWord();

            // put variable in hashmap
            int address = CompilerUtils.getNextAddress(variables);
            Variable variable = new Variable(varName, varType, null, address);
            variables.put(varName, variable);

            // write same code
            sam += "PUSHOFF " + variable.getAddress() + "\n";

            if (f.check(',')) {
                continue;
            } else if (f.check(';')) {
                break;
            } else {
                throw new SyntaxErrorException(
                    "Expected ',' or `;` after each variable declaration",
                    f.lineNo()
                );
            }
        }

        return sam + "\n";
    }

    static String getBlock(SamTokenizer f) throws CompilerException {
        String sam = "";

        if (!f.check('{')) {
            throw new SyntaxErrorException(
                "getBlock expects '{' at start of block",
                f.lineNo()
            );
        }

        // while not "}"
        while (!f.check('}')) {
            sam += getStmt(f);
        }

        return sam;
    }

    static String getStmt(SamTokenizer f) throws CompilerException {
        String sam = "";

        if (f.check(';')) {
            return sam; // Null statement
        }

        Variable variable = getVar(f);

        if (!f.check('=')) {
            throw new SyntaxErrorException(
                "Expected '=' after variable in assignment",
                f.lineNo()
            );
        }

        // getExpr() would return something on the stack
        sam += getExpr(f);

        // Store item on the stack to Variable
        sam += "STOREOFF " + variable.getAddress() + "\n";

        if (!f.check(';')) {
            throw new SyntaxErrorException(
                "Expected ';' at end of statement",
                f.lineNo()
            );
        }

        return sam;
    }

    static Variable getVar(SamTokenizer f) throws CompilerException {
        // Not a var, raise
        if (f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getVar should starts with a WORD",
                f.lineNo()
            );
        }

        String varName = f.getWord();

        // Trying to access var that has not been declared
        Variable variable = variables.get(varName);
        if (variable == null) {
            throw new SyntaxErrorException(
                "getVar trying to access variable that has not been declared",
                f.lineNo()
            );
        }

        return variable;
    }

    static String getExpr(SamTokenizer f) throws CompilerException {
        String sam = "";

        // Expr -> ( ... )
        if (f.check('(')) {
            // Do stuffs
        }

        // Expr -> Var
        try {
            Variable variable = getVar(f);

            if (variable.hasValue()) {
                switch (variable.getType()) {
                    case INT:
                        String intValue = variable.getVal();
                        sam += "PUSHIMM " + intValue + "\n";
                        break;
                    case BOOL:
                        String boolValue = variable.getVal();
                        if (boolValue.equals("true")) {
                            sam += "PUSHIMM 1\n";
                        } else if (boolValue.equals("false")) {
                            sam += "PUSHIMM 0\n";
                        }
                        break;
                    case STRING:
                        String strValue = variable.getVal();
                        sam += "PUSHIMMSTR \"" + strValue + "\"\n";
                        break;
                    default:
                        throw new TypeErrorException(
                            "getExpr received invalid type",
                            f.lineNo()
                        );
                }
            } else {
                sam += "PUSHOFF " + variable.getAddress() + "\n";
            }
        } catch (SyntaxErrorException e) {
            // Expr -> Literal
            sam += getLiteral(f);
        }

        return sam;
    }

    static String getLiteral(SamTokenizer f) throws CompilerException {
        System.out.println(f.peekAtKind());
        switch (f.peekAtKind()) {
            case INTEGER:
                int value = f.getInt();
                return "PUSHIMM " + value + "\n";
            case STRING:
                String strValue = f.getString();
                return "PUSHIMMSTR \"" + strValue + "\"\n";
            case OPERATOR:
                char op = f.getOp();
                System.out.println(op);
                if (op == 'a')) {
                    return "PUSHIMM 1\n";
                } else if (op == 'c') {
                    return "PUSHIMM 0\n";
                } else {
                    throw new SyntaxErrorException(
                        "getLiteral expects words of 'true' or 'false' only",
                        f.lineNo()
                    );
                }
            default:
                throw new TypeErrorException(
                    "getLiteral received invalid type",
                    f.lineNo()
                );
        }
    }

    static String getIdentifier(SamTokenizer f) {
        return null;
    }
}
