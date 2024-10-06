package assignment2;

public class Expression {

    String samCode;
    Type type;

    public Expression(String samCode, Type type) {
        this.samCode = samCode;
        this.type = type;
    }

    // Constructor with default values
    public Expression() {
        this("", Type.INT);
    }
}
