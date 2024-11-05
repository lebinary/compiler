package assignment2;

import java.util.ArrayList;

public class MainMethod extends MethodSymbol {

    private static MainMethod instance = null;

    private MainMethod() {
        super(null, "main", Type.INT, 0);
    }

    public static synchronized MainMethod getInstance() {
        if (instance == null) {
            instance = new MainMethod();
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        instance = null;
    }
}
