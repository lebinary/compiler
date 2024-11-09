package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {

    /*** Backend code that is used in many places
     ***/

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

        // Invalid identifier
        if (Tokenizer.isReservedWord(identifier)) {
            throw new SyntaxErrorException(
                "Identifier cannot be a reserved word",
                f.lineNo()
            );
        }

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
}
