package assignment2;

public enum Type {
    INT("int"),
    BOOL("bool"),
    STRING("String");

    private final String typeName;

    Type(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }

    public static Type fromString(String typeString) {
        for (Type type : Type.values()) {
            if (type.typeName.equals(typeString)) {
                return type;
            }
        }
        return null;
    }

    public boolean isCompatibleWith(Type other) {
        return this == other;
    }
}
