package utils;

import parser.ast.ProgramNode;
import semantic.SemanticAnalyzer;
import semantic.SemanticError;

import java.util.List;

public class ValidationReport {
    public static String generate(
            ProgramNode program,
            SemanticAnalyzer analyzer) {

        StringBuilder sb = new StringBuilder();
        List<SemanticError> errors = analyzer.getErrors();

        sb.append("=== Validation Report ===\n\n");

        sb.append("Summary:\n");
        sb.append("  Errors:   ").append(errors.size()).append("\n");
        sb.append("  Warnings: 0\n");
        sb.append("  Status:   ").append(errors.isEmpty() ? "PASSED" : "FAILED").append("\n\n");

        if (!errors.isEmpty()) {
            sb.append("Errors:\n");
            for (SemanticError err : errors) {
                sb.append(err.toString()).append("\n");
            }
        }

        sb.append(analyzer.getSymbolTable().dump()).append("\n");

        sb.append("Type Hierarchy:\n");
        sb.append("  Primitives: int, float, bool, void, string\n");
        sb.append("  Widening:   int -> float\n");
        sb.append("  Structs: (user-defined)\n\n");

        sb.append("Type-Annotated AST:\n");
        TypeAnnotatedPrinter printer = new TypeAnnotatedPrinter();
        sb.append(printer.print(program));

        return sb.toString();
    }
}