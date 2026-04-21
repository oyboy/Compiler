package parser.ast.decl;

import utils.ASTVisitor;
import parser.ast.DeclarationNode;
import parser.ast.StatementNode;

import java.util.List;

public class FunctionDeclNode extends DeclarationNode {
    public final String name;
    public final String returnType;
    public final List<ParamNode> params;
    public final StatementNode body;

    public FunctionDeclNode(String name, String returnType, List<ParamNode> params, StatementNode body, int line, int column) {
        super(line, column);
        this.name = name;
        this.returnType = returnType;
        this.params = params;
        this.body = body;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}