package parser.ast.expr;

import utils.ASTVisitor;
import parser.ast.ExpressionNode;

public class LiteralExprNode extends ExpressionNode {
    public final Object value;
    public final String literalType;

    public LiteralExprNode(Object value, String literalType, int line, int column) {
        super(line, column);
        this.value = value;
        this.literalType = literalType;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
