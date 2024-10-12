package assignment2;

import java.util.ArrayList;

public class MainMethod extends MethodNode {

    private static MainMethod instance = null;

    private MainMethod() {
        super(null, new ArrayList<>(), "main", Type.INT, 0);
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
