package codegen;

import ir.*;
import java.util.*;

public class X86Generator {
    private final StringBuilder asm = new StringBuilder();
    private StackFrame frame;
    private IRFunction currentFunc;
    private final Map<String, String> stringPool = new HashMap<>();
    private final Map<String, String> floatPool = new HashMap<>();
    private final Set<String> floatTemps = new HashSet<>();
    private int floatCount = 0, xmmUsed = 0;

    public String generate(IRProgram p) {
        for (IRFunction f : p.functions.values()) collectConstants(f);
        asm.append("bits 64\nsection .data\n");
        stringPool.forEach((l, v) -> asm.append(l).append(" db \"").append(v.replace("\\n", "\", 10, \"")).append("\", 0\n"));
        floatPool.forEach((v, l) -> asm.append(l).append(" dq ").append(v).append("\n"));
        asm.append("\nsection .text\nglobal main\nextern exit, printf, malloc, free, scanf, puts, getchar, pow, sqrt, sin, cos, strlen\nextern print_int, print_bool, print_string, print_float\n\n");
        asm.append("_start:\n    call main\n    mov rdi, rax\n    call exit\n\n");
        for (IRFunction f : p.functions.values()) generateFunction(f);
        return asm.toString();
    }

    private void collectConstants(IRFunction f) {
        for (BasicBlock b : f.blocks) {
            for (Instruction i : b.instructions) {
                if (i instanceof Instruction.Param p) {
                    if (p.value instanceof Operand.FloatLiteral fl) floatPool.putIfAbsent(fl.toString(), "L_flt_" + (floatCount++));
                    if (p.value instanceof Operand.StringLiteral s) stringPool.put(s.label, s.value);
                }
            }
        }
    }

    private void generateFunction(IRFunction f) {
        currentFunc = f; frame = new StackFrame(); floatTemps.clear(); preprocess(f);
        asm.append(f.name).append(":\n    push rbp\n    mov rbp, rsp\n");
        int sz = frame.getPaddedSize(); if (sz > 0) asm.append("    sub rsp, ").append(sz).append("\n");
        for (int i = 0; i < Math.min(f.paramNames.size(), 6); i++) {
            String p = f.paramNames.get(i).split(" ")[1];
            asm.append("    mov qword ").append(frame.getAddress(new Operand.Parameter(p))).append(", ").append(ABI.ARG_REGISTERS[i]).append("\n");
        }
        for (BasicBlock b : f.blocks) {
            asm.append(".").append(f.name).append("_").append(b.label).append(":\n");
            for (Instruction i : b.instructions) translate(i);
        }
        asm.append("\n");
    }

    private void preprocess(IRFunction f) {
        for (String v : f.variables.keySet()) {
            frame.allocate(v);
        }
        for (BasicBlock b : f.blocks) {
            for (Instruction i : b.instructions) {
                allocateAll(i);
                if (i instanceof Instruction.Call c && c.dest != null && isMathFunction(c.funcName)) floatTemps.add(c.dest.toString());
            }
        }
    }

    private void allocateAll(Instruction i) {
        if (i instanceof Instruction.BinaryOp op) { frame.allocate(op.dest.toString()); frame.allocate(op.src1.toString()); frame.allocate(op.src2.toString()); }
        else if (i instanceof Instruction.Compare op) { frame.allocate(op.dest.toString()); frame.allocate(op.src1.toString()); frame.allocate(op.src2.toString()); }
        else if (i instanceof Instruction.UnaryOp op) { frame.allocate(op.dest.toString()); frame.allocate(op.src.toString()); }
        else if (i instanceof Instruction.Move op) { frame.allocate(op.dest.toString()); frame.allocate(op.src.toString()); }
        else if (i instanceof Instruction.Load op) { frame.allocate(op.dest.toString()); frame.allocate(op.addr.toString()); }
        else if (i instanceof Instruction.Store op) { frame.allocate(op.addr.toString()); frame.allocate(op.src.toString()); }
        else if (i instanceof Instruction.LoadIndex op) { frame.allocate(op.dest.toString()); frame.allocate(op.index.toString()); }
        else if (i instanceof Instruction.StoreIndex op) { frame.allocate(op.index.toString()); frame.allocate(op.src.toString()); }
        else if (i instanceof Instruction.Call op && op.dest != null) frame.allocate(op.dest.toString());
        else if (i instanceof Instruction.Param op && !(op.value instanceof Operand.FloatLiteral || op.value instanceof Operand.StringLiteral)) frame.allocate(op.value.toString());
        else if (i instanceof Instruction.Return op && op.value != null) frame.allocate(op.value.toString());
        else if (i instanceof Instruction.JumpIf op) frame.allocate(op.condition.toString());
        else if (i instanceof Instruction.LoadIndexPtr op) {
            frame.allocate(op.dest.toString());
            frame.allocate(op.ptr.toString());
            frame.allocate(op.index.toString());
        }
        else if (i instanceof Instruction.StoreIndexPtr op) {
            frame.allocate(op.ptr.toString());
            frame.allocate(op.index.toString());
            frame.allocate(op.src.toString());
        }
    }

