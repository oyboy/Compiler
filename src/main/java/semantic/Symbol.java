package semantic;

public class Symbol {
    public enum Kind {
        VARIABLE,
        PARAMETER,
        FUNCTION,
        STRUCT
    }

    public final String name;
    public final Type type;
    public final Kind kind;
    public final int line;
    public final int column;

    public boolean initialized;

    public Symbol(String name, Type type, Kind kind, int line, int column, boolean initialized) {
        this.name = name;
        this.type = type;
        this.kind = kind;
        this.line = line;
        this.column = column;
        this.initialized = initialized;
    }

    @Override
    public String toString() {
        return String.format("%s %s:%s (declared at %d:%d)", kind, name, type, line, column);
    }
}