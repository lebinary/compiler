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

    public void updateValue(Object value) {
        this.value = value;
    }

    public void addChild(Node child) {
        child.parent = this;
        children.add(child);
        if (child.name != null) {
            symbols.put(child.name, child);
        }
    }

    public Node lookupSymbol(String name) {
        Node symbol = symbols.get(name);
        if (symbol != null) return symbol;

        // Not allowing access to parent scope at the moment
        // if (parent != null) return parent.lookupSymbol(name);
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
            Objects.equals(parent, node.parent) &&
            Objects.equals(children, node.children) &&
            Objects.equals(name, node.name) &&
            Objects.equals(value, node.value) &&
            type == node.type &&
            address == node.address
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, children, name, type, address);
    }
}
