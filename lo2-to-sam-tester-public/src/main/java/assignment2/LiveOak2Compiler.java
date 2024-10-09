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

public class LiveOak2Compiler extends LiveOak0Compiler {

    static String getProgram(SamTokenizer f) throws CompilerException {
        String pgm = "";

        // LiveOak-0
        while (f.peekAtKind() != TokenType.EOF) {
            pgm += getMethodDecl(f);
        }
        pgm += "STOP\n";

        // define all the methods
        // pgm += CompilerUtils.getMethodsSam(symbolTable);

        return pgm;
    }

    static String getMethodDecl(SamTokenizer f) throws CompilerException{
        String sam = "";

        // MethodDecl -> Type ...
        Type methodType = getType(f);

        return "";
    }
}
