package compiler.errors;

public class TypeErrorException extends CompilerException {
    public TypeErrorException(String message, int line) {
        super(message, line);
    }

    @Override
    public String toString() {
        return "Type error at line " + getLine() + ": " + getMessage();
    }
}
