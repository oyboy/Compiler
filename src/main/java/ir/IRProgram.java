package ir;

import java.util.LinkedHashMap;
import java.util.Map;

public class IRProgram {
    public final Map<String, IRFunction> functions = new LinkedHashMap<>();

    public void addFunction(IRFunction function) {
        functions.put(function.name, function);
    }

    public IRFunction getFunction(String name) {
        return functions.get(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== IR Program ===\n\n");
        for (IRFunction func : functions.values()) {
            sb.append(func.toString()).append("\n");
        }
        return sb.toString();
    }
}