    private void translate(Instruction i) {
        if (i instanceof Instruction.BinaryOp) handleBinary((Instruction.BinaryOp) i);
        else if (i instanceof Instruction.Compare) handleCompare((Instruction.Compare) i);
        else if (i instanceof Instruction.UnaryOp) handleUnary((Instruction.UnaryOp) i);
        else if (i instanceof Instruction.Move) handleMove((Instruction.Move) i);
        else if (i instanceof Instruction.Store) handleStore((Instruction.Store) i);
        else if (i instanceof Instruction.Load) handleLoad((Instruction.Load) i);
        else if (i instanceof Instruction.LoadIndex) handleLoadIndex((Instruction.LoadIndex) i);
        else if (i instanceof Instruction.StoreIndex) handleStoreIndex((Instruction.StoreIndex) i);
        else if (i instanceof Instruction.Jump) asm.append("    jmp .").append(currentFunc.name).append("_").append(((Instruction.Jump) i).label).append("\n");
        else if (i instanceof Instruction.JumpIf) handleJumpIf((Instruction.JumpIf) i);
        else if (i instanceof Instruction.Param) handleParam((Instruction.Param) i);
        else if (i instanceof Instruction.Call) handleCall((Instruction.Call) i);
        else if (i instanceof Instruction.Return) handleReturn((Instruction.Return) i);
        else if (i instanceof Instruction.LoadIndexPtr)  handleLoadIndexPtr((Instruction.LoadIndexPtr) i);
        else if (i instanceof Instruction.StoreIndexPtr) handleStoreIndexPtr((Instruction.StoreIndexPtr) i);
    }

    private void handleBinary(Instruction.BinaryOp b) {
        asm.append("    mov rax, ").append(frame.getAddress(b.src1)).append("\n    mov r10, ").append(frame.getAddress(b.src2)).append("\n");
        switch (b.op) { case ADD->asm.append("    add rax, r10\n"); case SUB->asm.append("    sub rax, r10\n"); case MUL->asm.append("    imul rax, r10\n"); case DIV->asm.append("    cqo\n    idiv r10\n"); case MOD->asm.append("    cqo\n    idiv r10\n    mov rax, rdx\n"); }
        asm.append("    mov ").append(frame.getAddress(b.dest)).append(", rax\n");
    }

    private void handleCompare(Instruction.Compare c) {
        asm.append("    mov rax, ").append(frame.getAddress(c.src1)).append("\n    cmp rax, ").append(frame.getAddress(c.src2)).append("\n");
        String s = switch (c.op) { case EQ->"setz"; case NE->"setnz"; case LT->"setl"; case LE->"setle"; case GT->"setg"; case GE->"setge"; };
        asm.append("    ").append(s).append(" al\n    movzx rax, al\n    mov ").append(frame.getAddress(c.dest)).append(", rax\n");
    }

    private void handleUnary(Instruction.UnaryOp u) {
        asm.append("    mov rax, ").append(frame.getAddress(u.src)).append("\n");
        if (u.op == Instruction.UnaryOp.Op.NEG) asm.append("    neg rax\n"); else asm.append("    not rax\n");
        asm.append("    mov ").append(frame.getAddress(u.dest)).append(", rax\n");
    }

