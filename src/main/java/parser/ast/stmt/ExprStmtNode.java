package parser.ast.stmt;

import utils.ASTVisitor;
import parser.ast.ExpressionNode;
import parser.ast.StatementNode;

public class ExprStmtNode extends StatementNode {
    public final ExpressionNode expression;

    public ExprStmtNode(ExpressionNode expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
