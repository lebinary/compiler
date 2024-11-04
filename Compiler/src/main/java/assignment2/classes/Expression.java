package assignment2;

import java.util.Objects;

public class Expression {

    String samCode;
    Type type;
    Object value;

    public Expression(String samCode, Type type, Object value) {
        this.samCode = samCode;
        this.type = type;
        this.value = value;
    }

    // Constructor with default values
    public Expression(String samCode, Type type) {
        this(samCode, type, null);
    }

    public Expression() {
        this("", Type.INT, null);
    }
}
