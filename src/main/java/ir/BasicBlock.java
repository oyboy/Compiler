package ir;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    public final String label;
    public final List<Instruction> instructions = new ArrayList<>();

    public final List<BasicBlock> successors   = new ArrayList<>();
    public final List<BasicBlock> predecessors = new ArrayList<>();

    public BasicBlock(String label) {
        this.label = label;
    }

    public void addInstruction(Instruction instr) {
        instructions.add(instr);
    }

    public void addSuccessor(BasicBlock block) {
        if (!successors.contains(block)) {
            successors.add(block);
            block.predecessors.add(this);
        }
    }

    public boolean isTerminated() {
        if (instructions.isEmpty()) return false;
        Instruction last = instructions.get(instructions.size() - 1);
        return last instanceof Instruction.Jump
                || last instanceof Instruction.JumpIf
                || last instanceof Instruction.JumpIfNot
                || last instanceof Instruction.Return;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(":\n");
        for (Instruction instr : instructions) {
            sb.append("    ").append(instr).append("\n");
        }
        return sb.toString();
    }
}