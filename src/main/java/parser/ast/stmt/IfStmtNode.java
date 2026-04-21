package parser.ast.stmt;

import utils.ASTVisitor;
import parser.ast.ExpressionNode;
import parser.ast.StatementNode;

public class IfStmtNode extends StatementNode {
    public final ExpressionNode condition;
    public final StatementNode thenBranch;
    public final StatementNode elseBranch;

    public IfStmtNode(ExpressionNode condition, StatementNode thenBranch, StatementNode elseBranch, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}