package parser.ast.decl;

import utils.ASTVisitor;
import parser.ast.DeclarationNode;
import parser.ast.stmt.VarDeclStmtNode;

import java.util.List;

public class StructDeclNode extends DeclarationNode {
    public final String name;
    public final List<VarDeclStmtNode> fields;

    public StructDeclNode(String name, List<VarDeclStmtNode> fields, int line, int column) {
        super(line, column);
        this.name = name;
        this.fields = fields;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
