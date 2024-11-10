package compiler;

import compiler.errors.CompilerException;
import compiler.errors.SyntaxErrorException;
import compiler.errors.TypeErrorException;
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
        StringBuilder sam = new StringBuilder("vtables:\n"); // Keep raw string for label

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
            sam.append(Backend.swap()).append(Backend.free());
        }

        return sam.toString();
    }

    private static String generateVTable(ClassSymbol classSymbol)
        throws CompilerException {
        StringBuilder sam = new StringBuilder();

        sam
            .append(Backend.pushImmediate(classSymbol.virtualMethods.size()))
            .append(Backend.malloc());

        for (MethodSymbol method : classSymbol.virtualMethods) {
            sam
                .append(Backend.dup())
                .append(Backend.pushImmediate(method.address))
                .append(Backend.add())
                .append(Backend.pushImmediateAddress(method.getLabelName()))
                .append(Backend.storeIndirect());
        }

        return sam.toString();
    }
}
