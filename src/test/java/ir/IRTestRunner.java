package ir;

import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;
import parser.Parser;
import parser.ast.ProgramNode;
import semantic.SemanticAnalyzer;
import tests.BaseTestRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IRTestRunner extends BaseTestRunner {
    @Override
    protected String getSuiteName() { return "IR Generation Test Suite"; }

    @Override
    protected String getExpectedExtension() { return ".expected"; }

    @Override
    protected String getActualOutput(String source, boolean isErrorTest, File file) {
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

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(program);
        if (!analyzer.getErrors().isEmpty()) {
            System.err.println("FAIL (semantic errors)");
            analyzer.getErrors().forEach(e -> System.out.println("    " + e));
            return null;
        }

        IRGenerator gen = new IRGenerator();
        IRProgram irProgram = gen.generate(program);
        return irProgram.toString().trim();
    }

    public static void main(String[] args) {
        new IRTestRunner().run(
                "src/test/java/ir/expressions",
                "src/test/java/ir/control_flow",
                "src/test/java/ir/functions",
                "src/test/java/ir/integration"
        );
    }
}
