package parser.ast.expr;

import lexer.Token;
import parser.ast.ExpressionNode;

import utils.ASTVisitor;

public class AssignmentExprNode extends ExpressionNode {
    public final ExpressionNode target;
    public final Token operator;
    public final ExpressionNode value;

    public AssignmentExprNode(ExpressionNode target, Token operator, ExpressionNode value, int line, int column) {
        super(line, column);
        this.target = target;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}