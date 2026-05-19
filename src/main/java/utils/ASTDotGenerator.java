package utils;

import parser.ast.*;
import parser.ast.decl.*;
import parser.ast.expr.*;
import parser.ast.stmt.*;

public class ASTDotGenerator implements ASTVisitor<Integer> {
    private final StringBuilder sb = new StringBuilder();
    private int nodeCount = 0;

    public String generate(ProgramNode program) {
        sb.append("digraph AST {\n");
        sb.append("  rankdir=TB;\n");
        sb.append("  node [shape=box, fontname=\"Courier\", fontsize=10];\n\n");

        int rootId = createNode("Program", "lightblue");
        for (DeclarationNode decl : program.declarations) {
            int childId = decl.accept(this);
            edge(rootId, childId);
        }

        sb.append("}\n");
        return sb.toString();
    }

    private int createNode(String label, String color) {
        int id = nodeCount++;
        sb.append("  n").append(id)
                .append(" [label=\"").append(escape(label))
                .append("\", style=filled, fillcolor=\"").append(color)
                .append("\"];\n");
        return id;
    }

    private void edge(int from, int to) {
        sb.append("  n").append(from).append(" -> n").append(to).append(";\n");
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }

    @Override
    public Integer visit(FunctionDeclNode node) {
        String ret = node.returnType != null ? node.returnType : "void";
        int id = createNode("FunctionDecl\\n" + node.name + " -> " + ret, "#90EE90");

        if (!node.params.isEmpty()) {
            int paramsId = createNode("Params", "#98FB98");
            edge(id, paramsId);
            for (ParamNode p : node.params) {
                int pId = createNode("Param\\n" + p.type + " " + p.name, "#98FB98");
                edge(paramsId, pId);
            }
        }

        int bodyId = node.body.accept(this);
        edge(id, bodyId);
        return id;
    }

    @Override
    public Integer visit(StructDeclNode node) {
        int id = createNode("StructDecl\\n" + node.name, "#90EE90");
        for (VarDeclStmtNode field : node.fields) {
            edge(id, field.accept(this));
        }
        return id;
    }

    @Override
    public Integer visit(VarDeclWrapper node) {
        return node.varDecl.accept(this);
    }

    @Override
    public Integer visit(BlockStmtNode node) {
        int id = createNode("Block", "#FFFFCC");
        for (StatementNode stmt : node.statements) {
            edge(id, stmt.accept(this));
        }
        return id;
    }
    @Override
    public Integer visit(BreakStmtNode node) {
        return createNode("Break", "#FFCCCC");
    }

    @Override
    public Integer visit(ContinueStmtNode node) {
        return createNode("Continue", "#CCFFCC");
    }

    @Override
    public Integer visit(VarDeclStmtNode node) {
        String label = "VarDecl\\n" + node.type + " " + node.name;
        int id = createNode(label, "#FFFFCC");
        if (node.initializer != null) {
            int initId = node.initializer.accept(this);
            edge(id, initId);
        }
        return id;
    }

    @Override
    public Integer visit(ExprStmtNode node) {
        int id = createNode("ExprStmt", "#FFFFCC");
        edge(id, node.expression.accept(this));
        return id;
    }

    @Override
    public Integer visit(IfStmtNode node) {
        int id = createNode("If", "#FFFFCC");

        int condId = createNode("Condition", "#FFE4B5");
        edge(id, condId);
        edge(condId, node.condition.accept(this));

        int thenId = createNode("Then", "#FFE4B5");
        edge(id, thenId);
        edge(thenId, node.thenBranch.accept(this));

        if (node.elseBranch != null) {
            int elseId = createNode("Else", "#FFE4B5");
            edge(id, elseId);
            edge(elseId, node.elseBranch.accept(this));
        }
        return id;
    }

    @Override
    public Integer visit(WhileStmtNode node) {
        int id = createNode("While", "#FFFFCC");
        int condId = createNode("Condition", "#FFE4B5");
        edge(id, condId);
        edge(condId, node.condition.accept(this));
        int bodyId = createNode("Body", "#FFE4B5");
        edge(id, bodyId);
        edge(bodyId, node.body.accept(this));
        return id;
    }

    @Override
    public Integer visit(ForStmtNode node) {
        int id = createNode("For", "#FFFFCC");
        if (node.initializer != null) {
            int initId = createNode("Init", "#FFE4B5");
            edge(id, initId);
            edge(initId, node.initializer.accept(this));
        }
        if (node.condition != null) {
            int condId = createNode("Condition", "#FFE4B5");
            edge(id, condId);
            edge(condId, node.condition.accept(this));
        }
        if (node.update != null) {
            int updId = createNode("Update", "#FFE4B5");
            edge(id, updId);
            edge(updId, node.update.accept(this));
        }
        int bodyId = createNode("Body", "#FFE4B5");
        edge(id, bodyId);
        edge(bodyId, node.body.accept(this));
        return id;
    }

    @Override
    public Integer visit(ReturnStmtNode node) {
        int id = createNode("Return", "#FFFFCC");
        if (node.value != null) {
            edge(id, node.value.accept(this));
        }
        return id;
    }

    @Override
    public Integer visit(BinaryExprNode node) {
        int id = createNode("BinaryOp\\n" + node.operator.lexeme, "#FFD700");
        edge(id, node.left.accept(this));
        edge(id, node.right.accept(this));
        return id;
    }

    @Override
    public Integer visit(UnaryExprNode node) {
        int id = createNode("UnaryOp\\n" + node.operator.lexeme, "#FFD700");
        edge(id, node.operand.accept(this));
        return id;
    }

    @Override
    public Integer visit(LiteralExprNode node) {
        String val = node.value != null ? node.value.toString() : "null";
        return createNode("Literal\\n" + node.literalType + ": " + val, "#FFA07A");
    }

    @Override
    public Integer visit(IdentifierExprNode node) {
        return createNode("Identifier\\n" + node.name, "#FFA07A");
    }

    @Override
    public Integer visit(GroupingExprNode node) {
        int id = createNode("Grouping", "#FFD700");
        edge(id, node.expression.accept(this));
        return id;
    }

    @Override
    public Integer visit(AssignmentExprNode node) {
        int id = createNode("Assign\\n" + node.operator.lexeme, "#FFD700");
        edge(id, node.target.accept(this));
        edge(id, node.value.accept(this));
        return id;
    }

    @Override
    public Integer visit(CallExprNode node) {
        int id = createNode("Call", "#FFD700");
        int calleeId = node.callee.accept(this);
        edge(id, calleeId);
        for (ExpressionNode arg : node.arguments) {
            edge(id, arg.accept(this));
        }
        return id;
    }

    @Override
    public Integer visit(ArrayIndexExprNode node) {
        int id = createNode("Index: " + node.arrayName, "#FFA07A");
        int indexId = node.index.accept(this);
        edge(id, indexId);
        return id;
    }

    @Override
    public Integer visit(StmtWrapper node) {
        return node.statement.accept(this);
    }
}
