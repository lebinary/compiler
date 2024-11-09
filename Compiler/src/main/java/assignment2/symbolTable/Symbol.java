package assignment2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Symbol {

    /*** Instance properties
     ***/
    Symbol parent;
    List<Symbol> children;
    Map<String, Symbol> symbolTable = new HashMap<>();

    String name;
    int address;

    /*** Abstract
     ***/
    public abstract Type getType();
    public abstract void addChild(Symbol child);

    /** Constructors
     **/
    public Symbol(
        Symbol parent,
        List<Symbol> children,
        String name,
        int address
    ) {
        this.parent = parent;
        this.children = children;
        this.name = name;
        this.address = address;

        // Populate symbolTable with children that have names
        for (Symbol child : this.children) {
            if (child.name != null && !child.name.isEmpty()) {
                this.symbolTable.put(child.name, child);
            }
        }
    }

    public Symbol(String name, int address) {
        this(null, new ArrayList<>(), name, address);
    }

    public Symbol() {
        this(null, new ArrayList<>(), "", 0);
    }

    protected void udpateSuper(Symbol child) {
        child.parent = this;
        children.add(child);
        if (child.name != null) {
            symbolTable.put(child.name, child);
        }
    }

    public Symbol lookupSymbol(String name) {
        return lookupSymbol(name, null);
    }

    public <T extends Symbol> T lookupSymbol(String name, Class<T> type) {
        Symbol symbol = symbolTable.get(name);
        if (symbol != null) {
            if (type == null || type.isInstance(symbol)) {
                @SuppressWarnings("unchecked")
                T result = (T) symbol;
                return result;
            }
        }

        if (parent != null) {
            return parent.lookupSymbol(name, type);
        }
        return null;
    }

    public boolean existSymbol(String name) {
        Symbol exist = lookupSymbol(name);

        return exist == null ? false : true;
    }

    public void reset() {
        this.parent = null;
        this.symbolTable.clear();
        this.children.clear();
        this.name = "";
        this.address = 0;
    }

    public void resetRecursive() {
        for (Symbol child : this.children) {
            child.resetRecursive();
        }
        this.reset();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Symbol symbol = (Symbol) o;
        return (address == symbol.address && Objects.equals(name, symbol.name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, address); // dont hash parent and children to avoid stack overflow
    }
}
