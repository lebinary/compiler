package assignment2.errors;

public class CompilerException extends Exception {
    private final int line;

    public CompilerException(String message, int line) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return "Error at line " + line + ": " + getMessage();
    }
}
