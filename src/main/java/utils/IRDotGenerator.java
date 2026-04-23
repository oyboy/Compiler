package utils;

import ir.*;
import java.util.stream.Collectors;

public class IRDotGenerator {
    public String generate(IRProgram program) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph CFG {\n");
        sb.append("  rankdir=TB;\n");
        sb.append("  node [shape=record, fontname=\"Courier\", fontsize=10];\n\n");

        for (IRFunction func : program.functions.values()) {
            sb.append(generateFunction(func));
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String generateFunction(IRFunction func) {
        StringBuilder sb = new StringBuilder();
        sb.append("  // Function: ").append(func.name).append("\n");
        sb.append("  subgraph cluster_").append(func.name).append(" {\n");
        sb.append("    label=\"").append(func.name).append("\";\n");
        sb.append("    style=dashed;\n\n");

        for (BasicBlock block : func.blocks) {
            String nodeId = func.name + "_" + block.label;
            String color  = getBlockColor(block, func);

            String instrs = block.instructions.stream()
                    .map(i -> escape(i.toString()))
                    .collect(Collectors.joining("\\l"));

            sb.append("    ").append(nodeId)
                    .append(" [label=\"{").append(block.label).append(":|")
                    .append(instrs).append("\\l}\", ")
                    .append("style=filled, fillcolor=\"").append(color).append("\"];\n");
        }

        sb.append("\n");
        for (BasicBlock block : func.blocks) {
            String fromId = func.name + "_" + block.label;
            for (BasicBlock succ : block.successors) {
                String toId = func.name + "_" + succ.label;
                sb.append("    ").append(fromId)
                        .append(" -> ").append(toId).append(";\n");
            }
        }

        sb.append("  }\n\n");
        return sb.toString();
    }

    private String getBlockColor(BasicBlock block, IRFunction func) {
        if (block == func.getEntry()) return "lightblue";
        if (!block.successors.isEmpty()) return "lightyellow";
        return "lightgreen";
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"")
                .replace("<", "\\<")
                .replace(">", "\\>")
                .replace("{", "\\{")
                .replace("}", "\\}");
    }
}