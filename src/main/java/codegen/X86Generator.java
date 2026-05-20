package codegen;

import ir.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class X86Generator {
    private final StringBuilder asm = new StringBuilder();
    private StackFrame currentFrame;
    private IRFunction currentFunc;
    private final Map<String, String> stringPool = new HashMap<>();

    public StackFrame getFrame() { return currentFrame; }

    public String generate(IRProgram p) {
        for (IRFunction f : p.functions.values()) {
            collectStrings(f);
        }

        asm.append("bits 64\n");
        asm.append("section .data\n");
        stringPool.forEach((label, val) -> {
            String escaped = val.replace("\\n", "\", 10, \"")
                    .replace("\\t", "\", 9, \"")
                    .replace("\\r", "\", 13, \"");
            asm.append(label).append(" db \"").append(escaped).append("\", 0\n");
        });

        asm.append("section .text\n");
        asm.append("global main\n");
        asm.append("extern exit, printf, malloc, free, scanf, sqrt\n");
        asm.append("extern print_int, print_bool, print_string, print_float\n\n");

        asm.append("_start:\n");
        asm.append("    call main\n");
        asm.append("    mov rdi, rax\n");
        asm.append("    call exit\n\n");

        for (IRFunction f : p.functions.values()) {
            generateFunction(f);
        }
        return asm.toString();
    }

    private void collectStrings(IRFunction f) {
        for (BasicBlock b : f.blocks) {
            for (Instruction i : b.instructions) {
                if (i instanceof Instruction.Param p && p.value instanceof Operand.StringLiteral str) {
                    stringPool.put(str.label, str.value);
                }
            }
        }
    }

    private void generateFunction(IRFunction func) {
        this.currentFunc = func;
        this.currentFrame = new StackFrame();

        preprocessStack(func);

        asm.append(func.name).append(":\n");
        asm.append("    push rbp\n");
        asm.append("    mov rbp, rsp\n");
        int size = currentFrame.getPaddedSize();
        if (size > 0) asm.append("    sub rsp, ").append(size).append("\n");

        for (int i = 0; i < Math.min(func.paramNames.size(), 6); i++) {
            String pName = func.paramNames.get(i).split(" ")[1];
            asm.append("    mov qword ").append(currentFrame.getAddress(new Operand.Parameter(pName)))
                    .append(", ").append(ABI.ARG_REGISTERS[i]).append("\n");
        }

        for (BasicBlock block : func.blocks) {
            asm.append(".").append(func.name).append("_").append(block.label).append(":\n");
            for (Instruction instr : block.instructions) {
                translate(instr);
            }
        }
        asm.append("\n");
    }

    private void preprocessStack(IRFunction func) {
        for (String v : func.variables.keySet()) {
            if (v.contains("$size$")) {
                String[] parts = v.split("\\$size\\$");
                String name = parts[0];
                int size = Integer.parseInt(parts[1]);
                currentFrame.allocateArray(name, size);
            } else {
                currentFrame.allocate(v);
            }
        }

        for (BasicBlock b : func.blocks) {
            for (Instruction i : b.instructions) {
                if (i instanceof Instruction.BinaryOp op) currentFrame.allocate(op.dest.toString());
                else if (i instanceof Instruction.Compare op) currentFrame.allocate(op.dest.toString());
                else if (i instanceof Instruction.UnaryOp op) currentFrame.allocate(op.dest.toString());
                else if (i instanceof Instruction.Load op) currentFrame.allocate(op.dest.toString());
                else if (i instanceof Instruction.Move op) currentFrame.allocate(op.dest.toString());
                else if (i instanceof Instruction.LoadIndex op) currentFrame.allocate(op.dest.toString());
                else if (i instanceof Instruction.Call op && op.dest != null) currentFrame.allocate(op.dest.toString());
            }
        }
    }

    private void translate(Instruction instr) {
        if (instr instanceof Instruction.BinaryOp) {
            handleBinary((Instruction.BinaryOp) instr);
        } else if (instr instanceof Instruction.UnaryOp) {
            Instruction.UnaryOp u = (Instruction.UnaryOp) instr;
            asm.append("    mov rax, ").append(currentFrame.getAddress(u.src)).append("\n");
            if (u.op == Instruction.UnaryOp.Op.NEG) asm.append("    neg rax\n");
            else asm.append("    not rax\n");
            asm.append("    mov ").append(currentFrame.getAddress(u.dest)).append(", rax\n");
        } else if (instr instanceof Instruction.Compare) {
            handleCompare((Instruction.Compare) instr);
        } else if (instr instanceof Instruction.Move) {
            Instruction.Move m = (Instruction.Move) instr;
            if (m.src instanceof Operand.IntLiteral || m.src instanceof Operand.BoolLiteral) {
                asm.append("    mov qword ").append(currentFrame.getAddress(m.dest))
                        .append(", ").append(currentFrame.getAddress(m.src)).append("\n");
            } else {
                asm.append("    mov rax, ").append(currentFrame.getAddress(m.src)).append("\n");
                asm.append("    mov ").append(currentFrame.getAddress(m.dest)).append(", rax\n");
            }
        } else if (instr instanceof Instruction.Store) {
            Instruction.Store s = (Instruction.Store) instr;
            if (s.src instanceof Operand.IntLiteral || s.src instanceof Operand.BoolLiteral) {
                asm.append("    mov qword ").append(currentFrame.getAddress(s.addr))
                        .append(", ").append(currentFrame.getAddress(s.src)).append("\n");
            } else {
                asm.append("    mov rax, ").append(currentFrame.getAddress(s.src)).append("\n");
                asm.append("    mov ").append(currentFrame.getAddress(s.addr)).append(", rax\n");
            }
        } else if (instr instanceof Instruction.Load) {
            Instruction.Load l = (Instruction.Load) instr;
            asm.append("    mov rax, ").append(currentFrame.getAddress(l.addr)).append("\n");
            asm.append("    mov ").append(currentFrame.getAddress(l.dest)).append(", rax\n");
        } else if (instr instanceof Instruction.LoadIndex li) {
            asm.append("    mov r11, ").append(currentFrame.getAddress(li.index)).append("\n");
            asm.append("    shl r11, 3\n");
            asm.append("    mov r10, rbp\n");
            asm.append("    sub r10, ").append(currentFrame.getArrayOffset(li.arrayName)).append("\n");
            asm.append("    mov rax, [r10 + r11]\n");
            asm.append("    mov ").append(currentFrame.getAddress(li.dest)).append(", rax\n");
        } else if (instr instanceof Instruction.StoreIndex si) {
            asm.append("    mov r11, ").append(currentFrame.getAddress(si.index)).append("\n");
            asm.append("    shl r11, 3\n");
            asm.append("    mov r10, rbp\n");
            asm.append("    sub r10, ").append(currentFrame.getArrayOffset(si.arrayName)).append("\n");
            asm.append("    mov rax, ").append(currentFrame.getAddress(si.src)).append("\n");
            asm.append("    mov [r10 + r11], rax\n");
        } else if (instr instanceof Instruction.JumpIf j) {
            asm.append("    mov rax, ").append(currentFrame.getAddress(j.condition)).append("\n");
            asm.append("    test rax, rax\n");
            asm.append("    jnz .").append(currentFunc.name).append("_").append(j.label).append("\n");
        } else if (instr instanceof Instruction.Jump j) {
            asm.append("    jmp .").append(currentFunc.name).append("_").append(j.label).append("\n");
        } else if (instr instanceof Instruction.Param p) {
            String reg = ABI.ARG_REGISTERS[p.index];
            if (p.value instanceof Operand.StringLiteral) {
                asm.append("    lea ").append(reg).append(", [").append(p.value).append("]\n");
            } else {
                asm.append("    mov ").append(reg).append(", ").append(currentFrame.getAddress(p.value)).append("\n");
            }
        } else if (instr instanceof Instruction.Call c) {
            if (isExternal(c.funcName)) asm.append("    xor rax, rax\n");
            asm.append("    call ").append(c.funcName).append("\n");
            if (c.dest != null) asm.append("    mov ").append(currentFrame.getAddress(c.dest)).append(", rax\n");
        } else if (instr instanceof Instruction.Return r) {
            if (r.value != null) asm.append("    mov rax, ").append(currentFrame.getAddress(r.value)).append("\n");
            asm.append("    leave\n    ret\n");
        }
    }

    private void handleBinary(Instruction.BinaryOp b) {
        asm.append("    mov rax, ").append(currentFrame.getAddress(b.src1)).append("\n");
        asm.append("    mov r10, ").append(currentFrame.getAddress(b.src2)).append("\n");
        switch (b.op) {
            case ADD -> asm.append("    add rax, r10\n");
            case SUB -> asm.append("    sub rax, r10\n");
            case MUL -> asm.append("    imul rax, r10\n");
            case DIV -> asm.append("    cqo\n    idiv r10\n");
            case MOD -> asm.append("    cqo\n    idiv r10\n    mov rax, rdx\n");
            case AND -> asm.append("    and rax, r10\n");
            case OR  -> asm.append("    or rax, r10\n");
        }
        asm.append("    mov ").append(currentFrame.getAddress(b.dest)).append(", rax\n");
    }

    private void handleCompare(Instruction.Compare c) {
        asm.append("    mov rax, ").append(currentFrame.getAddress(c.src1)).append("\n");
        asm.append("    cmp rax, ").append(currentFrame.getAddress(c.src2)).append("\n");
        String s = switch (c.op) {
            case EQ -> "setz"; case NE -> "setnz"; case LT -> "setl";
            case LE -> "setle"; case GT -> "setg"; case GE -> "setge";
        };
        asm.append("    ").append(s).append(" al\n");
        asm.append("    movzx rax, al\n");
        asm.append("    mov ").append(currentFrame.getAddress(c.dest)).append(", rax\n");
    }

    private boolean isExternal(String name) {
        return List.of("printf", "scanf", "malloc", "free", "sqrt", "pow", "strlen").contains(name);
    }
}