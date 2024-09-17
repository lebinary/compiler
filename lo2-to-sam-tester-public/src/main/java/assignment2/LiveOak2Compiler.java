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

public class LiveOak2Compiler {

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
        // LiveOak-2
        // while(f.peekAtKind()!=TokenType.EOF) {
        //     pgm+= getMethod(f);
        // }


        // LiveOak-0
        while(f.peekAtKind()!=TokenType.EOF) {
            pgm += getBody(f);
        }
        return pgm;
    }

    static String getMethod(SamTokenizer f) {
        //TODO: add code to convert a method declaration to SaM code.
        //TODO: add appropriate exception handlers to generate useful error msgs.
        f.check("int"); //must match at begining
        String methodName = f.getString();
        f.check("("); // must be an opening parenthesis
        String formals = getFormals(f);
        f.check(")"); // must be an closing parenthesis
        //You would need to read in formals if any
        //And then have calls to getDeclarations and getStatements.
        return null;
    }

    static String getExp(SamTokenizer f) {
        return "";
    }

    static String getFormals(SamTokenizer f) {
        return null;
    }

    /** LiveOak 0
     **/
    static String getBody(SamTokenizer f) throws CompilerException {
        String body = "";

        // while start with "int | bool | String"
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl will store variable in Hashmap: identifier -> { type: TokenType, relative_address: int }
            parseVarDecl(f);
        }
        CompilerUtils.printHashmap(variables);

        // Then, get Block
        body += getBlock(f);

        return body;
    }

    static void parseVarDecl(SamTokenizer f) throws CompilerException {
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
            Variable variable = new Variable(varName, varType, address);
            variables.put(varName, variable);

            if(f.check(',')) {
                continue;
            }
            else if(f.check(';')) {
                break;
            } else {
                throw new SyntaxErrorException(
                    "Expected ',' or `;` after each variable declaration",
                    f.lineNo()
                );
            }
        }
    }

    static String getBlock(SamTokenizer f) throws CompilerException {
        String block = "";

        // while start with "{"
        while(f.check('{')) {
            block += getStmt(f);
        }

        return block;
    }

    static String getStmt(SamTokenizer f) throws CompilerException {
        Variable variable = getVar(f);
        String variableExpr = "STOREOFF " + variable.getAddress();

        if(f.check('=')) {
            throw new SyntaxErrorException(
                "getStmt expects '=' after variable",
                f.lineNo()
            );
        }

        String expression = getExpr(f);

        return expression + variableExpr;
    }

    static Variable getVar(SamTokenizer f) throws CompilerException {
        // Not a var, raise
        if(f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getVar should starts with a WORD",
                f.lineNo()
            );
        }

        String varName = f.getWord();

        // Trying to access var that has not been declared
        Variable variable = variables.get(varName);
        if(variable == null) {
            throw new SyntaxErrorException(
                "getVar trying to access variable that has not been declared",
                f.lineNo()
            );
        }

        return variable;
    }

    static String getExpr(SamTokenizer f) throws CompilerException {
        String expr = "";

        // Expr -> ( ... )
        if(f.check('(')) {
            // Do stuffs
        }

        // Expr -> Var
        try {
            Variable variable = getVar(f);

        } catch (SyntaxErrorException e) {
            // Expr -> Literal

        }

        return "";
    }

    static String getLiteral(SamTokenizer f) throws CompilerException {
        switch (f.peekAtKind()) {
            case INTEGER:
                return Integer.toString(f.getInt());
            case STRING:
                return f.getString();
            case WORD:
                return f.getWord();
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
