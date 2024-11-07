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
    public int vtableAddress;
    public List<VariableSymbol> instanceVariables;
    public List<MethodSymbol> instanceMethods;
    public List<MethodSymbol> virtualMethods;

    public ClassSymbol(Symbol parent, String name, int address) {
        super(parent, new ArrayList<>(), name, address);
        this.instanceVariables = new ArrayList<>();
        this.instanceMethods = new ArrayList<>();
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
            // constuctor
            MethodSymbol methodChild = (MethodSymbol) child;
            if (methodChild.isVirtual) {
                methodChild.address = virtualMethods.size(); // method's address relative to vtable
                virtualMethods.add(methodChild);
            } else {
                methodChild.address = instanceMethods.size(); // method's address relative to class-record
                instanceMethods.add(methodChild);
            }
        } else if (child instanceof ClassSymbol) {
            ((ClassSymbol) child).vtableAddress = ClassSymbol.nextVTableAddress;
            ClassSymbol.nextVTableAddress++;
        }
    }

    public void addChild(Symbol child) {
        udpateSuper(child);
        updateProperties(child);
    }

    public void reset() {
        super.reset();

        this.instanceVariables.clear();
        this.virtualMethods.clear();
    }

    public int getSize() {
        return instanceVariables.size() + 1; // extra bit is for vtable
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
        return vtableAddress == that.vtableAddress;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), vtableAddress);
    }
}
