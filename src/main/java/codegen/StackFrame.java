package codegen;

import ir.Operand;
import java.util.HashMap;
import java.util.Map;

public class StackFrame {
    private final Map<String, Integer> offsets = new HashMap<>();
    private int currentOffset = 0;

    private String cleanKey(String key) {
        if (key.startsWith("[") && key.endsWith("]")) {
            return key.substring(1, key.length() - 1);
        }
        return key;
    }

    public void allocate(String identifier) {
        String key = cleanKey(identifier);
        if (key.matches("-?\\d+") || key.equals("true") || key.equals("false")) return;
        if (!offsets.containsKey(key)) {
            currentOffset += 8;
            offsets.put(key, currentOffset);
        }
    }

    public void allocateArray(String name, int size) {
        String key = cleanKey(name);
        if (!offsets.containsKey(key)) {
            currentOffset += (size * 8);
            offsets.put(key, currentOffset);
        }
    }

    public String getAddress(Operand op) {
        if (op instanceof Operand.IntLiteral) return String.valueOf(((Operand.IntLiteral) op).value);
        if (op instanceof Operand.BoolLiteral) return ((Operand.BoolLiteral) op).value ? "1" : "0";
        if (op instanceof Operand.Parameter) {
            return "[rbp - " + offsets.get(cleanKey(((Operand.Parameter) op).name)) + "]";
        }

        String key = cleanKey(op.toString());
        Integer offset = offsets.get(key);
        if (offset == null) {
            throw new RuntimeException("Missing stack allocation for: " + key);
        }
        return "[rbp - " + offset + "]";
    }

    public int getArrayOffset(String name) {
        Integer offset = offsets.get(cleanKey(name));
        return (offset != null) ? offset : 0;
    }

    public int getPaddedSize() {
        return (currentOffset + 15) & ~15;
    }
}