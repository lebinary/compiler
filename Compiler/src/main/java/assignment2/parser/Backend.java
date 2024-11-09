package assignment2;

public class Backend {

    // Stack Operations
    public static String push() {
        return "PUSH\n";
    }

    public static String pop() {
        return "POP\n";
    }

    public static String dup() {
        return "DUP\n";
    }

    public static String swap() {
        return "SWAP\n";
    }

    public static String pushImmediate(int value) {
        return "PUSHIMM " + value + "\n";
    }

    public static String pushImmediateString(String value) {
        return "PUSHIMMSTR \"" + value + "\"\n";
    }

    public static String pushImmediateChar(char value) {
        return "PUSHIMMCH '" + value + "'\n";
    }

    public static String pushOffset(int offset) {
        return "PUSHOFF " + offset + "\n";
    }

    public static String pushIndirect() {
        return "PUSHIND\n";
    }

    public static String pushImmediateAddress(String label) {
        return "PUSHIMMPA " + label + "\n";
    }

    public static String pushAbsolute(int stackOffset) {
        return "PUSHABS " + stackOffset + "\n";
    }

    // Memory Operations
    public static String malloc() {
        return "MALLOC\n";
    }

    public static String free() {
        return "FREE\n";
    }

    public static String storeIndirect() {
        return "STOREIND\n";
    }

    public static String storeOffset(int offset) {
        return "STOREOFF " + offset + "\n";
    }

    // Arithmetic Operations
    public static String add() {
        return "ADD\n";
    }

    public static String subtract() {
        return "SUB\n";
    }

    public static String multiply() {
        return "TIMES\n";
    }

    public static String divide() {
        return "DIV\n";
    }

    public static String mod() {
        return "MOD\n";
    }

    // Comparison Operations
    public static String greater() {
        return "GREATER\n";
    }

    public static String less() {
        return "LESS\n";
    }

    public static String compare() {
        return "CMP\n";
    }

    public static String equal() {
        return "EQUAL\n";
    }

    public static String isNegative() {
        return "ISNEG\n";
    }

    public static String isNil() {
        return "ISNIL\n";
    }

    public static String isPositive() {
        return "ISPOS\n";
    }

    // Control Flow
    public static String jump(String label) {
        return "JUMP " + label + "\n";
    }

    public static String jumpConditional(String label) {
        return "JUMPC " + label + "\n";
    }

    public static String label(String name) {
        return name + ":\n";
    }

    public static String stop() {
        return "STOP\n";
    }

    // Function Operations
    public static String link() {
        return "LINK\n";
    }

    public static String unlink() {
        return "UNLINK\n";
    }

    public static String jumpSubroutine(String label) {
        return "JSR " + label + "\n";
    }

    public static String returnSubroutine() {
        return "RST\n";
    }

    // Stack Pointer Manipulation
    public static String addStackPointer(int value) {
        return "ADDSP " + value + "\n";
    }

    // Logical Operations
    public static String and() {
        return "AND\n";
    }

    public static String or() {
        return "OR\n";
    }
}
