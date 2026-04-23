package ir;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IRFunction {
    public final String name;
    public final String returnType;
    public final List<String> paramNames = new ArrayList<>();

    public final List<BasicBlock> blocks = new ArrayList<>();

    public final Map<String, Operand.Variable> variables = new LinkedHashMap<>();

    public IRFunction(String name, String returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public void addBlock(BasicBlock block) {
        blocks.add(block);
    }

    public BasicBlock getEntry() {
        return blocks.isEmpty() ? null : blocks.get(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("function ").append(name).append(": ")
                .append(returnType).append(" (")
                .append(String.join(", ", paramNames)).append(")\n");

        for (BasicBlock block : blocks) {
            sb.append(block.toString());
        }
        return sb.toString();
    }
}