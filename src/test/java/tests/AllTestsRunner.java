package tests;

import ir.IRTestRunner;
import lexer.LexerTestRunner;
import parser.ParserTestRunner;
import semantic.SemanticTestRunner;

public class AllTestsRunner {
    public static void main(String[] args) {
        System.out.println("========================================");
        LexerTestRunner.main(args);
        System.out.println("\n========================================");
        ParserTestRunner.main(args);
        System.out.println("\n========================================");
        SemanticTestRunner.main(args);
        System.out.println("\n========================================");
        IRTestRunner.main(args);
    }
}
