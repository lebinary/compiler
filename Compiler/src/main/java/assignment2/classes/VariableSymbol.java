package assignment2;

import java.util.ArrayList;
import java.util.Objects;

public class VariableSymbol extends Symbol {

    boolean isParameter;

    public VariableSymbol(
        Symbol parent,
        String name,
        Type type,
        int address,
        Object value,
        boolean isParameter
    ) {
        super(parent, new ArrayList<>(), name, type, address, value);
        this.isParameter = isParameter;
    }

    public VariableSymbol(String name, Type type, boolean isParameter) {
        this(null, name, type, 0, null, isParameter);
    }

    @Override
    public void addChild(Symbol child) {
        throw new UnsupportedOperationException(
            "VariableSymbol cannot have children"
        );
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
