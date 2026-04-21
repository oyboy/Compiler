package parser.ast.stmt;

import utils.ASTVisitor;
import parser.ast.ExpressionNode;
import parser.ast.StatementNode;

public class ForStmtNode extends StatementNode {
    public final StatementNode initializer;
    public final ExpressionNode condition;
    public final ExpressionNode update;
    public final StatementNode body;

    public ForStmtNode(StatementNode initializer, ExpressionNode condition, ExpressionNode update, StatementNode body, int line, int column) {
        super(line, column);
        this.initializer = initializer;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
