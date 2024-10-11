package assignment2;

public class MainMethod extends Method {

    private static MainMethod instance = null;

    private MainMethod() {
        super("main", Type.INT);
    }

    public static synchronized MainMethod getInstance() {
        if (instance == null) {
            instance = new MainMethod();
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }
}
