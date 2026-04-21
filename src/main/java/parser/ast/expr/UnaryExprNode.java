package parser.ast.expr;

import lexer.Token;
import utils.ASTVisitor;
import parser.ast.ExpressionNode;

public class UnaryExprNode extends ExpressionNode {
    public final Token operator;
    public final ExpressionNode operand;

    public UnaryExprNode(Token operator, ExpressionNode operand, int line, int column) {
        super(line, column);
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}