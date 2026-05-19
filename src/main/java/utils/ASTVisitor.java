package utils;

import parser.ast.expr.*;
import parser.ast.stmt.*;
import parser.ast.decl.*;

public interface ASTVisitor<R> {
    R visit(LiteralExprNode node);
    R visit(IdentifierExprNode node);
    R visit(BinaryExprNode node);
    R visit(UnaryExprNode node);
    R visit(GroupingExprNode node);
    R visit(AssignmentExprNode node);
    R visit(CallExprNode node);

    R visit(BlockStmtNode node);
    R visit(ExprStmtNode node);
    R visit(IfStmtNode node);
    R visit(WhileStmtNode node);
    R visit(ForStmtNode node);
    R visit(ReturnStmtNode node);
    R visit(VarDeclStmtNode node);
    R visit(BreakStmtNode node);
    R visit(ContinueStmtNode node);

    R visit(FunctionDeclNode node);
    R visit(StructDeclNode node);
    R visit(VarDeclWrapper node);
}
