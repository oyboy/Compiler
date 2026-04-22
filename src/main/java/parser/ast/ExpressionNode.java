package parser.ast;

import semantic.Symbol;
import semantic.Type;
import utils.ASTVisitor;

public abstract class ExpressionNode extends ASTNode {
    public Type resolvedType = null;
    public Symbol resolvedSymbol = null;
    public ExpressionNode(int line, int column) { super(line, column); }
    public abstract <R> R accept(ASTVisitor<R> visitor);
}
