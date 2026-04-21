package parser.ast.expr;

import utils.ASTVisitor;
import parser.ast.ExpressionNode;

import java.util.List;

public class CallExprNode extends ExpressionNode {
    public final ExpressionNode callee;
    public final List<ExpressionNode> arguments;

    public CallExprNode(ExpressionNode callee, List<ExpressionNode> arguments, int line, int column) {
        super(line, column);
        this.callee = callee;
        this.arguments = arguments;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}