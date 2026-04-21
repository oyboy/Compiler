package parser.ast.expr;

import lexer.Token;
import utils.ASTVisitor;
import parser.ast.ExpressionNode;

public class BinaryExprNode extends ExpressionNode {
    public final ExpressionNode left;
    public final Token operator;
    public final ExpressionNode right;

    public BinaryExprNode(ExpressionNode left, Token operator, ExpressionNode right, int line, int column) {
        super(line, column);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}