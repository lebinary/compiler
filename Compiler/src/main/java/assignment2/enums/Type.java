package assignment2;

import java.util.HashSet;
import java.util.Set;

public class Type {

    // Built-in types
    public static final Type VOID = new Type("void");
    public static final Type INT = new Type("int");
    public static final Type BOOL = new Type("bool");
    public static final Type STRING = new Type("String");

    // Keep track of all types (both built-in and declared)
    private static final Set<Type> allTypes = new HashSet<>();

    // Initialize built-in types
    static {
        allTypes.add(VOID);
        allTypes.add(INT);
        allTypes.add(BOOL);
        allTypes.add(STRING);
    }

    private final String typeName;
    private final boolean isClass;

    private Type(String typeName) {
        this.typeName = typeName;
        this.isClass =
            !typeName.equals("int") &&
            !typeName.equals("bool") &&
            !typeName.equals("String");
    }

    // Single method to create or get any type
    public static Type getType(String typeName) {
        for (Type type : allTypes) {
            if (type.typeName.equals(typeName)) {
                return type;
            }
        }
        return null;
    }

    // Factory method for creating class types
    public static Type createClassType(String className) {
        Type existingType = getType(className);
        if (existingType != null) {
            return existingType;
        }
        Type newType = new Type(className);
        allTypes.add(newType);
        return newType;
    }

    public boolean isCompatibleWith(Type other) {
        if (other == null || this == null) return false;

        // Allow null to be assigned to String or custom classes
        if (
            (this == VOID && !other.isPrimitive()) ||
            (other == VOID && !this.isPrimitive())
        ) {
            return true;
        }
        // For now, types are only compatible if they're exactly the same
        return this == other;
    }

    public boolean isPrimitive() {
        return this == INT || this == BOOL;
    }

    public boolean isClass() {
        return isClass;
    }

    @Override
    public String toString() {
        return typeName;
    }
}
