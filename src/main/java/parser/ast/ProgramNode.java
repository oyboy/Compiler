package parser.ast;

import java.util.List;

public class ProgramNode extends ASTNode {
    public final List<DeclarationNode> declarations;

    public ProgramNode(List<DeclarationNode> declarations, int line, int column) {
        super(line, column);
        this.declarations = declarations;
    }
}
