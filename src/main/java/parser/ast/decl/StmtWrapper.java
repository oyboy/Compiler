package parser.ast.decl;

import parser.ast.DeclarationNode;
import parser.ast.StatementNode;
import utils.ASTVisitor;

public class StmtWrapper extends DeclarationNode {
    public final StatementNode statement;

    public StmtWrapper(StatementNode statement, int line, int column) {
        super(line, column);
        this.statement = statement;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
