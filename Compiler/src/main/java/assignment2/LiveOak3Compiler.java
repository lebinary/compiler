package assignment2;

import assignment2.errors.CompilerException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LiveOak3Compiler {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println(
                "Error: Two arguments are required - input file name and output file name."
            );
            throw new Error();
        }
        String inFileName = args[0];
        String outFileName = args[1];

        try {
            String samCode = compiler(inFileName);
            try (
                BufferedWriter writer = new BufferedWriter(
                    new FileWriter(outFileName)
                )
            ) {
                writer.write(samCode);
            }
        } catch (IOException e) {
            String errorMessage =
                "Failed to compile src/test/resources/LO-2/InvalidPrograms/" +
                inFileName;
            System.err.println(errorMessage);
            throw new Error(errorMessage, e);
        } catch (CompilerException e) {
            String errorMessage =
                "Failed to compile src/test/resources/LO-2/InvalidPrograms/" +
                inFileName;
            System.err.println(errorMessage);
            // printTokens();
            throw new Error(errorMessage, e);
        } catch (Error e) {
            String errorMessage =
                "Failed to compile src/test/resources/LO-2/InvalidPrograms/" +
                inFileName;
            System.err.println(errorMessage);
            // printTokens();
            throw new Error(errorMessage, e);
        } catch (Exception e) {
            String errorMessage =
                "Failed to compile src/test/resources/LO-2/InvalidPrograms/" +
                inFileName;
            System.err.println(errorMessage);
            // printTokens();
            throw new Error(errorMessage, e);
        }
    }

    public static ArrayList<String> processedTokens = new ArrayList<>();

    public static void reset() {
        processedTokens.clear();
    }

    static String compiler(String fileName) throws Exception {
        try {
            reset(); // Clear the list before starting

            /*** First pass: Symbol table
             ***/
            SamTokenizer firstPass = new SamTokenizer(
                fileName,
                SamTokenizer.TokenizerOptions.PROCESS_STRINGS
            );
            ClassSymbol symbolTable = SymbolTableBuilder.createSymbolTable(
                firstPass
            );
            processedTokens.clear();
            // TreeUtils.printTree(globalSymbol);

            /*** Second pass: codegen
             ***/
            SamTokenizer secondPass = new SamTokenizer(
                fileName,
                SamTokenizer.TokenizerOptions.PROCESS_STRINGS
            );
            String program = CodeGenerator.getProgram(secondPass, symbolTable);

            return program;
        } catch (CompilerException e) {
            String errorMessage = createErrorMessage(fileName);
            System.err.println(errorMessage);
            // printTokens();
            throw new Error(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = createErrorMessage(fileName);
            System.err.println(errorMessage);
            // printTokens();
            throw new Error(errorMessage, e);
        }
    }

    private static String createErrorMessage(String fileName) {
        String normalizedPath = Paths.get(fileName).normalize().toString();
        String errorMessage = "Failed to compile " + normalizedPath;
        return errorMessage;
    }

    public static boolean check(SamTokenizer f, char expected) {
        boolean result = f.check(expected);
        if (result) {
            processedTokens.add(String.valueOf(expected));
        }
        return result;
    }

    public static boolean check(SamTokenizer f, String expected) {
        boolean result = f.check(expected);
        if (result) {
            processedTokens.add(expected);
        }
        return result;
    }

    public static String getWord(SamTokenizer f) {
        String word = f.getWord();
        processedTokens.add(word);
        return word;
    }

    public static String getString(SamTokenizer f) {
        String str = f.getString();
        processedTokens.add("\"" + str + "\"");
        return str;
    }

    public static int getInt(SamTokenizer f) {
        int value = f.getInt();
        processedTokens.add(String.valueOf(value));
        return value;
    }

    public static char getOp(SamTokenizer f) {
        char op = f.getOp();
        processedTokens.add(String.valueOf(op));
        return op;
    }

    public static void skipToken(SamTokenizer f) {
        f.skipToken();
        processedTokens.add(".");
    }

    public static void printTokens() {
        System.out.println("PROCESSED TOKENS:");
        for (String token : processedTokens) {
            System.out.print(token + " ");
        }
        System.out.println("\n");
    }
}
