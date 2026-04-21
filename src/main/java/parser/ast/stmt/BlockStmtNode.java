package parser.ast.stmt;

import utils.ASTVisitor;
import parser.ast.StatementNode;

import java.util.List;

public class BlockStmtNode extends StatementNode {
    public final List<StatementNode> statements;

    public BlockStmtNode(List<StatementNode> statements, int line, int column) {
        super(line, column);
        this.statements = statements;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
