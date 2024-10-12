package assignment2;

import java.util.Map;

public class TreeUtils {

    public static void printTree(Node root) {
        printNodeRecursive(root, 0, "");
    }

    private static void printNodeRecursive(
        Node node,
        int depth,
        String prefix
    ) {
        String indentation = "  ".repeat(depth);

        // Print the current node
        System.out.println(indentation + prefix + nodeToString(node));

        // Print named children (symbols)
        // for (Map.Entry<String, Node> entry : node.symbols.entrySet()) {
        //     String symbolName = entry.getKey();
        //     Node symbolNode = entry.getValue();
        //     System.out.println(
        //         indentation +
        //         "  Symbol: " +
        //         symbolName +
        //         " -> " +
        //         nodeToString(symbolNode)
        //     );
        // }

        // Recursively print all children
        for (int i = 0; i < node.children.size(); i++) {
            Node child = node.children.get(i);
            String childPrefix = (i == node.children.size() - 1)
                ? "└─ "
                : "├─ ";
            printNodeRecursive(child, depth + 1, childPrefix);
        }
    }

    private static String nodeToString(Node node) {
        StringBuilder sb = new StringBuilder();
        sb
            .append(node.getClass().getSimpleName())
            .append("(name='")
            .append(node.name)
            .append("'")
            .append(", type=")
            .append(node.type)
            .append(", address=")
            .append(node.address)
            .append(", value=")
            .append(node.value);

        if (node instanceof VariableNode) {
            sb
                .append(", isParameter=")
                .append(((VariableNode) node).isParameter);
        } else if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) node;
            sb
                .append(", parameters=")
                .append(methodNode.parameters.size())
                .append(", localVariables=")
                .append(methodNode.localVariables.size());
        }

        sb.append(")");
        return sb.toString();
    }

    // Utility method to print just the symbol table of a node
    public static void printSymbolTable(Node node) {
        System.out.println("Symbol Table for " + nodeToString(node) + ":");
        for (Map.Entry<String, Node> entry : node.symbols.entrySet()) {
            System.out.println(
                "  " + entry.getKey() + " -> " + nodeToString(entry.getValue())
            );
        }
    }
}
