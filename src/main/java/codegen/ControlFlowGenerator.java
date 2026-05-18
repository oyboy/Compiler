package codegen;

import ir.*;

public class ControlFlowGenerator {
    private final X86Generator base;
    private final LabelManager labels;

    public ControlFlowGenerator(X86Generator base, LabelManager labels) {
        this.base = base;
        this.labels = labels;
    }

    public void generateIf(Instruction.JumpIf jif, String elseLabel) {
        String addr = base.getFrame().getAddress(jif.condition);
        base.appendAsm("    mov rax, " + addr);
        base.appendAsm("    test rax, rax");
        base.appendAsm("    jnz " + base.getLabel(jif.label));
        base.appendAsm("    jmp " + base.getLabel(elseLabel));
    }

    public void generateJump(String label) {
        base.appendAsm("    jmp " + base.getLabel(label));
    }
}
