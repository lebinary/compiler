package assignment2;

import java.util.Map;

public class TreeUtils {

    public static void printTree(Symbol root) {
        printSymbolRecursive(root, 0, "");
    }

    private static void printSymbolRecursive(
        Symbol symbol,
        int depth,
        String prefix
    ) {
        String indentation = "  ".repeat(depth);

        // Print the current symbol
        System.out.println(indentation + prefix + symbolToString(symbol));

        // Print named children (symbolTable)
        // for (Map.Entry<String, Symbol> entry : symbol.symbolTable.entrySet()) {
        //     String symbolName = entry.getKey();
        //     Symbol symbolSymbol = entry.getValue();
        //     System.out.println(
        //         indentation +
        //         "  Symbol: " +
        //         symbolName +
        //         " -> " +
        //         symbolToString(symbolSymbol)
        //     );
        // }

        // Recursively print all children
        for (int i = 0; i < symbol.children.size(); i++) {
            Symbol child = symbol.children.get(i);
            String childPrefix = (i == symbol.children.size() - 1)
                ? "└─ "
                : "├─ ";
            printSymbolRecursive(child, depth + 1, childPrefix);
        }
    }

    private static String symbolToString(Symbol symbol) {
        StringBuilder sb = new StringBuilder();

        sb
            .append(symbol.getClass().getSimpleName())
            .append("(name='")
            .append(symbol.name)
            .append("'")
            .append(", address=")
            .append(symbol.address);

        if (symbol instanceof VariableSymbol) {
            sb
                .append(", type=")
                .append(symbol.getType())
                .append(", isParameter=")
                .append(((VariableSymbol) symbol).isParameter);
        } else if (symbol instanceof MethodSymbol) {
            MethodSymbol methodSymbol = (MethodSymbol) symbol;
            sb
                .append(", returnType=")
                .append(symbol.getType())
                .append(", parameters=")
                .append(methodSymbol.parameters.size())
                .append(", localVariables=")
                .append(methodSymbol.localVariables.size());
        }

        sb.append(")");
        return sb.toString();
    }

    // Utility method to print just the symbol table of a symbol
    public static void printSymbolTable(Symbol symbol) {
        System.out.println("Symbol Table for " + symbolToString(symbol) + ":");
        for (Map.Entry<String, Symbol> entry : symbol.symbolTable.entrySet()) {
            System.out.println(
                "  " +
                entry.getKey() +
                " -> " +
                symbolToString(entry.getValue())
            );
        }
    }
}
