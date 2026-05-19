package parser.ast.stmt;

import parser.ast.StatementNode;
import utils.ASTVisitor;

public class BreakStmtNode extends StatementNode {
    public BreakStmtNode(int line, int column) {
        super(line, column);
    }
    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
