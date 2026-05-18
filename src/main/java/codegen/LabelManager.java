package codegen;

public class LabelManager {
    private int counter = 0;

    public String newLabel(String prefix) {
        return ".L" + prefix + "_" + (counter++);
    }
}
