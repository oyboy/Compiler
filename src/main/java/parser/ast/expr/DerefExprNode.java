package parser.ast.expr;

import parser.ast.ExpressionNode;
import utils.ASTVisitor;

public class DerefExprNode extends ExpressionNode {
    public ExpressionNode pointer;
    public ExpressionNode index;

    public DerefExprNode(ExpressionNode pointer, ExpressionNode index,
                         int line, int column) {
        super(line, column);
        this.pointer = pointer;
        this.index = index;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
