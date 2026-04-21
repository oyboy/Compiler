package parser.ast.expr;

import utils.ASTVisitor;
import parser.ast.ExpressionNode;

public class GroupingExprNode extends ExpressionNode {
    public final ExpressionNode expression;

    public GroupingExprNode(ExpressionNode expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
