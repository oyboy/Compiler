package parser.ast.stmt;

import utils.ASTVisitor;
import parser.ast.ExpressionNode;
import parser.ast.StatementNode;

public class ReturnStmtNode extends StatementNode {
    public final ExpressionNode value;

    public ReturnStmtNode(ExpressionNode value, int line, int column) {
        super(line, column);
        this.value = value;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
