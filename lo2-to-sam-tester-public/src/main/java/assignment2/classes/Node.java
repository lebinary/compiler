package assignment2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Node {

    Node parent;
    List<Node> children;
    Map<String, Node> symbols = new HashMap<>();

    String name;
    Type type;
    int address;
    Object value;

    /** Constructors
     **/
    public Node(
        Node parent,
        List<Node> children,
        String name,
        Type type,
        int address,
        Object value
    ) {
        this.parent = parent;
        this.children = children;
        this.name = name;
        this.type = type;
        this.address = address;
        this.value = value;

        // Populate symbols with children that have names
        for (Node child : this.children) {
            if (child.name != null && !child.name.isEmpty()) {
                this.symbols.put(child.name, child);
            }
        }
    }

    public Node(String name, Type type, int address) {
        this(null, new ArrayList<>(), name, type, address, null);
    }

    public Node() {
        this(null, new ArrayList<>(), "", Type.INT, 0, null);
    }

    public void addChild(Node child) {
        child.parent = this;
        children.add(child);
        if (child.name != null) {
            symbols.put(child.name, child);
        }
    }

    public Node lookupSymbol(String name) {
        return lookupSymbol(name, null);
    }

    public <T extends Node> T lookupSymbol(String name, Class<T> type) {
        Node symbol = symbols.get(name);
        if (symbol != null) {
            if (type == null || type.isInstance(symbol)) {
                @SuppressWarnings("unchecked")
                T result = (T) symbol;
                return result;
            }
        }

        if (parent != null) {
            return parent.lookupSymbol(name, type);
        }
        return null;
    }

    public boolean existSymbol(String name) {
        Node exist = lookupSymbol(name);

        return exist == null ? false : true;
    }

    public void reset() {
        this.parent = null;
        this.symbols.clear();
        this.children.clear();
        this.name = "";
        this.type = Type.INT; // Assuming INT is the default type
        this.address = 0;
        this.value = null;
    }

    public void resetRecursive() {
        for (Node child : this.children) {
            child.resetRecursive();
        }
        this.reset();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return (
            address == node.address &&
            Objects.equals(name, node.name) &&
            type == node.type
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, address); // dont hash parent and children to avoid stack overflow
    }
}
