package compiler.errors;

public class SyntaxErrorException extends CompilerException {
    public SyntaxErrorException(String message, int line) {
        super(message, line);
    }

    @Override
    public String toString() {
        return "Syntax error at line " + getLine() + ": " + getMessage();
    }
}
