package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile(
        "^[a-zA-Z]([a-zA-Z0-9'_'])*$"
    );

    public static String getIdentifier(SamTokenizer f)
        throws CompilerException {
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

    public static Type getType(SamTokenizer f) throws CompilerException {
        // typeString = "int" | "bool" | "String"
        String typeString = Tokenizer.getWord(f);
        Type type = Type.getType(typeString);

        // typeString != INT | BOOL | STRING
        if (type == null) {
            throw new TypeErrorException(
                new StringBuilder()
                    .append("Invalid type: ")
                    .append(typeString)
                    .toString(),
                f.lineNo()
            );
        }

        return type;
    }
}
