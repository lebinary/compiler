package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataGenerator {

    static String generateStaticData() throws CompilerException {
        String sam = "vtables:\n";

        for (Symbol child : LiveOak3Compiler.globalSymbol.children) {
            sam += generateVTable((ClassSymbol) child);
        }

        return sam;
    }

    static String freeStaticData() throws CompilerException {
        // Note: you should have only main's return value on top of the stack before running this
        String sam = "";

        for (Symbol child : LiveOak3Compiler.globalSymbol.children) {
            sam += "SWAP\n";
            sam += "FREE\n";
        }

        return sam;
    }

    static String generateVTable(ClassSymbol classSymbol)
        throws CompilerException {
        String sam = "";
        sam += "PUSHIMM " + classSymbol.virtualMethods.size() + "\n";
        sam += "MALLOC\n";

        for (MethodSymbol method : classSymbol.virtualMethods) {
            sam += "DUP\n";
            sam += "PUSHIMM " + method.address + "\n";
            sam += "ADD\n";
            sam += "PUSHIMMPA " + method.name + "\n";
            sam += "STOREIND\n";
        }

        return sam;
    }
}
