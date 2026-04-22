package utils;

import parser.ast.*;
import parser.ast.decl.*;
import parser.ast.expr.*;
import parser.ast.stmt.*;

import java.util.stream.Collectors;

public class TypeAnnotatedPrinter implements ASTVisitor<String> {
    private int indent = 0;

    public String print(ProgramNode program) {
        StringBuilder sb = new StringBuilder();
        sb.append("Program [type-checked]:\n");
        indent++;
        for (DeclarationNode decl : program.declarations) {
            sb.append(decl.accept(this));
        }
        indent--;
        return sb.toString();
    }

    private String pad() {
        return "  ".repeat(indent);
    }

    private String withType(ExpressionNode node) {
        String expr = node.accept(this);
        if (node.resolvedType != null) {
            return expr + " [type: " + node.resolvedType + "]";
        }
        return expr + " [type: ?]";
    }

    @Override
    public String visit(FunctionDeclNode node) {
        StringBuilder sb = new StringBuilder();
        String ret = node.returnType != null ? node.returnType : "void";
        sb.append(pad()).append("FunctionDecl: ").append(node.name)
                .append(" -> ").append(ret).append("\n");
        indent++;

        if (node.params.isEmpty()) {
            sb.append(pad()).append("Parameters: []\n");
        } else {
            sb.append(pad()).append("Parameters:\n");
            indent++;
            for (ParamNode p : node.params) {
                sb.append(pad()).append("- ").append(p.name).append(": ").append(p.type).append("\n");
            }
            indent--;
        }

        sb.append(pad()).append("Body [type-checked]:\n");
        indent++;
        sb.append(node.body.accept(this));
        indent--;
        indent--;
        return sb.toString();
    }

    @Override
    public String visit(StructDeclNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(pad()).append("StructDecl: ").append(node.name).append("\n");
        indent++;
        sb.append(pad()).append("Fields:\n");
        indent++;
        for (VarDeclStmtNode field : node.fields) {
            sb.append(pad()).append("- ").append(field.name).append(": ").append(field.type).append("\n");
        }
        indent--;
        indent--;
        return sb.toString();
    }

    @Override
    public String visit(VarDeclWrapper node) {
        return node.varDecl.accept(this);
    }

    @Override
    public String visit(BlockStmtNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(pad()).append("Block:\n");
        indent++;
        for (StatementNode stmt : node.statements) {
            sb.append(stmt.accept(this));
        }
        indent--;
        return sb.toString();
    }

    @Override
    public String visit(VarDeclStmtNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(pad()).append("VarDecl: ").append(node.type).append(" ").append(node.name);
        if (node.initializer != null) {
            sb.append(" = ").append(withType(node.initializer));
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String visit(ExprStmtNode node) {
        return pad() + "ExprStmt: " + withType(node.expression) + "\n";
    }

    @Override
    public String visit(IfStmtNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(pad()).append("If:\n");
        indent++;
        sb.append(pad()).append("Condition: ").append(withType(node.condition)).append("\n");
        sb.append(pad()).append("Then:\n");
        indent++;
        sb.append(node.thenBranch.accept(this));
        indent--;
        if (node.elseBranch != null) {
            sb.append(pad()).append("Else:\n");
            indent++;
            sb.append(node.elseBranch.accept(this));
            indent--;
        }
        indent--;
        return sb.toString();
    }

    @Override
    public String visit(WhileStmtNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(pad()).append("While:\n");
        indent++;
        sb.append(pad()).append("Condition: ").append(withType(node.condition)).append("\n");
        sb.append(pad()).append("Body:\n");
        indent++;
        sb.append(node.body.accept(this));
        indent--;
        indent--;
        return sb.toString();
    }

    @Override
    public String visit(ForStmtNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(pad()).append("For:\n");
        indent++;
        if (node.initializer != null) {
            sb.append(pad()).append("Init:\n");
            indent++;
            sb.append(node.initializer.accept(this));
            indent--;
        }
        if (node.condition != null) {
            sb.append(pad()).append("Condition: ").append(withType(node.condition)).append("\n");
        }
        if (node.update != null) {
            sb.append(pad()).append("Update: ").append(withType(node.update)).append("\n");
        }
        sb.append(pad()).append("Body:\n");
        indent++;
        sb.append(node.body.accept(this));
        indent--;
        indent--;
        return sb.toString();
    }

    @Override
    public String visit(ReturnStmtNode node) {
        if (node.value != null) {
            return pad() + "Return: " + withType(node.value) + "\n";
        }
        return pad() + "Return\n";
    }

    @Override
    public String visit(BinaryExprNode node) {
        return "(" + node.left.accept(this) + " " + node.operator.lexeme + " " + node.right.accept(this) + ")";
    }

    @Override
    public String visit(UnaryExprNode node) {
        return "(" + node.operator.lexeme + node.operand.accept(this) + ")";
    }

    @Override
    public String visit(LiteralExprNode node) {
        if (node.literalType.equals("string")) return "\"" + node.value + "\"";
        return node.value != null ? node.value.toString() : "null";
    }

    @Override
    public String visit(IdentifierExprNode node) {
        if (node.resolvedSymbol != null) {
            return node.name + "@" + node.resolvedSymbol.line;
        }
        return node.name;
    }

    @Override
    public String visit(GroupingExprNode node) {
        return node.expression.accept(this);
    }

    @Override
    public String visit(AssignmentExprNode node) {
        return "(" + node.target.accept(this) + " = " + node.value.accept(this) + ")";
    }

    @Override
    public String visit(CallExprNode node) {
        String args = node.arguments.stream()
                .map(a -> a.accept(this))
                .collect(Collectors.joining(", "));
        String resolved = node.resolvedSymbol != null ? "@" + node.resolvedSymbol.line : "";
        return node.callee.accept(this) + resolved + "(" + args + ")";
    }
}
