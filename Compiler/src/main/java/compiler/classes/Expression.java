package compiler;

import java.util.Objects;

public class Expression {

    String samCode;
    Type type;

    public Expression(String samCode, Type type) {
        this.samCode = samCode;
        this.type = type;
    }

    public Expression() {
        this("", Type.INT);
    }
}
