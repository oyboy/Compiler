package parser.ast.decl;

import parser.ast.ASTNode;

public class ParamNode extends ASTNode {
    public final String type;
    public final String name;

    public ParamNode(String type, String name, int line, int column) {
        super(line, column);
        this.type = type;
        this.name = name;
    }
}
