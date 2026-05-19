package parser.ast.expr;

import parser.ast.ExpressionNode;
import utils.ASTVisitor;

public class ArrayIndexExprNode extends ExpressionNode {
    public final String arrayName;
    public final ExpressionNode index;

    public ArrayIndexExprNode(String arrayName, ExpressionNode index, int line, int column) {
        super(line, column);
        this.arrayName = arrayName;
        this.index = index;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}