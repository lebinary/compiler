package assignment2;

import java.util.Map;

public class SymbolUtils {

    public static void print(Symbol symbolTable) {
        printSymbolRecursive(symbolTable, 0, "");
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
                .append(methodSymbol.localVariables.size())
                .append(", isVirtual=")
                .append(methodSymbol.isVirtual);
        } else if (symbol instanceof ClassSymbol) {
            sb
                .append(", vtableAddress=")
                .append(((ClassSymbol) symbol).vtableAddress)
                .append(", instanceVariables=")
                .append(((ClassSymbol) symbol).instanceVariables.size())
                .append(", virtualMethods=")
                .append(((ClassSymbol) symbol).virtualMethods.size());
        }

        sb.append(")");
        return sb.toString();
    }
}
