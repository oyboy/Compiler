package parser.ast;

import utils.ASTVisitor;

public abstract class ExpressionNode extends ASTNode {
    public ExpressionNode(int line, int column) { super(line, column); }
    public abstract <R> R accept(ASTVisitor<R> visitor);
}
