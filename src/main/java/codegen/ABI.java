package codegen;

public class ABI {
    public static final String[] ARG_REGISTERS = {"rdi", "rsi", "rdx", "rcx", "r8", "r9"};
    public static final String RETURN_REGISTER = "rax";
    public static final String[] CALLEE_SAVED = {"rbx", "r12", "r13", "r14", "r15"};
    public static final String[] SCRATCH_REGISTERS = {"rax", "r10", "r11"};
    public static final int STACK_ALIGNMENT = 16;
}
