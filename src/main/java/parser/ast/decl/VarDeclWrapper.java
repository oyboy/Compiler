package parser.ast.decl;

import utils.ASTVisitor;
import parser.ast.DeclarationNode;
import parser.ast.stmt.VarDeclStmtNode;

public class VarDeclWrapper extends DeclarationNode {
    public final VarDeclStmtNode varDecl;

    public VarDeclWrapper(VarDeclStmtNode varDecl, int line, int column) {
        super(line, column);
        this.varDecl = varDecl;
    }

    @Override
    public <R> R accept(ASTVisitor<R> visitor) {
        return visitor.visit(this);
    }
}