package compiler;

import java.util.UUID;

public class Label {

    String name;
    LabelType type;

    public Label(String name, LabelType type) {
        this.name = name;
        this.type = type;
    }

    // Constructor with default values
    public Label() {
        this(generateUUID(), LabelType.DEFAULT);
    }

    public Label(String name) {
        this(name, LabelType.DEFAULT);
    }

    public Label(LabelType type) {
        this(generateUUID(), type);
    }

    private static String generateUUID() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        if (!Character.isLetter(uuid.charAt(0))) {
            char randomLetter = (char) ('a' + Math.random() * ('z' - 'a' + 1));
            uuid = randomLetter + uuid.substring(1);
        }
        return uuid;
    }
}
