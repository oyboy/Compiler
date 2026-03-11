package lexer;

import java.util.HashMap;
import java.util.Map;

public class Scanner {
    private final String source;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private int startLine = 1;
    private int startColumn = 1;

    private Token stashedToken = null;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("if", TokenType.KW_IF);
        keywords.put("else", TokenType.KW_ELSE);
        keywords.put("while", TokenType.KW_WHILE);
        keywords.put("for", TokenType.KW_FOR);
        keywords.put("int", TokenType.KW_INT);
        keywords.put("float", TokenType.KW_FLOAT);
        keywords.put("bool", TokenType.KW_BOOL);
        keywords.put("return", TokenType.KW_RETURN);
        keywords.put("true", TokenType.KW_TRUE);
        keywords.put("false", TokenType.KW_FALSE);
        keywords.put("void", TokenType.KW_VOID);
        keywords.put("struct", TokenType.KW_STRUCT);
        keywords.put("fn", TokenType.KW_FN);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public Token next_token() {
        if (stashedToken != null) {
            Token t = stashedToken;
            stashedToken = null;
            return t;
        }
        return scanToken();
    }

    public Token peek_token() {
        if (stashedToken == null) {
            stashedToken = scanToken();
        }
        return stashedToken;
    }

    private Token scanToken() {
        skipWhitespace();

        start = current;
        startColumn = column;
        startLine = line;

        if (isAtEnd()) return makeToken(TokenType.EOF, null);

        char c = advance();

        if (isAlpha(c)) return identifier();
        if (isDigit(c)) return number();

        switch (c) {
            case '(': return makeToken(TokenType.LPAREN);
            case ')': return makeToken(TokenType.RPAREN);
            case '{': return makeToken(TokenType.LBRACE);
            case '}': return makeToken(TokenType.RBRACE);
            case '[': return makeToken(TokenType.LBRACKET);
            case ']': return makeToken(TokenType.RBRACKET);
            case ':': return makeToken(TokenType.COLON);
            case ';': return makeToken(TokenType.SEMICOLON);
            case ',': return makeToken(TokenType.COMMA);
            case '.': return makeToken(TokenType.DOT);

            case '+':
                if (match('=')) return makeToken(TokenType.PLUS_ASSIGN);
                return makeToken(TokenType.OP_PLUS);
            case '-':
                if (match('=')) return makeToken(TokenType.MINUS_ASSIGN);
                return makeToken(TokenType.OP_MINUS);
            case '*':
                if (match('=')) return makeToken(TokenType.MULTIPLY_ASSIGN);
                return makeToken(TokenType.OP_MULTIPLY);
            case '%': return makeToken(TokenType.OP_MODULO);

            case '!': return makeToken(match('=') ? TokenType.OP_NEQ : TokenType.OP_NOT);
            case '=': return makeToken(match('=') ? TokenType.OP_EQ : TokenType.OP_ASSIGN);
            case '<': return makeToken(match('=') ? TokenType.OP_LTE : TokenType.OP_LT);
            case '>': return makeToken(match('=') ? TokenType.OP_GTE : TokenType.OP_GT);

            case '&':
                if (match('&')) return makeToken(TokenType.OP_AND);
                return errorToken("Unexpected character '&'");

            case '|':
                if (match('|')) return makeToken(TokenType.OP_OR);
                return errorToken("Unexpected character '|'");

            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                    return scanToken();

                }
                else if (match('=')) {
                    return makeToken(TokenType.DIVIDE_ASSIGN);
                } else if (match('*')) {
                    return scanMultiLineComment();
                } else {
                    return makeToken(TokenType.OP_DIVIDE);
                }

            case '"': return string();

            default:
                return errorToken("Unexpected character: " + c);
        }
    }

    private Token identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);

        Object literal = null;
        if (type == TokenType.KW_TRUE) literal = true;
        if (type == TokenType.KW_FALSE) literal = false;

        return makeToken(type, literal);
    }

    private Token number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // Consume '.'
            while (isDigit(peek())) advance();

            // Float
            String text = source.substring(start, current);
            return makeToken(TokenType.LIT_FLOAT, Double.parseDouble(text));
        }

        // Integer
        String text = source.substring(start, current);
        try {
            return makeToken(TokenType.LIT_INT, Integer.parseInt(text));
        } catch (NumberFormatException e) {
            return errorToken("Integer overflow");
        }
    }

    private Token string() {
        while (peek() != '"' && !isAtEnd() && peek() != '\n' && peek() != '\r') {
            advance();
        }
        if (isAtEnd() || peek() == '\n' || peek() == '\r') {
            return errorToken("Unterminated string");
        }
        advance();

        String value = source.substring(start + 1, current - 1);
        return makeToken(TokenType.LIT_STRING, value);
    }

    private Token scanMultiLineComment() {
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // *
                advance(); // /
                return scanToken();
            }
            if (peek() == '\n') { line++; column = 1; }
            advance();
        }
        return errorToken("Unterminated multi-line comment");
    }

    private char advance() {
        current++;
        column++;
        return source.charAt(current - 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        column++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void skipWhitespace() {
        while (true) {
            char c = peek();
            switch (c) {
                case ' ':
                case '\r':
                case '\t':
                    advance();
                    break;
                case '\n':
                    line++;
                    advance();
                    column = 1;
                    break;
                default:
                    return;
            }
        }
    }

    private Token makeToken(TokenType type) {
        return makeToken(type, null);
    }

    private Token makeToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        return new Token(type, text, literal, startLine, startColumn);
    }

    private Token errorToken(String message) {
        return new Token(TokenType.ERROR, message, null, startLine, startColumn);
    }
}
