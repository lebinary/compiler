package assignment2;

import java.util.ArrayList;
import java.util.Objects;

public class VariableSymbol extends Symbol {

    boolean isParameter;
    Type type;

    public VariableSymbol(
        Symbol parent,
        String name,
        Type type,
        int address,
        boolean isParameter
    ) {
        super(parent, new ArrayList<>(), name, address);
        this.type = type;
        this.isParameter = isParameter;
    }

    public VariableSymbol(String name, Type type, boolean isParameter) {
        this(null, name, type, 0, isParameter);
    }

    @Override
    public void addChild(Symbol child) {
        throw new UnsupportedOperationException(
            "VariableSymbol cannot have children"
        );
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableSymbol)) return false;
        if (!super.equals(o)) return false;
        VariableSymbol that = (VariableSymbol) o;
        return isParameter == that.isParameter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isParameter);
    }
}