    private void handleMove(Instruction.Move m) {
        if (m.src instanceof Operand.IntLiteral || m.src instanceof Operand.BoolLiteral) asm.append("    mov qword ").append(frame.getAddress(m.dest)).append(", ").append(frame.getAddress(m.src)).append("\n");
        else asm.append("    mov rax, ").append(frame.getAddress(m.src)).append("\n    mov ").append(frame.getAddress(m.dest)).append(", rax\n");
    }

    private void handleStore(Instruction.Store s) {
        if (s.src instanceof Operand.IntLiteral || s.src instanceof Operand.BoolLiteral) asm.append("    mov qword ").append(frame.getAddress(s.addr)).append(", ").append(frame.getAddress(s.src)).append("\n");
        else asm.append("    mov rax, ").append(frame.getAddress(s.src)).append("\n    mov ").append(frame.getAddress(s.addr)).append(", rax\n");
    }

    private void handleLoad(Instruction.Load l) {
        asm.append("    mov rax, ").append(frame.getAddress(l.addr)).append("\n    mov ").append(frame.getAddress(l.dest)).append(", rax\n");
    }

/*    private void handleLoadIndex(Instruction.LoadIndex li) {
        asm.append("    mov r11, ").append(frame.getAddress(li.index)).append("\n    shl r11, 3\n    mov r10, rbp\n    sub r10, ").append(frame.getArrayOffset(li.arrayName)).append("\n    mov rax, [r10 + r11]\n    mov ").append(frame.getAddress(li.dest)).append(", rax\n");
    }

    private void handleStoreIndex(Instruction.StoreIndex si) {
        asm.append("    mov r11, ").append(frame.getAddress(si.index)).append("\n    shl r11, 3\n    mov r10, rbp\n    sub r10, ").append(frame.getArrayOffset(si.arrayName)).append("\n    mov rax, ").append(frame.getAddress(si.src)).append("\n    mov [r10 + r11], rax\n");
    }*/

    private void handleJumpIf(Instruction.JumpIf ji) {
        asm.append("    mov rax, ").append(frame.getAddress(ji.condition)).append("\n    test rax, rax\n    jnz .").append(currentFunc.name).append("_").append(ji.label).append("\n");
    }

    private void handleParam(Instruction.Param p) {
        String r = ABI.ARG_REGISTERS[p.index];
        boolean toMath = isMathFunction(p.funcName) || p.funcName.equals("print_float");
        boolean toPrintf = p.funcName.equals("printf") && p.index > 0;

        if (p.value instanceof Operand.StringLiteral) {
            asm.append("    lea ").append(r).append(", [").append(p.value).append("]\n");
        } else if (p.value instanceof Operand.FloatLiteral || floatTemps.contains(p.value.toString())) {
            String x = "xmm" + (toPrintf ? xmmUsed++ : p.index);
            String addr = (p.value instanceof Operand.FloatLiteral) ? "[" + floatPool.get(p.value.toString()) + "]" : frame.getAddress(p.value);
            asm.append("    movsd ").append(x).append(", ").append(addr).append("\n");
        } else {
            if (p.value instanceof Operand.Variable && isLocalArrayVar(p.value.toString())) {
                asm.append("    lea ").append(r).append(", ").append(frame.getAddress(p.value)).append("\n");
            } else if (toMath) {
                asm.append("    cvtsi2sd xmm").append(p.index).append(", ").append(frame.getAddress(p.value)).append("\n");
            } else if (p.funcName.equals("scanf") && p.index > 0) {
                asm.append("    lea ").append(r).append(", ").append(frame.getAddress(p.value)).append("\n");
            } else {
                asm.append("    mov ").append(r).append(", ").append(frame.getAddress(p.value)).append("\n");
            }
        }
    }

