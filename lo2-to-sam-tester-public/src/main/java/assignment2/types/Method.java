package assignment2;

import java.util.ArrayList;
import java.util.List;

public class Method {

    String name;
    Type returnType;
    List<Node> parameters;
    List<Node> localVariables;

    public Method(
        String name,
        Type returnType,
        List<Node> parameters,
        List<Node> localVariables
    ) {
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
        this.localVariables = localVariables;
    }

    // Constructor with default values
    public Method() {
        this("main", Type.INT, new ArrayList<>(), new ArrayList<>());
    }

    public Method(String name, Type returnType) {
        this(name, returnType, new ArrayList<>(), new ArrayList<>());
    }

    public int getNextAddress() {
        return -(1 + parameters.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Method method = (Method) o;
        return name.equals(method.name) && returnType == method.returnType;
    }
}
