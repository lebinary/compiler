package assignment2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassSymbol extends Symbol {

    /*** Static propeties
     ***/
    private static int nextVTableAddress = 0;

    /*** Instance properties
     ***/
    public int vtableAdress;
    public List<VariableSymbol> instanceVariables;
    public List<MethodSymbol> virtualMethods;

    public ClassSymbol(Symbol parent, String name, int address) {
        super(parent, new ArrayList<>(), name, address);
        this.vtableAdress = ClassSymbol.nextVTableAddress;
        this.instanceVariables = new ArrayList<>();
        this.virtualMethods = new ArrayList<>();
    }

    public ClassSymbol(String name) {
        this(null, name, 0);
    }

    // update class's vtable next address
    public void updateProperties(Symbol child) {
        if (child instanceof VariableSymbol) {
            child.address = instanceVariables.size();
            instanceVariables.add((VariableSymbol) child);
        } else if (child instanceof MethodSymbol) {
            child.address = virtualMethods.size(); // method's address relative to vtable
            virtualMethods.add((MethodSymbol) child);
        }
    }

    public void addChild(Symbol child) {
        udpateSuper(child);
        updateProperties(child);
    }

    @Override
    public Type getType() {
        throw new UnsupportedOperationException(
            "ClassSymbol does not have primary type"
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassSymbol)) return false;
        if (!super.equals(o)) return false;
        ClassSymbol that = (ClassSymbol) o;
        return vtableAdress == that.vtableAdress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), vtableAdress);
    }
}
