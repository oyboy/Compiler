package lexer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScannerTest {
    private Token scanSingle(String source) {
        Scanner scanner = new Scanner(source);
        return scanner.next_token();
    }

    @Test
    void testIdentifiersAndKeywords() {
        Token t = scanSingle("variableName");
        assertEquals(TokenType.IDENTIFIER, t.type);
        assertEquals("variableName", t.lexeme);

        t = scanSingle("while");
        assertEquals(TokenType.KW_WHILE, t.type);
    }

    @Test
    void testNumbersEdgeCases() {
        Token t = scanSingle("2147483647");
        assertEquals(TokenType.LIT_INT, t.type);
        assertEquals(Integer.MAX_VALUE, t.literal);

        t = scanSingle("9999999999");
        assertEquals(TokenType.ERROR, t.type);

        t = scanSingle("123.456");
        assertEquals(TokenType.LIT_FLOAT, t.type);
        assertEquals(123.456, t.literal);
    }

    @Test
    void testStrings() {
        Token t = scanSingle("\"Hello World\"");
        assertEquals(TokenType.LIT_STRING, t.type);
        assertEquals("Hello World", t.literal);

        t = scanSingle("\"Unterminated");
        assertEquals(TokenType.ERROR, t.type);
    }

    @Test
    void testOperatorsComplex() {
        Scanner s = new Scanner("= == ! !=");
        assertEquals(TokenType.OP_ASSIGN, s.next_token().type);
        assertEquals(TokenType.OP_EQ, s.next_token().type);
        assertEquals(TokenType.OP_NOT, s.next_token().type);
        assertEquals(TokenType.OP_NEQ, s.next_token().type);
    }

    @Test
    void testComments() {
        Scanner s = new Scanner("// This is a comment\n");
        assertEquals(TokenType.EOF, s.next_token().type);

        s = new Scanner("/* comment */ int");
        assertEquals(TokenType.KW_INT, s.next_token().type);
    }
}