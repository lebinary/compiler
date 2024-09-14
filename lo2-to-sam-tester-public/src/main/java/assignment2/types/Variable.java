package assignment2;

public class Variable {
    private String name;
    private Type type;
    private int address;

    public Variable(String name, Type type, int address) {
        this.name = name;
        this.type = type;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return "VariableInfo{" +
               "name='" + name + '\'' +
               ", type='" + type + '\'' +
               ", address=" + address +
               "}";
    }
}
