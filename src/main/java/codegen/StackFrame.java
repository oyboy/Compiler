package codegen;

import ir.Operand;
import java.util.HashMap;
import java.util.Map;

public class StackFrame {
    private final Map<String, Integer> offsets = new HashMap<>();
    private int currentOffset = 0;

    public void allocate(String identifier) {
        if (!offsets.containsKey(identifier)) {
            currentOffset += 8;
            offsets.put(identifier, currentOffset);
        }
    }

    public String getAddress(Operand op) {
        if (op instanceof Operand.IntLiteral) return String.valueOf(((Operand.IntLiteral) op).value);
        if (op instanceof Operand.BoolLiteral) return ((Operand.BoolLiteral) op).value ? "1" : "0";
        if (op instanceof Operand.Parameter) {
            Integer offset = offsets.get(((Operand.Parameter) op).name);
            return "[rbp - " + offset + "]";
        }

        String key = op.toString();
        Integer offset = offsets.get(key);
        if (offset == null) {
            allocate(key);
            offset = offsets.get(key);
        }
        return "[rbp - " + offset + "]";
    }

    public int getPaddedSize() {
        return (currentOffset + ABI.STACK_ALIGNMENT - 1) & ~(ABI.STACK_ALIGNMENT - 1);
    }
}