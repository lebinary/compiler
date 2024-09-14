package assignment2;

import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;
import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;


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
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName))) {
                writer.write(samCode);
            }
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // maps identifier -> variable
    public static Map<String, Variable> variables = new HashMap<String, Variable>();

    static String compiler(String fileName) {
        //returns SaM code for program in file
        try {
            SamTokenizer f = new SamTokenizer(fileName, SamTokenizer.TokenizerOptions.PROCESS_STRINGS);
            String pgm = getProgram(f);
            return pgm;
        } catch (CompilerException e) {
            System.out.println("COMPILE ERROR: " + e.getMessage());
            return "STOP\n";
        } catch (Exception e) {
            System.out.println("SOMETHING WENT WRONG: " + e.getMessage());
            return "STOP\n";
        }
    }

    static String getProgram(SamTokenizer f) throws CompilerException {
        try {
            String pgm="";
            // LiveOak-2
            // while(f.peekAtKind()!=TokenType.EOF) {
            //     pgm+= getMethod(f);
            // }
            // LiveOak-0
            pgm += getBody(f);
            return pgm;
        } catch(Exception e) {
            String errorMessage = "Fatal error: could not compile program";
            System.out.println(errorMessage);
            System.out.println(e);
            return "STOP\n";
        }
    }

    static String getMethod(SamTokenizer f) {
        //TODO: add code to convert a method declaration to SaM code.
        //TODO: add appropriate exception handlers to generate useful error msgs.
        f.check("int"); //must match at begining
        String methodName = f.getString();
        f.check ("("); // must be an opening parenthesis
        String formals = getFormals(f);
        f.check(")");  // must be an closing parenthesis
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

        // while not Block, declare Var
        String typeString = f.getWord();
        Type varType = Type.fromString(typeString);
        while (varType != null) {
            // VarDecl will store variable in Hashmap: identifier -> { type: TokenType, relative_address: int }
            parseVarDecl(f);
        }

        CompilerUtils.printHashmap(variables);
        // Then, get Block

        return body;
    }

    static void parseVarDecl(SamTokenizer f) throws CompilerException {
        // handle Type
        String typeString = f.getWord();
        Type varType = Type.fromString(typeString);
        if (varType != null) {
            throw new TypeErrorException("Invalid type: " + typeString, f.lineNo());
        }
        f.skipToken();

        // put variables in hashmap
        while (f.getCharacter() != ';') {
            String varName = f.getString();

            int address = CompilerUtils.getNextAddress(variables);
            Variable variable = new Variable(varName, varType, address);
            variables.put(varName, variable);

            // BUG HERE
            if(!f.check(',') && f.getCharacter() != ';') {
                throw new SyntaxErrorException("Expected ',' or `;` after each variable declaration", f.lineNo());
            }
        }
        f.skipToken(); // skip ';'
    }

    static String getIdentifier(SamTokenizer f) {
        return null;
    }
}
