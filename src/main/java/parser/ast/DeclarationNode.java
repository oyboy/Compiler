package parser.ast;

import utils.ASTVisitor;

public abstract class DeclarationNode extends ASTNode {
    public DeclarationNode(int line, int column) {
        super(line, column);
    }

    public abstract <R> R accept(ASTVisitor<R> visitor);
}
