package parser;

import lexer.Scanner;
import lexer.Token;
import lexer.TokenType;
import parser.ast.ProgramNode;
import tests.BaseTestRunner;
import utils.ASTPrettyPrinter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ParserTestRunner extends BaseTestRunner {

    @Override
    protected String getSuiteName() {
        return "Parser Test Suite";
    }

    @Override
    protected String getExpectedExtension() {
        return ".expected";
    }

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
        List<String> errors = parser.getErrors();

        if (isErrorTest) {
            StringBuilder sb = new StringBuilder();
            for (String err : errors) {
                sb.append(err).append("\n");
            }
            return sb.toString();
        }

        if (!errors.isEmpty()) {
            System.err.println("FAIL (unexpected parse errors)");
            errors.forEach(e -> System.out.println("    " + e));
            return null;
        }

        return new ASTPrettyPrinter().print(program);
    }

    public static void main(String[] args) {
        new ParserTestRunner().run(
                "src/test/java/parser/valid/expressions",
                "src/test/java/parser/valid/statements",
                "src/test/java/parser/valid/declarations",
                "src/test/java/parser/valid/full_programs",
                "src/test/java/parser/invalid/syntax_errors"
        );
    }
}