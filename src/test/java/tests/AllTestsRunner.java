package tests;

import lexer.LexerTestRunner;
import parser.ParserTestRunner;

public class AllTestsRunner {
    public static void main(String[] args) {
        System.out.println("========================================");
        LexerTestRunner.main(args);
        System.out.println("\n========================================");
        ParserTestRunner.main(args);
    }
}
