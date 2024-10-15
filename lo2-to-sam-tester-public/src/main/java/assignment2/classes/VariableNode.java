package assignment2;

import java.util.ArrayList;
import java.util.Objects;

public class VariableNode extends Node {

    boolean isParameter;

    public VariableNode(
        Node parent,
        String name,
        Type type,
        int address,
        Object value,
        boolean isParameter
    ) {
        super(parent, new ArrayList<>(), name, type, address, value);
        this.isParameter = isParameter;
    }

    public VariableNode(String name, Type type, boolean isParameter) {
        this(null, name, type, 0, null, isParameter);
    }

    @Override
    public void addChild(Node child) {
        throw new UnsupportedOperationException(
            "VariableNode cannot have children"
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableNode)) return false;
        if (!super.equals(o)) return false;
        VariableNode that = (VariableNode) o;
        return isParameter == that.isParameter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isParameter);
    }
}
