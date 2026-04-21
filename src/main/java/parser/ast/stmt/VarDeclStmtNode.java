package parser.ast.stmt;

import parser.ast.ExpressionNode;
import parser.ast.StatementNode;

import utils.ASTVisitor;

public class VarDeclStmtNode extends StatementNode {
    public final String type;
    public final String name;
    public final ExpressionNode initializer;

    public VarDeclStmtNode(String type, String name, ExpressionNode initializer, int line, int column) {
        super(line, column);
        this.type = type;
        this.name = name;
        this.initializer = initializer;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
