package semantic;

import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;
import parser.Parser;
import parser.ast.ProgramNode;
import tests.BaseTestRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SemanticTestRunner extends BaseTestRunner {

    @Override
    protected String getSuiteName() { return "Semantic Analysis Test Suite"; }

    @Override
    protected String getExpectedExtension() { return ".expected"; }

    @Override
    protected String getActualOutput(String source, boolean isErrorTest, File srcFile) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = new ArrayList<>();
        Token t;
        do {
            t = scanner.next_token();
            tokens.add(t);
        } while (t.type != TokenType.EOF);

        Parser parser = new Parser(tokens);
        ProgramNode program = parser.parse();

        if (!parser.getErrors().isEmpty()) {
            System.err.println("FAIL (parse errors)");
            parser.getErrors().forEach(e -> System.out.println("    " + e));
            return null;
        }

        SemanticAnalyzer analyzer = new SemanticAnalyzer(srcFile.getName());
        analyzer.analyze(program);

        if (isErrorTest) {
            StringBuilder sb = new StringBuilder();
            for (SemanticError err : analyzer.getErrors()) {
                sb.append(err.toString().trim()).append("\n");
            }
            if (sb.isEmpty()) {
                System.err.println("FAIL (expected semantic errors, got none)");
                return null;
            }
            return sb.toString();
        } else {
            if (!analyzer.getErrors().isEmpty()) {
                System.err.println("FAIL (unexpected semantic errors)");
                analyzer.getErrors().forEach(e -> System.out.println("    " + e));
                return null;
            }
            return analyzer.getSymbolTable().dump();
        }
    }

    public static void main(String[] args) {
        new SemanticTestRunner().run(
                "src/test/java/semantic/valid/type_compatibility",
                "src/test/java/semantic/valid/nested_scopes",
                "src/test/java/semantic/valid/complex_programs",
                "src/test/java/semantic/invalid/undeclared_variable",
                "src/test/java/semantic/invalid/type_mismatch",
                "src/test/java/semantic/invalid/duplicate_declaration",
                "src/test/java/semantic/invalid/argument_errors",
                "src/test/java/semantic/invalid/scope_errors"
        );
    }
}
