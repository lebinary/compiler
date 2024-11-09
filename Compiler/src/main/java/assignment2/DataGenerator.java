package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataGenerator {

    static String generateStaticData(ClassSymbol symbolTable)
        throws CompilerException {
        StringBuilder sam = new StringBuilder("vtables:\n");

        for (Symbol child : symbolTable.children) {
            sam.append(generateVTable((ClassSymbol) child));
        }

        return sam.toString();
    }

    static String freeStaticData(ClassSymbol symbolTable)
        throws CompilerException {
        // Note: you should have only main's return value on top of the stack before running this
        StringBuilder sam = new StringBuilder();

        for (Symbol child : symbolTable.children) {
            sam.append("SWAP\n").append("FREE\n");
        }

        return sam.toString();
    }

    static String generateVTable(ClassSymbol classSymbol)
        throws CompilerException {
        StringBuilder sam = new StringBuilder();
        sam
            .append("PUSHIMM ")
            .append(classSymbol.virtualMethods.size())
            .append("\n")
            .append("MALLOC\n");

        for (MethodSymbol method : classSymbol.virtualMethods) {
            sam
                .append("DUP\n")
                .append("PUSHIMM ")
                .append(method.address)
                .append("\n")
                .append("ADD\n")
                .append("PUSHIMMPA ")
                .append(method.getLabelName())
                .append("\n")
                .append("STOREIND\n");
        }

        return sam.toString();
    }
}
