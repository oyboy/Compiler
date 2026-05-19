package semantic;

public class SemanticError {
    public enum ErrorType {
        UNDECLARED_IDENTIFIER,
        DUPLICATE_DECLARATION,
        TYPE_MISMATCH,
        ARGUMENT_COUNT_MISMATCH,
        ARGUMENT_TYPE_MISMATCH,
        INVALID_RETURN_TYPE,
        INVALID_CONDITION_TYPE,
        USE_BEFORE_DECLARATION,
        INVALID_ASSIGNMENT_TARGET,
        NOT_A_FUNCTION,
        NOT_A_VARIABLE,
        VOID_RETURN_VALUE,
        MISSING_RETURN,
        INVALID_BREAK,
        INVALID_CONTINUE
    }

    public final ErrorType errorType;
    public final String message;
    public final String context;
    public final String filename;
    public final int line;
    public final int column;

    public SemanticError(ErrorType errorType, String message, String filename, String context, int line, int column) {
        this.errorType = errorType;
        this.message = message;
        this.filename = filename;
        this.context = context;
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("semantic error: ").append(message).append("\n");

        if (filename != null && !filename.isEmpty()) {
            sb.append("  --> ").append(filename).append(":").append(line).append(":").append(column).append("\n");
        } else {
            sb.append("  --> ").append(line).append(":").append(column).append("\n");
        }

        if (context != null && !context.isEmpty()) {
            sb.append("  context: ").append(context).append("\n");
        }
        return sb.toString();
    }
}
