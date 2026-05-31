package utils;

import parser.ast.*;
import parser.ast.decl.*;
import parser.ast.expr.*;
import parser.ast.stmt.*;

import java.util.stream.Collectors;

public class AstPrinter implements ASTVisitor<String> {
    public String print(ProgramNode program) {
        StringBuilder sb = new StringBuilder();
        for (DeclarationNode decl : program.declarations) {
            sb.append(decl.accept(this)).append("\n");
        }
        return sb.toString().trim();
    }

    @Override
    public String visit(FunctionDeclNode node) {
        String params = node.params.stream()
                .map(p -> p.type + " " + p.name)
                .collect(Collectors.joining(", "));
        String ret = node.returnType != null ? " -> " + node.returnType : "";
        return "(fn " + node.name + "(" + params + ")" + ret + " " + node.body.accept(this) + ")";
    }

    @Override
    public String visit(StructDeclNode node) {
        String fields = node.fields.stream()
                .map(f -> f.accept(this))
                .collect(Collectors.joining(" "));
        return "(struct " + node.name + " " + fields + ")";
    }

    @Override
    public String visit(VarDeclWrapper node) {
        return node.varDecl.accept(this);
    }

    @Override
    public String visit(BlockStmtNode node) {
        if (node.statements.isEmpty()) return "(block)";
        String body = node.statements.stream()
                .map(s -> s.accept(this))
                .collect(Collectors.joining(" "));
        return "(block " + body + ")";
    }
    @Override
    public String visit(BreakStmtNode node) {
        return "(break)";
    }

    @Override
    public String visit(ContinueStmtNode node) {
        return "(continue)";
    }

    @Override
    public String visit(VarDeclStmtNode node) {
        String init = node.initializer != null ? " " + node.initializer.accept(this) : "";
        return "(var " + node.type + " " + node.name + init + ")";
    }

    @Override
    public String visit(ExprStmtNode node) {
        return node.expression.accept(this);
    }

    @Override
    public String visit(IfStmtNode node) {
        String result = "(if " + node.condition.accept(this) + " " + node.thenBranch.accept(this);
        if (node.elseBranch != null) {
            result += " " + node.elseBranch.accept(this);
        }
        return result + ")";
    }

    @Override
    public String visit(WhileStmtNode node) {
        return "(while " + node.condition.accept(this) + " " + node.body.accept(this) + ")";
    }

    @Override
    public String visit(ForStmtNode node) {
        String init = node.initializer != null ? node.initializer.accept(this) : "_";
        String cond = node.condition != null ? node.condition.accept(this) : "_";
        String upd = node.update != null ? node.update.accept(this) : "_";
        return "(for " + init + " " + cond + " " + upd + " " + node.body.accept(this) + ")";
    }

    @Override
    public String visit(ReturnStmtNode node) {
        if (node.value != null) {
            return "(return " + node.value.accept(this) + ")";
        }
        return "(return)";
    }

    @Override
    public String visit(BinaryExprNode node) {
        return "(" + node.operator.lexeme + " " + node.left.accept(this) + " " + node.right.accept(this) + ")";
    }

    @Override
    public String visit(UnaryExprNode node) {
        return "(" + node.operator.lexeme + " " + node.operand.accept(this) + ")";
    }

    @Override
    public String visit(LiteralExprNode node) {
        if (node.value == null) return "null";
        if (node.literalType.equals("string")) return "\"" + node.value + "\"";
        return node.value.toString();
    }

    @Override
    public String visit(IdentifierExprNode node) {
        return node.name;
    }

    @Override
    public String visit(GroupingExprNode node) {
        return "(group " + node.expression.accept(this) + ")";
    }

    @Override
    public String visit(AssignmentExprNode node) {
        return "(" + node.operator.lexeme + " " + node.target.accept(this) + " " + node.value.accept(this) + ")";
    }

    @Override
    public String visit(CallExprNode node) {
        String args = node.arguments.stream()
                .map(a -> a.accept(this))
                .collect(Collectors.joining(" "));
        String calleeName = node.callee.accept(this);
        if (args.isEmpty()) return "(call " + calleeName + ")";
        return "(call " + calleeName + " " + args + ")";
    }

    @Override
    public String visit(ArrayIndexExprNode node) {
        return "(index " + node.arrayName + " " + node.index.accept(this) + ")";
    }

    @Override
    public String visit(StmtWrapper node) {
        return node.statement.accept(this);
    }
    @Override
    public String visit(DerefExprNode node) {
        if (node.index != null) {
            return "(deref " + node.pointer.accept(this)
                    + " [" + node.index.accept(this) + "])";
        }
        return "(deref " + node.pointer.accept(this) + ")";
    }
}