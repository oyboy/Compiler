package ir;

public class IROptimizer {
    public static Operand foldBinary(Instruction.BinaryOp.Op op, Operand left, Operand right) {
        if (op == null) return null;
        if (left instanceof Operand.IntLiteral && right instanceof Operand.IntLiteral) {
            int v1 = ((Operand.IntLiteral) left).value;
            int v2 = ((Operand.IntLiteral) right).value;
            return switch (op) {
                case ADD -> new Operand.IntLiteral(v1 + v2);
                case SUB -> new Operand.IntLiteral(v1 - v2);
                case MUL -> new Operand.IntLiteral(v1 * v2);
                case DIV -> v2 != 0 ? new Operand.IntLiteral(v1 / v2) : null;
                case MOD -> v2 != 0 ? new Operand.IntLiteral(v1 % v2) : null;
                default -> null;
            };
        }
        return null;
    }

    public static Operand foldCompare(Instruction.Compare.Op op, Operand left, Operand right) {
        if (op == null) return null;
        if (left instanceof Operand.IntLiteral && right instanceof Operand.IntLiteral) {
            int v1 = ((Operand.IntLiteral) left).value;
            int v2 = ((Operand.IntLiteral) right).value;
            boolean res = switch (op) {
                case EQ -> v1 == v2;
                case NE -> v1 != v2;
                case LT -> v1 < v2;
                case LE -> v1 <= v2;
                case GT -> v1 > v2;
                case GE -> v1 >= v2;
            };
            return new Operand.BoolLiteral(res);
        }
        return null;
    }
}
