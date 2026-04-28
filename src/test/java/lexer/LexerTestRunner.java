package lexer;

import tests.BaseTestRunner;

import java.io.File;

public class LexerTestRunner extends BaseTestRunner {

    @Override
    protected String getSuiteName() {
        return "Lexer Test Suite";
    }

    @Override
    protected String getExpectedExtension() {
        return ".txt";
    }

    @Override
    protected String getActualOutput(String source, boolean isErrorTest, File file) {
        Scanner scanner = new Scanner(source);
        StringBuilder sb = new StringBuilder();

        while (true) {
            Token t = scanner.next_token();
            sb.append(t.toString()).append("\n");
            if (t.type == TokenType.EOF) break;
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        new LexerTestRunner().run(
                "src/test/java/lexer/valid",
                "src/test/java/lexer/invalid"
        );
    }
}
