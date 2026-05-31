package ir;

public abstract class Instruction {
    public String comment;

    public Instruction withComment(String comment) {
        this.comment = comment;
        return this;
    }

    protected String commentStr() {
        return comment != null ? "  # " + comment : "";
    }

    public static class BinaryOp extends Instruction {
        public enum Op { ADD, SUB, MUL, DIV, MOD, AND, OR, XOR }
        public final Operand dest;
        public final Op op;
        public final Operand src1;
        public final Operand src2;

        public BinaryOp(Operand dest, Op op, Operand src1, Operand src2) {
            this.dest = dest;
            this.op = op;
            this.src1 = src1;
            this.src2 = src2;
        }

        @Override
        public String toString() {
            return dest + " = " + op + " " + src1 + ", " + src2 + commentStr();
        }
    }

    public static class UnaryOp extends Instruction {
        public enum Op { NEG, NOT }
        public final Operand dest;
        public final Op op;
        public final Operand src;

        public UnaryOp(Operand dest, Op op, Operand src) {
            this.dest = dest;
            this.op = op;
            this.src = src;
        }

        @Override
        public String toString() {
            return dest + " = " + op + " " + src + commentStr();
        }
    }

    public static class Compare extends Instruction {
        public enum Op { EQ, NE, LT, LE, GT, GE }
        public final Operand dest;
        public final Op op;
        public final Operand src1;
        public final Operand src2;

        public Compare(Operand dest, Op op, Operand src1, Operand src2) {
            this.dest = dest;
            this.op = op;
            this.src1 = src1;
            this.src2 = src2;
        }

        @Override
        public String toString() {
            return dest + " = CMP_" + op + " " + src1 + ", " + src2 + commentStr();
        }
    }

    public static class Load extends Instruction {
        public final Operand dest;
        public final Operand addr;

        public Load(Operand dest, Operand addr) {
            this.dest = dest;
            this.addr = addr;
        }

        @Override
        public String toString() {
            return dest + " = LOAD " + addr + commentStr();
        }
    }

    public static class Store extends Instruction {
        public final Operand addr;
        public final Operand src;

        public Store(Operand addr, Operand src) {
            this.addr = addr;
            this.src = src;
        }

        @Override
        public String toString() {
            return "STORE " + addr + ", " + src + commentStr();
        }
    }

    public static class Move extends Instruction {
        public final Operand dest;
        public final Operand src;

        public Move(Operand dest, Operand src) {
            this.dest = dest;
            this.src = src;
        }

        @Override
        public String toString() {
            return dest + " = MOVE " + src + commentStr();
        }
    }

    public static class Jump extends Instruction {
        public final String label;

        public Jump(String label) { this.label = label; }

        @Override
        public String toString() {
            return "JUMP " + label + commentStr();
        }
    }

    public static class JumpIf extends Instruction {
        public final Operand condition;
        public final String label;

        public JumpIf(Operand condition, String label) {
            this.condition = condition;
            this.label = label;
        }

        @Override
        public String toString() {
            return "JUMP_IF " + condition + ", " + label + commentStr();
        }
    }

    public static class JumpIfNot extends Instruction {
        public final Operand condition;
        public final String label;

        public JumpIfNot(Operand condition, String label) {
            this.condition = condition;
            this.label = label;
        }

        @Override
        public String toString() {
            return "JUMP_IF_NOT " + condition + ", " + label + commentStr();
        }
    }

    public static class Param extends Instruction {
        public final int index;
        public final Operand value;
        public final String funcName;

        public Param(int index, Operand value, String funcName) {
            this.index = index;
            this.value = value;
            this.funcName = funcName;
        }

        @Override
        public String toString() {
            return "PARAM " + index + ", " + value + commentStr();
        }
    }

    public static class Call extends Instruction {
        public final Operand dest;
        public final String funcName;
        public final int argCount;

        public Call(Operand dest, String funcName, int argCount) {
            this.dest = dest;
            this.funcName = funcName;
            this.argCount = argCount;
        }

        @Override
        public String toString() {
            if (dest != null) {
                return dest + " = CALL " + funcName + ", " + argCount + commentStr();
            }
            return "CALL " + funcName + ", " + argCount + commentStr();
        }
    }

    public static class Return extends Instruction {
        public final Operand value;

        public Return(Operand value) { this.value = value; }

        @Override
        public String toString() {
            return value != null ? "RETURN " + value + commentStr() : "RETURN" + commentStr();
        }
    }

    public static class LoadIndex extends Instruction {
        public final Operand dest;
        public final String arrayName;
        public final Operand index;

        public LoadIndex(Operand dest, String arrayName, Operand index) {
            this.dest = dest;
            this.arrayName = arrayName;
            this.index = index;
        }

        @Override
        public String toString() {
            return dest + " = LOAD_INDEX " + arrayName + ", " + index + commentStr();
        }
    }

    public static class StoreIndex extends Instruction {
        public final String arrayName;
        public final Operand index;
        public final Operand src;

        public StoreIndex(String arrayName, Operand index, Operand src) {
            this.arrayName = arrayName;
            this.index = index;
            this.src = src;
        }

        @Override
        public String toString() {
            return "STORE_INDEX " + arrayName + ", " + index + ", " + src + commentStr();
        }
    }

    public static class LoadIndexPtr extends Instruction {
        public final Operand dest;
        public final Operand ptr;
        public final Operand index;

        public LoadIndexPtr(Operand dest, Operand ptr, Operand index) {
            this.dest  = dest;
            this.ptr   = ptr;
            this.index = index;
        }
    }

    public static class StoreIndexPtr extends Instruction {
        public final Operand ptr;
        public final Operand index;
        public final Operand src;

        public StoreIndexPtr(Operand ptr, Operand index, Operand src) {
            this.ptr   = ptr;
            this.index = index;
            this.src   = src;
        }
    }
}