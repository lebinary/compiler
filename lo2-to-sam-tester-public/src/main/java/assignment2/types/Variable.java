package assignment2;

import java.util.Objects;

public class Variable {

    private String name;
    private Type type;
    private String val;
    private int address;

    public Variable(String name, Type type, String val, int address) {
        this.name = name;
        this.type = type;
        this.val = val;
        this.address = address;
    }

    // getters
    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getVal() {
        return val;
    }

    public int getAddress() {
        return address;
    }

    // setters
    public void setVal(String val) {
        this.val = val;
    }

    // bool checks
    public boolean hasValue() {
        return val != null && !val.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return (
            address == variable.address &&
            name.equals(variable.name) &&
            type == variable.type
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, address);
    }

    @Override
    public String toString() {
        return (
            "var {" +
            "name='" +
            name +
            '\'' +
            ", type='" +
            type +
            '\'' +
            ", val='" +
            val +
            '\'' +
            ", address=" +
            address +
            " }"
        );
    }
}
