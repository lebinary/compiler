package assignment2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public class MethodNode extends Node {

    public List<VariableNode> parameters;
    public List<VariableNode> localVariables;
    private Deque<String> exitLabels;
    private Deque<Statement> statements;

    public MethodNode(
        Node parent,
        List<Node> children,
        String name,
        Type returnType,
        int address
    ) {
        super(parent, children, name, returnType, address, null);
        this.parameters = new ArrayList<>();
        this.localVariables = new ArrayList<>();
        this.exitLabels = new ArrayDeque<>();
        this.statements = new ArrayDeque<>();

        // Populate parameters and localVariables
        for (Node child : children) {
            if (child instanceof VariableNode) {
                udpateParamsAndLocals(child);
            }
        }
    }

    // Constructor with default values
    public MethodNode() {
        this(null, new ArrayList<>(), "main", Type.INT, 0);
    }

    public MethodNode(String name, Type returnType) {
        this(null, new ArrayList<>(), name, returnType, 0);
    }

    // update child's address and categorize them
    public void udpateParamsAndLocals(Node child) {
        VariableNode castChild = (VariableNode) child;
        if (castChild.isParameter) {
            castChild.address = -1;
            parameters.add(castChild);

            // correct address of other params
            for (int i = parameters.size() - 2; i >= 0; i--) {
                parameters.get(i).address -= 1;
            }
        } else {
            castChild.address = getNextLocalAddress();
            localVariables.add(castChild);
        }
    }

    // Tree operations
    public void addChild(Node child) {
        super.addChild(child);

        if (child instanceof VariableNode) {
            udpateParamsAndLocals(child);
        }
    }

    public void reset() {
        super.reset();

        this.parameters.clear();
        this.localVariables.clear();
    }

    // Params and Locals operations
    public int getNextParamAddress() {
        return -(1 + parameters.size());
    }

    public int getNextLocalAddress() {
        return 2 + localVariables.size();
    }

    public int returnAddress() {
        return -(1 + parameters.size());
    }

    public int numLocalVariables() {
        return localVariables.size();
    }

    public int numParameters() {
        return parameters.size();
    }

    // Exit loops stack operations
    public void pushExitLabel(String label) {
        exitLabels.push(label);
    }

    public String popExitLabel() {
        return exitLabels.pop();
    }

    public String peekExitLabel() {
        return exitLabels.isEmpty() ? null : exitLabels.peek();
    }

    // Statements stack operations
    public void pushStatement(Statement statement) {
        statements.push(statement);
    }

    public Statement popStatement() {
        return statements.pop();
    }

    public Statement peekStatement() {
        return statements.isEmpty() ? null : statements.peek();
    }

    public boolean hasStatement(Statement statement) {
        return statements.contains(statement);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodNode)) return false;
        if (!super.equals(o)) return false;
        MethodNode that = (MethodNode) o;
        return Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parameters);
    }
}