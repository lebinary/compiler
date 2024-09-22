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
        CompilerUtils.clearTokens(); // Clear the list before starting

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
            CompilerUtils.printTokens();
            return "STOP\n";
        } catch (Exception e) {
            System.out.println("SOMETHING WENT WRONG: " + e.toString());
            CompilerUtils.printTokens();
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
            String varName = CompilerUtils.getWord(f);

            // put variable in hashmap
            int address = CompilerUtils.getNextAddress(variables);
            Variable variable = new Variable(varName, varType, null, address);
            variables.put(varName, variable);

            // write same code
            sam += "PUSHOFF " + variable.getAddress() + "\n";

            if (CompilerUtils.check(f, ',')) {
                continue;
            } else if (CompilerUtils.check(f, ';')) {
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

        if (!CompilerUtils.check(f, '{')) {
            throw new SyntaxErrorException(
                "getBlock expects '{' at start of block",
                f.lineNo()
            );
        }

        // while not "}"
        while (!CompilerUtils.check(f, '}')) {
            sam += getStmt(f);
        }

        return sam;
    }

    static String getStmt(SamTokenizer f) throws CompilerException {
        String sam = "";

        if (CompilerUtils.check(f, ';')) {
            return sam; // Null statement
        }

        Variable variable = getVar(f);

        if (!CompilerUtils.check(f, '=')) {
            throw new SyntaxErrorException(
                "getStmt expects '=' after variable",
                f.lineNo()
            );
        }

        // getExpr() would return something on the stack
        sam += getExpr(f);

        // Store item on the stack to Variable
        sam += "STOREOFF " + variable.getAddress() + "\n";

        if (!CompilerUtils.check(f, ';')) {
            throw new SyntaxErrorException(
                "getStmt expects ';' at end of statement",
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

        String varName = CompilerUtils.getWord(f);

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
        // Expr -> ( ... )
        if (CompilerUtils.check(f, '(')) {
            String sam = "";
            // Do stuffs
        }

        // Expr -> Var or Expr -> Literal(bool)
        if (f.peekAtKind() == TokenType.WORD) {
            String boolOrVar = CompilerUtils.getWord(f);

            // Expr -> Literal(bool)
            if (boolOrVar.equals("true")) {
                return "PUSHIMM 1\n";
            }
            if (boolOrVar.equals("false")) {
                return "PUSHIMM 0\n";
            }

            // Expr -> Var
            Variable variable = variables.get(boolOrVar);

            if (variable.hasValue()) {
                switch (variable.getType()) {
                    case INT:
                        return "PUSHIMM " + variable.getVal() + "\n";
                    case BOOL:
                        return variable.getVal().equals("true")
                            ? "PUSHIMM 1\n"
                            : "PUSHIMM 0\n";
                    case STRING:
                        return "PUSHIMMSTR \"" + variable.getVal() + "\"\n";
                    default:
                        throw new TypeErrorException(
                            "getExpr received invalid type",
                            f.lineNo()
                        );
                }
            } else {
                return "PUSHOFF " + variable.getAddress() + "\n";
            }
        }

        // Expr -> Literal (not bool)
        return getLiteral(f);
    }

    static String getLiteral(SamTokenizer f) throws CompilerException {
        // System.out.println(f.peekAtKind());
        switch (f.peekAtKind()) {
            case INTEGER:
                int value = CompilerUtils.getInt(f);
                return "PUSHIMM " + value + "\n";
            case STRING:
                String strValue = CompilerUtils.getString(f);
                System.out.println(strValue);
                return "PUSHIMMSTR \"" + strValue + "\"\n";
            case WORD:
                String bool = CompilerUtils.getWord(f);
                if (bool == "true") {
                    return "PUSHIMM 1\n";
                } else if (bool == "false") {
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
