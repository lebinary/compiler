package assignment2;

import assignment2.errors.CompilerException;
import edu.utexas.cs.sam.io.SamTokenizer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

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

    static String compiler(String fileName) throws Exception {
        try {
            /*** First pass: Symbol table
             ***/
            Tokenizer.reset(); // Clear the list before starting
            SamTokenizer firstPass = new SamTokenizer(
                fileName,
                SamTokenizer.TokenizerOptions.PROCESS_STRINGS
            );
            ClassSymbol symbolTable = SymbolTableBuilder.createSymbolTable(
                firstPass
            );

            /*** Second pass: codegen
             ***/
            Tokenizer.reset();
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
}
