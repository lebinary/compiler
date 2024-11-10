package compiler;

import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    private static final String[] RESERVED_WORDS = {
        "class",
        "void",
        "int",
        "bool",
        "string",
        "new",
        "if",
        "else",
        "while",
        "return",
        "this",
        "null",
        "true",
        "false",
    };

    public static ArrayList<String> processedTokens = new ArrayList<>();

    public static boolean isReservedWord(String identifier) {
        for (String word : RESERVED_WORDS) {
            if (word.equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    public static void reset() {
        processedTokens.clear();
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
