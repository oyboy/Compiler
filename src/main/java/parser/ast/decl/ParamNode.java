package parser.ast.decl;

import parser.ast.ASTNode;

public class ParamNode extends ASTNode {
    public final String type;
    public final String name;
    public final int size;

    public ParamNode(String type, String name, int size, int line, int column) {
        super(line, column);
        this.type = type;
        this.name = name;
        this.size = size;
    }
}
