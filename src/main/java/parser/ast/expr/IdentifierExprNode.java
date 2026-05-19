package parser.ast.expr;

import utils.ASTVisitor;
import parser.ast.ExpressionNode;

public class IdentifierExprNode extends ExpressionNode {
    public String name;

    public IdentifierExprNode(String name, int line, int column) {
        super(line, column);
        this.name = name;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
