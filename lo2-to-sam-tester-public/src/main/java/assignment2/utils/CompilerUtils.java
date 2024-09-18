package assignment2;

import java.util.Map;

public class CompilerUtils {
    public static void printHashmap(Map<String, Variable> variables) {
        System.out.println("Current variables:");
        for (Map.Entry<String, Variable> entry : variables.entrySet()) {
            String varName = entry.getKey();
            Variable variable = entry.getValue();
            System.out.println(variable.toString());
        }
        System.out.println();
    }

    public static int getNextAddress(Map<String, Variable> variables) {
        return variables.values().stream()
                        .mapToInt(variable -> variable.getAddress())
                        .max()
                        .orElse(-1) + 1; // Start at 0 if the map is empty
    }
}