    private void handleLoadIndex(Instruction.LoadIndex li) {
        asm.append("    mov r11, ").append(frame.getAddress(li.index)).append("\n    shl r11, 3\n");
        if (isParameter(li.arrayName)) {
            asm.append("    mov r10, ").append(frame.getAddress(new Operand.Parameter(li.arrayName))).append("\n");
        } else {
            asm.append("    mov r10, rbp\n    sub r10, ").append(frame.getArrayOffset(li.arrayName)).append("\n");
        }
        asm.append("    mov rax, [r10 + r11]\n    mov ").append(frame.getAddress(li.dest)).append(", rax\n");
    }

    private void handleStoreIndex(Instruction.StoreIndex si) {
        asm.append("    mov r11, ").append(frame.getAddress(si.index)).append("\n    shl r11, 3\n");
        if (isParameter(si.arrayName)) {
            asm.append("    mov r10, ").append(frame.getAddress(new Operand.Parameter(si.arrayName))).append("\n");
        } else {
            asm.append("    mov r10, rbp\n    sub r10, ").append(frame.getArrayOffset(si.arrayName)).append("\n");
        }
        asm.append("    mov rax, ").append(frame.getAddress(si.src)).append("\n    mov [r10 + r11], rax\n");
    }

    private boolean isLocalArrayVar(String name) {
        String clean = name.replace("[", "").replace("]", "");
        return currentFunc.variables.keySet().stream().anyMatch(k -> k.startsWith(clean + "$size$"));
    }

    private boolean isParameter(String name) {
        for (String p : currentFunc.paramNames) {
            String pName = p.split(" ")[1];
            if (pName.contains("[")) pName = pName.substring(0, pName.indexOf("["));
            if (pName.equals(name)) return true;
        }
        return false;
    }

    private void handleCall(Instruction.Call c) {
        if (c.funcName.equals("malloc")) {
            asm.append("    xor rax, rax\n");
            asm.append("    call malloc\n");
            if (c.dest != null) {
                asm.append("    mov ")
                        .append(frame.getAddress(c.dest))
                        .append(", rax\n");
            }
            return;
        }
        if (c.funcName.equals("free")) {
            asm.append("    call free\n");
            return;
        }
        if (c.funcName.equals("printf")) asm.append("    mov rax, ").append(xmmUsed).append("\n");
        else if (c.funcName.equals("scanf")) asm.append("    xor rax, rax\n");
        asm.append("    call ").append(c.funcName).append("\n");
        if (c.dest != null) {
            if (isMathFunction(c.funcName)) asm.append("    movsd ").append(frame.getAddress(c.dest)).append(", xmm0\n");
            else asm.append("    mov ").append(frame.getAddress(c.dest)).append(", rax\n");
        }
        xmmUsed = 0;
    }

    private void handleReturn(Instruction.Return r) {
        if (r.value != null) {
            if (r.value instanceof Operand.FloatLiteral) asm.append("    movsd xmm0, [").append(floatPool.get(r.value.toString())).append("]\n");
            else if (floatTemps.contains(r.value.toString())) asm.append("    movsd xmm0, ").append(frame.getAddress(r.value)).append("\n");
            else asm.append("    mov rax, ").append(frame.getAddress(r.value)).append("\n");
        }
        asm.append("    leave\n    ret\n");
    }

    private boolean isMathFunction(String n) { return List.of("pow", "sqrt", "sin", "cos").contains(n); }

    private void handleLoadIndexPtr(Instruction.LoadIndexPtr li) {
        asm.append("    mov r10, ").append(frame.getAddress(li.ptr)).append("\n");
        asm.append("    mov r11, ").append(frame.getAddress(li.index)).append("\n");
        asm.append("    shl r11, 3\n");
        asm.append("    add r10, r11\n");
        asm.append("    mov rax, [r10]\n");
        asm.append("    mov ").append(frame.getAddress(li.dest)).append(", rax\n");
    }

    private void handleStoreIndexPtr(Instruction.StoreIndexPtr si) {
        asm.append("    mov r10, ").append(frame.getAddress(si.ptr)).append("\n");
        asm.append("    mov r11, ").append(frame.getAddress(si.index)).append("\n");
        asm.append("    shl r11, 3\n");
        asm.append("    add r10, r11\n");
        asm.append("    mov rax, ").append(frame.getAddress(si.src)).append("\n");
        asm.append("    mov [r10], rax\n");
    }
}