package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {

    /*** HELPERS
     ***/
    // Add at the top of the class with other constant definitions
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

    public static boolean isReservedWord(String identifier) {
        for (String word : RESERVED_WORDS) {
            if (word.equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    static String initObject(ClassSymbol classSymbol) {
        String sam = "";

        // instanciate class to pass in as "this" parameter
        sam += "PUSHIMM " + classSymbol.getSize() + "\n";
        sam += "MALLOC\n";
        // assign main class's vtable
        sam += "DUP\n";
        sam += "PUSHABS " + classSymbol.vtableAddress + "\n";
        sam += "STOREIND\n";

        return sam;
    }

    public static final Pattern IDENTIFIER_PATTERN = Pattern.compile(
        "^[a-zA-Z]([a-zA-Z0-9'_'])*$"
    );

    static String getIdentifier(SamTokenizer f) throws CompilerException {
        String identifier = Tokenizer.getWord(f);
        if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new SyntaxErrorException(
                "Invalid identifier: " + identifier,
                f.lineNo()
            );
        }
        return identifier;
    }

    public static String getUnop(char op) throws CompilerException {
        switch (op) {
            // TODO: string bitwise
            case '~':
                return "PUSHIMM -1\nTIMES\n";
            case '!':
                return "PUSHIMM 1\nADD\nPUSHIMM 2\nMOD\n";
            default:
                throw new TypeErrorException(
                    "getUnop received invalid input: " + op,
                    -1
                );
        }
    }

    public static String getBinop(char op) throws CompilerException {
        switch (op) {
            case '+':
                return "ADD\n";
            case '-':
                return "SUB\n";
            case '*':
                return "TIMES\n";
            case '/':
                return "DIV\n";
            case '%':
                return "MOD\n";
            case '&':
                return "AND\n";
            case '|':
                return "OR\n";
            case '>':
                return "GREATER\n";
            case '<':
                return "LESS\n";
            case '=':
                return "EQUAL\n";
            default:
                throw new TypeErrorException(
                    "getBinop received invalid input: " + op,
                    -1
                );
        }
    }

    public static BinopType getBinopType(char op) throws CompilerException {
        switch (op) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '%':
                return BinopType.ARITHMETIC;
            case '&':
            case '|':
                return BinopType.BITWISE;
            case '>':
            case '<':
            case '=':
                return BinopType.COMPARISON;
            default:
                throw new TypeErrorException(
                    "categorizeBinop received invalid input: " + op,
                    -1
                );
        }
    }

    public static String repeatString(
        Type firstInputType,
        Type secondInputType
    ) {
        // expects parameters already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();
        Label returnLabel = new Label();
        Label invalidParamLabel = new Label();

        String sam = "";

        // prepare params, String always on top
        if (firstInputType == Type.STRING) {
            sam += "SWAP\n";
        }

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -1\n"; // free second param, only first param remain with new value
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 1: loop counter
        sam += "PUSHIMM 0\n"; // local 2: increment address
        sam += "PUSHIMM 0\n"; // local 3: return address

        // validate param, if n < 0 -> return
        sam += "PUSHOFF -2\n";
        sam += "ISNEG\n";
        sam += "JUMPC " + invalidParamLabel.name + "\n";

        // allocate memory for new string -> Address
        sam += "PUSHOFF -1\n";
        sam += getStringLength();
        sam += "PUSHOFF -2\n";
        sam += "TIMES\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "MALLOC\n";
        sam += "STOREOFF 3\n";

        // return this address
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF 4\n";

        // loop...
        sam += startLoopLabel.name + ":\n";
        // check if done
        sam += "PUSHOFF 2\n";
        sam += "PUSHOFF -2\n";
        sam += "EQUAL\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // append str to memory
        sam += "PUSHIMM 0\n"; // will return next address
        sam += "PUSHOFF 3\n"; // param1: starting memory address
        sam += "PUSHOFF -1\n"; // param2: string
        sam += appendStringHeap();
        sam += "STOREOFF 3\n";

        // increase counter
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 2\n";

        // Continue loop
        sam += "JUMP " + startLoopLabel.name + "\n";

        // Stop loop
        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 4\n";
        sam += "STOREOFF -2\n";
        sam += "JUMP " + returnLabel.name + "\n";

        // Invalid param, return empty string
        sam += invalidParamLabel.name + ":\n";
        sam += "PUSHIMMSTR \"\"";
        sam += "STOREOFF -2\n";
        sam += "JUMP " + returnLabel.name + "\n";

        // Return func
        sam += returnLabel.name + ":\n";
        sam += "ADDSP -3\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }

    public static String getStringLength() {
        // expects parameters already on the stack
        Label startCountLabel = new Label();
        Label stopCountLabel = new Label();
        String sam = "";

        sam += "DUP\n";

        // START
        sam += startCountLabel.name + ":\n";
        sam += "DUP\n";
        sam += "PUSHIND\n";

        // check end of string
        sam += "ISNIL\n";
        sam += "JUMPC " + stopCountLabel.name + "\n";

        // increament count and continue loop
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "JUMP " + startCountLabel.name + "\n";

        // STOP
        sam += stopCountLabel.name + ":\n";
        sam += "SWAP\n";
        sam += "SUB\n";

        return sam;
    }

    public static String appendStringHeap() {
        // expects parameters already on the stack, String on top, Mempry address
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -2\n";
        sam += "JUMP " + exitFuncLabel.name + "\n";

        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHOFF -2\n";
        sam += "PUSHOFF -1\n";

        sam += startLoopLabel.name + ":\n";
        // put char in TOS
        // end loop if nil
        sam += "PUSHOFF 3\n";
        sam += "PUSHIND\n";
        sam += "ISNIL\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // Save to allocated memory
        sam += "PUSHOFF 2\n";
        sam += "PUSHOFF 3\n";
        sam += "PUSHIND\n";
        sam += "STOREIND\n";

        // increase address current string
        sam += "PUSHOFF 3\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 3\n";

        // increase final address string
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 2\n";

        sam += "JUMP " + startLoopLabel.name + "\n";

        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMMCH '\\0'" + "\n";
        sam += "STOREIND\n";
        sam += "PUSHOFF 2\n";
        sam += "STOREOFF -3\n";
        sam += "ADDSP -2\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }

    public static String concatString() {
        // expects parameters (2 strings) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -1\n"; // free second param, only first param remain with new value
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 2: increment address
        sam += "PUSHIMM 0\n"; // local 3: return address

        // allocate space for resulting string
        sam += "PUSHOFF -1\n";
        sam += getStringLength();
        sam += "PUSHOFF -2\n";
        sam += getStringLength();
        sam += "ADD\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "MALLOC\n";
        sam += "STOREOFF 2\n";

        // return this address
        sam += "PUSHOFF 2\n";
        sam += "STOREOFF 3\n";

        // append first string to memory
        sam += "PUSHIMM 0\n"; // will return next address
        sam += "PUSHOFF 2\n"; // param1: starting memory address
        sam += "PUSHOFF -2\n"; // param2: string
        sam += appendStringHeap();
        sam += "STOREOFF 2\n";

        // append second string to memory
        sam += "PUSHIMM 0\n";
        sam += "PUSHOFF 2\n";
        sam += "PUSHOFF -1\n";
        sam += appendStringHeap();
        sam += "STOREOFF 2\n";

        // store in the first string pos
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF -2\n";

        // clean local vars
        sam += "ADDSP -2\n";
        // return
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }

    public static String compareString(char op) throws CompilerException {
        if (getBinopType(op) != BinopType.COMPARISON) {
            throw new SyntaxErrorException(
                "compareString receive invalid operation: " + op,
                -1
            );
        }

        // expects parameters (2 strings) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -1\n"; // free second param, only first param remain with new value
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 2: counter
        sam += "PUSHIMM 0\n"; // local 3: result

        // loop...
        sam += startLoopLabel.name + ":\n";
        // reach end of string 1?
        sam += "PUSHOFF -2\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";
        sam += "ISNIL\n";

        // reach end of string 2?
        sam += "PUSHOFF -1\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";
        sam += "ISNIL\n";

        // reach end of both string, is equal
        sam += "AND\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // not end, comparing char by char
        // get char of string 1
        sam += "PUSHOFF -2\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";

        // get char of string 2
        sam += "PUSHOFF -1\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";

        // compare and store result
        sam += "CMP\n";
        sam += "STOREOFF 3\n";

        // check if done
        sam += "PUSHOFF 3\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // not done, continue to next char
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 2\n";
        sam += "JUMP " + startLoopLabel.name + "\n";

        // Stop loop
        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF -2\n";
        sam += "ADDSP -2\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        if (op == '<') {
            sam += "PUSHIMM 1\n";
        } else if (op == '>') {
            sam += "PUSHIMM -1\n";
        } else {
            sam += "PUSHIMM 0\n";
        }
        sam += "EQUAL\n";

        return sam;
    }

    public static String reverseString() {
        // expects parameter (1 string) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 2: counter
        sam += "PUSHIMM 0\n"; // local 3: increment address
        sam += "PUSHIMM 0\n"; // local 4: result

        // get string length and store in local 2
        sam += "PUSHOFF -1\n";
        sam += getStringLength();
        sam += "STOREOFF 2\n";

        // allocate space for resulting string
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "MALLOC\n";
        sam += "STOREOFF 3\n";

        // return this address
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF 4\n";

        // set EOS char first
        sam += "PUSHOFF 3\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIMMCH '\\0'" + "\n";
        sam += "STOREIND\n";

        // loop (backward)...
        sam += startLoopLabel.name + ":\n";

        // end loop if counter == 0
        sam += "PUSHOFF 2\n";
        sam += "ISNIL\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // get current address
        sam += "PUSHOFF 3\n";

        // get current char
        sam += "PUSHOFF -1\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIMM 1\n"; // subtract 1 because indexing
        sam += "SUB\n";
        sam += "PUSHIND\n";

        // store char in address
        sam += "STOREIND\n";

        // increment address
        sam += "PUSHOFF 3\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 3\n";

        // decrement counter
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "SUB\n";
        sam += "STOREOFF 2\n";

        // Continue loop
        sam += "JUMP " + startLoopLabel.name + "\n";

        // Stop loop
        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 4\n";
        sam += "STOREOFF -1\n";
        sam += "ADDSP -3\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }
}
