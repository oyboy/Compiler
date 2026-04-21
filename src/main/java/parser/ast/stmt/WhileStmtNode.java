package parser.ast.stmt;

import utils.ASTVisitor;
import parser.ast.ExpressionNode;
import parser.ast.StatementNode;

public class WhileStmtNode extends StatementNode {
    public final ExpressionNode condition;
    public final StatementNode body;

    public WhileStmtNode(ExpressionNode condition, StatementNode body, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.body = body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
