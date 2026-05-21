package ir;

import java.util.*;

public class IROptimizer {
    private final IRProgram program;
    private boolean changed;

    public IROptimizer(IRProgram program) { this.program = program; }

    public void optimize() {
        removeUnusedFunctions();
        do {
            changed = false;
            for (IRFunction func : program.functions.values()) {
                optimizeFunction(func);
            }
        } while (changed);
    }

    private void optimizeFunction(IRFunction func) {
        constantFolding(func);
        simplifyControlFlow(func);
        removeUnreachableBlocks(func);
        deadCodeElimination(func);
    }

    private void constantFolding(IRFunction func) {
        for (BasicBlock block : func.blocks) {
            ListIterator<Instruction> it = block.instructions.listIterator();
            while (it.hasNext()) {
                Instruction instr = it.next();
                if (instr instanceof Instruction.BinaryOp op) {
                    Operand f = foldBinary(op.op, op.src1, op.src2);
                    if (f != null) { it.set(new Instruction.Move(op.dest, f)); changed = true; }
                } else if (instr instanceof Instruction.Compare op) {
                    Operand f = foldCompare(op.op, op.src1, op.src2);
                    if (f != null) { it.set(new Instruction.Move(op.dest, f)); changed = true; }
                }
            }
        }
    }

    public static Operand foldBinary(Instruction.BinaryOp.Op op, Operand l, Operand r) {
        if (l instanceof Operand.IntLiteral v1 && r instanceof Operand.IntLiteral v2) {
            return switch (op) {
                case ADD -> new Operand.IntLiteral(v1.value + v2.value);
                case SUB -> new Operand.IntLiteral(v1.value - v2.value);
                case MUL -> new Operand.IntLiteral(v1.value * v2.value);
                case DIV -> v2.value != 0 ? new Operand.IntLiteral(v1.value / v2.value) : null;
                case MOD -> v2.value != 0 ? new Operand.IntLiteral(v1.value % v2.value) : null;
                default -> null;
            };
        }
        return null;
    }

    public static Operand foldCompare(Instruction.Compare.Op op, Operand l, Operand r) {
        if (l instanceof Operand.IntLiteral v1 && r instanceof Operand.IntLiteral v2) {
            boolean res = switch (op) {
                case EQ -> v1.value == v2.value; case NE -> v1.value != v2.value;
                case LT -> v1.value < v2.value; case LE -> v1.value <= v2.value;
                case GT -> v1.value > v2.value; case GE -> v1.value >= v2.value;
            };
            return new Operand.BoolLiteral(res);
        }
        return null;
    }

    private void simplifyControlFlow(IRFunction func) {
        for (BasicBlock block : func.blocks) {
            ListIterator<Instruction> it = block.instructions.listIterator();
            while (it.hasNext()) {
                Instruction instr = it.next();
                if (instr instanceof Instruction.JumpIf ji && ji.condition instanceof Operand.BoolLiteral) {
                    if (((Operand.BoolLiteral) ji.condition).value) it.set(new Instruction.Jump(ji.label));
                    else it.remove();
                    changed = true;
                }
            }
        }
    }

    private void removeUnreachableBlocks(IRFunction func) {
        if (func.blocks.isEmpty()) return;
        Set<String> reachable = new HashSet<>();
        collect(func.blocks.get(0), reachable, func);
        int oldSize = func.blocks.size();
        func.blocks.removeIf(b -> !reachable.contains(b.label));
        if (func.blocks.size() < oldSize) changed = true;
    }

    private void collect(BasicBlock b, Set<String> visited, IRFunction func) {
        if (b == null || visited.contains(b.label)) return;
        visited.add(b.label);
        for (Instruction i : b.instructions) {
            if (i instanceof Instruction.Jump) collect(find(func, ((Instruction.Jump) i).label), visited, func);
            if (i instanceof Instruction.JumpIf) collect(find(func, ((Instruction.JumpIf) i).label), visited, func);
        }
    }

    private BasicBlock find(IRFunction f, String l) { return f.blocks.stream().filter(b -> b.label.equals(l)).findFirst().orElse(null); }

    private void deadCodeElimination(IRFunction func) {
        for (BasicBlock b : func.blocks) {
            for (int i = 0; i < b.instructions.size(); i++) {
                if (b.instructions.get(i) instanceof Instruction.Jump || b.instructions.get(i) instanceof Instruction.Return) {
                    if (i < b.instructions.size() - 1) { b.instructions.subList(i + 1, b.instructions.size()).clear(); changed = true; }
                    break;
                }
            }
        }
    }

    private void removeUnusedFunctions() {
        Set<String> live = new HashSet<>();
        live.add("main");
        int last;
        do {
            last = live.size();
            for (IRFunction f : program.functions.values()) {
                if (live.contains(f.name)) {
                    for (BasicBlock b : f.blocks) {
                        for (Instruction i : b.instructions) if (i instanceof Instruction.Call c) live.add(c.funcName);
                    }
                }
            }
        } while (live.size() > last);
        program.functions.keySet().removeIf(n -> !live.contains(n));
    }
}