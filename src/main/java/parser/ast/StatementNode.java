package parser.ast;

import utils.ASTVisitor;

public abstract class StatementNode extends ASTNode {
    public StatementNode(int line, int column) { super(line, column); }
    public abstract <R> R accept(ASTVisitor<R> visitor);
}
