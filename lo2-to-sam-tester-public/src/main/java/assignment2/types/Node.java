package assignment2;

import java.util.Objects;

public class Node {

    String name;
    Type type;
    String val;
    int address;
    Method method;


    public Node(String name, Type type, int address, Method method, String val) {
        this.name = name;
        this.type = type;
        this.address = address;
        this.method = method;
        this.val = val;
    }

    public Node(String name, Type type, int address) {
        this(name, type, address, MainMethod.getInstance(), null);
    }

    public Node(String name, Type type, int address, Method method) {
        this(name, type, address, method, null);
    }

    // bool checks
    public boolean hasValue() {
        return val != null && !val.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return address == node.address &&
               name.equals(node.name) &&
               type == node.type &&
               Objects.equals(method, node.method);
    }
}
