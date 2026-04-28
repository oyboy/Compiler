package ir;

public abstract class Operand {
    public static class Temporary extends Operand {
        public final int id;
        public Temporary(int id) { this.id = id; }

        @Override public String toString() { return "t" + id; }
    }

    public static class Variable extends Operand {
        public final String name;
        public Variable(String name) { this.name = name; }

        @Override public String toString() { return "[" + name + "]"; }
    }

    public static class IntLiteral extends Operand {
        public final int value;
        public IntLiteral(int value) { this.value = value; }

        @Override public String toString() { return String.valueOf(value); }
    }

    public static class FloatLiteral extends Operand {
        public final double value;
        public FloatLiteral(double value) { this.value = value; }

        @Override public String toString() { return String.valueOf(value); }
    }

    public static class BoolLiteral extends Operand {
        public final boolean value;
        public BoolLiteral(boolean value) { this.value = value; }

        @Override public String toString() { return String.valueOf(value); }
    }

    public static class Parameter extends Operand {
        public final String name;
        public Parameter(String name) { this.name = name; }

        @Override
        public String toString() { return name; }
    }
}
