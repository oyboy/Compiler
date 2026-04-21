package parser;

import lexer.Token;
import lexer.TokenType;
import parser.ast.*;
import parser.ast.expr.*;
import parser.ast.stmt.*;
import parser.ast.decl.*;

import java.util.ArrayList;
import java.util.List;

import static lexer.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {
        ParseError(String msg) { super(msg); }
    }

    private final List<Token> tokens;
    private final List<String> errors = new ArrayList<>();
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<String> getErrors() { return errors; }

    public ProgramNode parse() {
        List<DeclarationNode> declarations = new ArrayList<>();
        while (!isAtEnd()) {
            try {
                declarations.add(declaration());
            } catch (ParseError e) {
                synchronize();
            }
        }
        return new ProgramNode(declarations, 1, 1);
    }

    private DeclarationNode declaration() {
        if (match(KW_FN)) return functionDecl();
        if (match(KW_STRUCT)) return structDecl();
        if (isVarDeclaration()) return varDeclAsDeclaration();

        throw error(peek(), "Expect declaration (fn, struct, or variable).");
    }

    private FunctionDeclNode functionDecl() {
        Token fnToken = previous();
        Token nameToken = consume(IDENTIFIER, "Expect function name.");

        consume(LPAREN, "Expect '(' after function name.");
        List<ParamNode> params = new ArrayList<>();
        if (!check(RPAREN)) {
            do {
                String type = parseTypeName();
                Token paramName = consume(IDENTIFIER, "Expect parameter name.");
                params.add(new ParamNode(type, paramName.lexeme, paramName.line, paramName.column));
            } while (match(COMMA));
        }
        consume(RPAREN, "Expect ')' after parameters.");

        String returnType = "void";
        if (match(ARROW)) {
            returnType = parseTypeName();
        }

        consume(LBRACE, "Expect '{' before function body.");
        BlockStmtNode body = blockStmt();

        return new FunctionDeclNode(nameToken.lexeme, returnType, params, body, fnToken.line, fnToken.column);
    }

    private StructDeclNode structDecl() {
        Token structToken = previous();
        Token nameToken = consume(IDENTIFIER, "Expect struct name.");
        consume(LBRACE, "Expect '{' before struct body.");

        List<VarDeclStmtNode> fields = new ArrayList<>();
        while (!check(RBRACE) && !isAtEnd()) {
            fields.add(varDeclStmt());
        }
        consume(RBRACE, "Expect '}' after struct body.");

        return new StructDeclNode(nameToken.lexeme, fields, structToken.line, structToken.column);
    }

    private DeclarationNode varDeclAsDeclaration() {
        VarDeclStmtNode varDecl = varDeclStmt();
        return new VarDeclWrapper(varDecl, varDecl.line, varDecl.column);
    }

    private VarDeclStmtNode varDeclStmt() {
        String type = parseTypeName();
        Token nameToken = consume(IDENTIFIER, "Expect variable name.");
        ExpressionNode initializer = null;
        if (match(OP_ASSIGN)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new VarDeclStmtNode(type, nameToken.lexeme, initializer, nameToken.line, nameToken.column);
    }

    private StatementNode statement() {
        if (match(LBRACE)) return blockStmt();
        if (match(KW_IF)) return ifStmt();
        if (match(KW_WHILE)) return whileStmt();
        if (match(KW_FOR)) return forStmt();
        if (match(KW_RETURN)) return returnStmt();
        if (match(SEMICOLON)) return new BlockStmtNode(new ArrayList<>(), previous().line, previous().column);
        if (isVarDeclaration()) return varDeclStmt();
        return exprStmt();
    }

    private BlockStmtNode blockStmt() {
        Token brace = previous();
        List<StatementNode> stmts = new ArrayList<>();
        while (!check(RBRACE) && !isAtEnd()) {
            stmts.add(statement());
        }
        consume(RBRACE, "Expect '}' after block.");
        return new BlockStmtNode(stmts, brace.line, brace.column);
    }

    private IfStmtNode ifStmt() {
        Token ifToken = previous();
        consume(LPAREN, "Expect '(' after 'if'.");
        ExpressionNode condition = expression();
        consume(RPAREN, "Expect ')' after if condition.");
        StatementNode thenBranch = statement();
        StatementNode elseBranch = match(KW_ELSE) ? statement() : null;
        return new IfStmtNode(condition, thenBranch, elseBranch, ifToken.line, ifToken.column);
    }

    private WhileStmtNode whileStmt() {
        Token whileToken = previous();
        consume(LPAREN, "Expect '(' after 'while'.");
        ExpressionNode condition = expression();
        consume(RPAREN, "Expect ')' after condition.");
        return new WhileStmtNode(condition, statement(), whileToken.line, whileToken.column);
    }

    private ForStmtNode forStmt() {
        Token forToken = previous();
        consume(LPAREN, "Expect '(' after 'for'.");

        StatementNode initializer = null;
        if (match(SEMICOLON)) { }
        else if (isVarDeclaration()) initializer = varDeclStmt();
        else initializer = exprStmt();

        ExpressionNode condition = !check(SEMICOLON) ? expression() : null;
        consume(SEMICOLON, "Expect ';' after loop condition.");

        ExpressionNode update = !check(RPAREN) ? expression() : null;
        consume(RPAREN, "Expect ')' after for clauses.");

        return new ForStmtNode(initializer, condition, update, statement(), forToken.line, forToken.column);
    }

    private ReturnStmtNode returnStmt() {
        Token retToken = previous();
        ExpressionNode value = !check(SEMICOLON) ? expression() : null;
        consume(SEMICOLON, "Expect ';' after return.");
        return new ReturnStmtNode(value, retToken.line, retToken.column);
    }

    private ExprStmtNode exprStmt() {
        ExpressionNode expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new ExprStmtNode(expr, expr.line, expr.column);
    }

    private ExpressionNode expression() { return assignment(); }

    private ExpressionNode assignment() {
        ExpressionNode expr = logicalOr();

        if (match(OP_ASSIGN, PLUS_ASSIGN, MINUS_ASSIGN, MULTIPLY_ASSIGN, DIVIDE_ASSIGN)) {
            Token op = previous();
            ExpressionNode value = assignment();

            if (expr instanceof IdentifierExprNode) {
                if (op.type != OP_ASSIGN) {
                    Token binaryOp = desugarOp(op);
                    value = new BinaryExprNode(expr, binaryOp, value, expr.line, expr.column);
                }
                Token assignOp = new Token(OP_ASSIGN, "=", null, op.line, op.column);
                return new AssignmentExprNode(expr, assignOp, value, expr.line, expr.column);
            }
            throw error(op, "Invalid assignment target.");
        }
        return expr;
    }

    private ExpressionNode logicalOr()      { return parseBinary(this::logicalAnd, OP_OR); }
    private ExpressionNode logicalAnd()     { return parseBinary(this::equality, OP_AND); }
    private ExpressionNode equality()       { return parseBinary(this::relational, OP_EQ, OP_NEQ); }
    private ExpressionNode relational()     { return parseBinary(this::additive, OP_LT, OP_LTE, OP_GT, OP_GTE); }
    private ExpressionNode additive()       { return parseBinary(this::multiplicative, OP_PLUS, OP_MINUS); }
    private ExpressionNode multiplicative() { return parseBinary(this::unary, OP_MULTIPLY, OP_DIVIDE, OP_MODULO); }

    private ExpressionNode parseBinary(java.util.function.Supplier<ExpressionNode> next, TokenType... types) {
        ExpressionNode expr = next.get();
        while (match(types)) {
            Token op = previous();
            ExpressionNode right = next.get();
            expr = new BinaryExprNode(expr, op, right, expr.line, expr.column);
        }
        return expr;
    }

    private ExpressionNode unary() {
        if (match(OP_NOT, OP_MINUS)) {
            Token op = previous();
            return new UnaryExprNode(op, unary(), op.line, op.column);
        }
        return primary();
    }

    private ExpressionNode primary() {
        if (match(KW_TRUE))
            return new LiteralExprNode(true, "bool", previous().line, previous().column);
        if (match(KW_FALSE))
            return new LiteralExprNode(false, "bool", previous().line, previous().column);
        if (match(LIT_INT))
            return new LiteralExprNode(previous().literal, "int", previous().line, previous().column);
        if (match(LIT_FLOAT))
            return new LiteralExprNode(previous().literal, "float", previous().line, previous().column);
        if (match(LIT_STRING))
            return new LiteralExprNode(previous().literal, "string", previous().line, previous().column);

        if (match(IDENTIFIER)) {
            Token name = previous();
            if (match(LPAREN)) return finishCall(name);
            return new IdentifierExprNode(name.lexeme, name.line, name.column);
        }

        if (match(LPAREN)) {
            Token paren = previous();
            ExpressionNode expr = expression();
            consume(RPAREN, "Expect ')' after expression.");
            return new GroupingExprNode(expr, paren.line, paren.column);
        }

        throw error(peek(), "Expect expression.");
    }

    private CallExprNode finishCall(Token callee) {
        IdentifierExprNode calleeNode = new IdentifierExprNode(callee.lexeme, callee.line, callee.column);
        List<ExpressionNode> args = new ArrayList<>();
        if (!check(RPAREN)) {
            do { args.add(expression()); } while (match(COMMA));
        }
        consume(RPAREN, "Expect ')' after arguments.");
        return new CallExprNode(calleeNode, args, callee.line, callee.column);
    }

    private boolean isVarDeclaration() {
        if (check(KW_INT) || check(KW_FLOAT) || check(KW_BOOL) || check(KW_VOID)) return true;
        if (check(IDENTIFIER) && current + 1 < tokens.size() && tokens.get(current + 1).type == IDENTIFIER) return true;
        return false;
    }

    private String parseTypeName() {
        if (match(KW_INT)) return "int";
        if (match(KW_FLOAT)) return "float";
        if (match(KW_BOOL)) return "bool";
        if (match(KW_VOID)) return "void";
        if (match(IDENTIFIER)) return previous().lexeme;
        throw error(peek(), "Expect type.");
    }

    private Token desugarOp(Token op) {
        TokenType t;
        switch (op.type) {
            case PLUS_ASSIGN: t = OP_PLUS; break;
            case MINUS_ASSIGN: t = OP_MINUS; break;
            case MULTIPLY_ASSIGN: t = OP_MULTIPLY; break;
            case DIVIDE_ASSIGN: t = OP_DIVIDE; break;
            default: return op;
        }
        return new Token(t, op.lexeme.substring(0, 1), null, op.line, op.column);
    }

    private boolean match(TokenType... types) {
        for (TokenType t : types) { if (check(t)) { advance(); return true; } }
        return false;
    }

    private Token consume(TokenType type, String msg) {
        if (check(type)) return advance();
        throw error(peek(), msg);
    }

    private boolean check(TokenType type) { return !isAtEnd() && peek().type == type; }
    private Token advance() { if (!isAtEnd()) current++; return previous(); }
    private boolean isAtEnd() { return peek().type == EOF; }
    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }

    private ParseError error(Token token, String msg) {
        String full = "[Line " + token.line + ", Col " + token.column + "] " + msg + " Got: " + token.type + " ('" + token.lexeme + "')";
        errors.add(full);
        return new ParseError(full);
    }

    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                case KW_FN: case KW_STRUCT: case KW_INT: case KW_FLOAT:
                case KW_BOOL: case KW_IF: case KW_WHILE: case KW_FOR: case KW_RETURN: return;
            }
            advance();
        }
    }
}