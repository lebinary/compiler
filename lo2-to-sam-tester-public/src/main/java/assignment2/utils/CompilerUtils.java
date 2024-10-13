package assignment2;

import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class CompilerUtils {

    /** General utils
     **/
    public static String generateLabel() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        if (!Character.isLetter(uuid.charAt(0))) {
            char randomLetter = (char) ('a' + Math.random() * ('z' - 'a' + 1));
            uuid = randomLetter + uuid.substring(1);
        }
        return uuid;
    }

    /** Symbol Tables Utils
     **/
    public static String getMethodsSam(Map<String, Node> symbolTable) {
        String sam = "";
        for (Map.Entry<String, Node> entry : symbolTable.entrySet()) {
            Node node = entry.getValue();

            // add an if here to fiter out functions
            sam += node.value;
        }
        return sam;
    }

    public static void printSymbolTable(Map<String, Node> symbolTable) {
        System.out.println("Current symbolTable:");
        for (Map.Entry<String, Node> entry : symbolTable.entrySet()) {
            Node node = entry.getValue();
            System.out.println(node.toString());
        }
        System.out.println();
    }

    public static int getNextAddress(Map<String, Node> symbolTable) {
        return (
            symbolTable
                .values()
                .stream()
                .mapToInt(node -> node.address)
                .max()
                .orElse(-1) +
            1
        ); // Start at 0 if the map is empty
    }

    /** Processed Tokens Utils
     **/
    public static ArrayList<String> processedTokens = new ArrayList<>();

    public static void clearTokens() {
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
