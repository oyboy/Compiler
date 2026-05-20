package semantic;

import parser.ast.*;
import parser.ast.decl.*;
import parser.ast.expr.*;
import parser.ast.stmt.*;
import utils.ASTVisitor;

import java.util.*;

public class SemanticAnalyzer implements ASTVisitor<Type> {
    private final SymbolTable symbolTable = new SymbolTable();
    private final List<SemanticError> errors = new ArrayList<>();

    private String currentFunctionName = null;
    private Type currentFunctionReturnType = null;
    private int loopDepth = 0;
    private final String filename;

    public SemanticAnalyzer() {
        this.filename = null;
    }

    public SemanticAnalyzer(String filename) {
        this.filename = filename;
    }

    public void analyze(ProgramNode program) {
        registerBuiltIns();
        for (DeclarationNode decl : program.declarations) {
            if (decl instanceof FunctionDeclNode) registerFunction((FunctionDeclNode) decl);
            if (decl instanceof StructDeclNode) registerStruct((StructDeclNode) decl);
        }
        for (DeclarationNode decl : program.declarations) {
            decl.accept(this);
        }
    }

    private void registerBuiltIns() {
        symbolTable.insert(new Symbol("malloc",
                new Type.FunctionType(List.of(Type.INT), Type.INT),
                Symbol.Kind.FUNCTION, 0, 0, true));

        symbolTable.insert(new Symbol("free",
                new Type.FunctionType(List.of(Type.INT), Type.VOID),
                Symbol.Kind.FUNCTION, 0, 0, true));

        symbolTable.insert(new Symbol("printf",
                new Type.FunctionType(List.of(Type.STRING), Type.INT),
                Symbol.Kind.FUNCTION, 0, 0, true));

        symbolTable.insert(new Symbol("scanf",
                new Type.FunctionType(List.of(Type.STRING), Type.INT),
                Symbol.Kind.FUNCTION, 0, 0, true)
        );
    }

    public List<SemanticError> getErrors() { return Collections.unmodifiableList(errors); }

    public SymbolTable getSymbolTable() { return symbolTable; }

    private void registerFunction(FunctionDeclNode node) {
        List<Type> paramTypes = new ArrayList<>();
        for (ParamNode p : node.params) {
            Type t = resolveType(p.type, p.line, p.column);
            paramTypes.add(t);
        }
        Type returnType = node.returnType != null
                ? resolveType(node.returnType, node.line, node.column)
                : Type.VOID;

        Type.FunctionType funcType = new Type.FunctionType(paramTypes, returnType);
        Symbol sym = new Symbol(node.name, funcType, Symbol.Kind.FUNCTION, node.line, node.column, true);

        if (!symbolTable.insert(sym)) {
            error(SemanticError.ErrorType.DUPLICATE_DECLARATION,
                    "Function '" + node.name + "' is already declared",
                    null, node.line, node.column);
        }
    }

    private void registerStruct(StructDeclNode node) {
        Map<String, Type> fields = new LinkedHashMap<>();
        for (VarDeclStmtNode field : node.fields) {
            Type fieldType = resolveType(field.type, field.line, field.column);
            if (fields.containsKey(field.name)) {
                error(SemanticError.ErrorType.DUPLICATE_DECLARATION,
                        "Duplicate field '" + field.name + "' in struct '" + node.name + "'",
                        "struct '" + node.name + "'", field.line, field.column);
            } else {
                fields.put(field.name, fieldType);
            }
        }
        Type.StructType structType = new Type.StructType(node.name, fields);
        Symbol sym = new Symbol(node.name, structType, Symbol.Kind.STRUCT, node.line, node.column, true);

        if (!symbolTable.insert(sym)) {
            error(SemanticError.ErrorType.DUPLICATE_DECLARATION,
                    "Struct '" + node.name + "' is already declared",
                    null, node.line, node.column);
        }
    }

    @Override
    public Type visit(FunctionDeclNode node) {
        String previousFunction = currentFunctionName;
        Type previousReturnType = currentFunctionReturnType;

        currentFunctionName = node.name;
        currentFunctionReturnType =
                node.returnType != null
                        ? resolveType(node.returnType, node.line, node.column)
                        : Type.VOID;

        symbolTable.enterScope();

        for (ParamNode p : node.params) {
            Type paramType = resolveType(p.type, p.line, p.column);
            Symbol paramSym = new Symbol(
                    p.name,
                    paramType,
                    Symbol.Kind.PARAMETER,
                    p.line,
                    p.column,
                    true
            );

            if (!symbolTable.insert(paramSym)) {
                error(
                        SemanticError.ErrorType.DUPLICATE_DECLARATION,
                        "Duplicate parameter '" + p.name + "'",
                        currentContext(),
                        p.line,
                        p.column
                );
            }
        }

        node.body.accept(this);

        symbolTable.exitScope();

        currentFunctionName = previousFunction;
        currentFunctionReturnType = previousReturnType;
        return null;
    }

    @Override
    public Type visit(StructDeclNode node) {
        return null;
    }

    @Override
    public Type visit(VarDeclWrapper node) {
        return node.varDecl.accept(this);
    }

    @Override
    public Type visit(BlockStmtNode node) {
        symbolTable.enterScope();
        for (StatementNode stmt : node.statements) {
            stmt.accept(this);
        }
        symbolTable.exitScope();
        return null;
    }

    @Override
    public Type visit(VarDeclStmtNode node) {
        Type declaredType = resolveType(node.type, node.line, node.column);

        if (node.size >= 0) {
            declaredType = new Type.ArrayType(declaredType, node.size);
        }

        if (symbolTable.lookupLocal(node.name).isPresent()) {
            error(SemanticError.ErrorType.DUPLICATE_DECLARATION,
                    "Variable '" + node.name + "' is already declared in this scope",
                    currentContext(), node.line, node.column);
            return null;
        }

        boolean initialized = node.initializer != null || node.size >= 0;

        if (node.initializer != null) {
            Type initType = node.initializer.accept(this);
            if (!initType.isError() && !declaredType.isCompatibleWith(initType)) {
                error(SemanticError.ErrorType.TYPE_MISMATCH,
                        "Cannot assign '" + initType + "' to variable of type '" + declaredType + "'",
                        currentContext(), node.initializer.line, node.initializer.column);
            }
        }

        Symbol sym = new Symbol(node.name, declaredType, Symbol.Kind.VARIABLE, node.line, node.column, initialized);
        symbolTable.insert(sym);
        return null;
    }

    @Override
    public Type visit(ExprStmtNode node) {
        node.expression.accept(this);
        return null;
    }

    @Override
    public Type visit(IfStmtNode node) {
        Type condType = node.condition.accept(this);
        checkCondition(condType, node.condition.line, node.condition.column);

        node.thenBranch.accept(this);
        if (node.elseBranch != null) node.elseBranch.accept(this);
        return null;
    }

    @Override
    public Type visit(WhileStmtNode node) {
        loopDepth++;
        Type condType = node.condition.accept(this);
        checkCondition(condType, node.condition.line, node.condition.column);

        node.body.accept(this);
        loopDepth--;
        return null;
    }
    @Override
    public Type visit(BreakStmtNode node) {
        if (loopDepth == 0) {
            error(SemanticError.ErrorType.INVALID_BREAK, "break outside of loop", null, node.line, node.column);
        }
        return null;
    }

    @Override
    public Type visit(ContinueStmtNode node) {
        if (loopDepth == 0) {
            error(SemanticError.ErrorType.INVALID_CONTINUE, "continue outside of loop", null, node.line, node.column);
        }
        return null;
    }

    @Override
    public Type visit(ForStmtNode node) {
        loopDepth++;
        symbolTable.enterScope();

        if (node.initializer != null) node.initializer.accept(this);

        if (node.condition != null) {
            Type condType = node.condition.accept(this);
            checkCondition(condType, node.condition.line, node.condition.column);
        }

        if (node.update != null) node.update.accept(this);

        node.body.accept(this);

        symbolTable.exitScope();
        loopDepth--;
        return null;
    }

    @Override
    public Type visit(ReturnStmtNode node) {
        if (currentFunctionReturnType == null) {
            error(SemanticError.ErrorType.INVALID_RETURN_TYPE,
                    "Return statement outside of function",
                    null, node.line, node.column);
            return null;
        }

        if (node.value == null) {
            if (currentFunctionReturnType != Type.VOID) {
                error(SemanticError.ErrorType.INVALID_RETURN_TYPE,
                        "Function '" + currentFunctionName + "' must return a value of type '"
                                + currentFunctionReturnType + "'",
                        currentContext(), node.line, node.column);
            }
        } else {
            Type returnedType = node.value.accept(this);
            if (!returnedType.isError() && !currentFunctionReturnType.isCompatibleWith(returnedType)) {
                error(SemanticError.ErrorType.INVALID_RETURN_TYPE,
                        "Cannot return '" + returnedType + "' from function '" + currentFunctionName
                                + "' declared to return '" + currentFunctionReturnType + "'",
                        currentContext(), node.line, node.column);
            }
        }
        return null;
    }

    @Override
    public Type visit(LiteralExprNode node) {
        Type type;
        switch (node.literalType) {
            case "int":    type = Type.INT; break;
            case "float":  type = Type.FLOAT; break;
            case "bool":   type = Type.BOOL; break;
            case "string": type = Type.STRING; break;
            default:       type = Type.ERROR; break;
        }
        node.resolvedType = type;
        return type;
    }

    @Override
    public Type visit(IdentifierExprNode node) {
        Optional<Symbol> sym = symbolTable.lookup(node.name);
        if (sym.isEmpty()) {
            error(SemanticError.ErrorType.UNDECLARED_IDENTIFIER,
                    "Undeclared identifier '" + node.name + "'",
                    currentContext(), node.line, node.column);
            node.resolvedType = Type.ERROR;
            return Type.ERROR;
        }

        Symbol s = sym.get();

        if (!s.initialized && s.kind == Symbol.Kind.VARIABLE) {
            error(SemanticError.ErrorType.USE_BEFORE_DECLARATION,
                    "Variable '" + node.name + "' may not be initialized",
                    currentContext(), node.line, node.column);
        }

        node.resolvedSymbol = s;
        node.resolvedType = s.type;
        return s.type;
    }

    @Override
    public Type visit(BinaryExprNode node) {
        Type left = node.left.accept(this);
        Type right = node.right.accept(this);

        if (left.isError() || right.isError()) {
            node.resolvedType = Type.ERROR;
            return Type.ERROR;
        }

        Type result = checkBinaryOp(node.operator.lexeme, left, right, node.line, node.column);
        node.resolvedType = result;
        return result;
    }

    @Override
    public Type visit(UnaryExprNode node) {
        Type operand = node.operand.accept(this);

        if (operand.isError()) {
            node.resolvedType = Type.ERROR;
            return Type.ERROR;
        }

        Type result;
        switch (node.operator.lexeme) {
            case "-":
                if (!operand.isNumeric()) {
                    error(SemanticError.ErrorType.TYPE_MISMATCH,
                            "Unary '-' requires numeric type, got '" + operand + "'",
                            currentContext(), node.line, node.column);
                    result = Type.ERROR;
                } else {
                    result = operand;
                }
                break;
            case "!":
                if (operand != Type.BOOL) {
                    error(SemanticError.ErrorType.TYPE_MISMATCH,
                            "Unary '!' requires bool, got '" + operand + "'",
                            currentContext(), node.line, node.column);
                    result = Type.ERROR;
                } else {
                    result = Type.BOOL;
                }
                break;
            default:
                result = Type.ERROR;
        }

        node.resolvedType = result;
        return result;
    }

    @Override
    public Type visit(GroupingExprNode node) {
        Type inner = node.expression.accept(this);
        node.resolvedType = inner;
        return inner;
    }

    @Override
    public Type visit(AssignmentExprNode node) {
        if (node.target instanceof IdentifierExprNode id) {
            symbolTable.lookup(id.name).ifPresent(s -> s.initialized = true);
        } else if (node.target instanceof ArrayIndexExprNode ai) {
            symbolTable.lookup(ai.arrayName).ifPresent(s -> s.initialized = true);
        }

        Type targetType = node.target.accept(this);
        Type valueType = node.value.accept(this);

        if (!(node.target instanceof IdentifierExprNode) && !(node.target instanceof ArrayIndexExprNode)) {
            error(SemanticError.ErrorType.INVALID_ASSIGNMENT_TARGET,
                    "Left side of assignment must be a variable or array element",
                    currentContext(), node.line, node.column);
            node.resolvedType = Type.ERROR;
            return Type.ERROR;
        }

        if (targetType.isError() || valueType.isError()) {
            node.resolvedType = Type.ERROR;
            return Type.ERROR;
        }

        if (!targetType.isCompatibleWith(valueType)) {
            error(SemanticError.ErrorType.TYPE_MISMATCH,
                    "Cannot assign '" + valueType + "' to '" + targetType + "'",
                    currentContext(), node.line, node.column);
            node.resolvedType = Type.ERROR;
            return Type.ERROR;
        }

        node.resolvedType = targetType;
        return targetType;
    }

    @Override
    public Type visit(CallExprNode node) {
        if (!(node.callee instanceof IdentifierExprNode)) {
            error(SemanticError.ErrorType.NOT_A_FUNCTION,
                    "Callee is not a function",
                    currentContext(), node.line, node.column);
            node.resolvedType = Type.ERROR;
            return Type.ERROR;
        }

        IdentifierExprNode callee = (IdentifierExprNode) node.callee;
        String funcName = callee.name;

        if (funcName.equals("print")) {
            if (node.arguments.size() != 1) {
                error(SemanticError.ErrorType.ARGUMENT_COUNT_MISMATCH, "print expects 1 argument", currentContext(), node.line, node.column);
                return Type.VOID;
            }

            Type argType = node.arguments.get(0).accept(this);

            if (argType == Type.INT) callee.name = "print_int";
            else if (argType == Type.BOOL) callee.name = "print_bool";
            else if (argType == Type.FLOAT) callee.name = "print_float";
            else if (argType == Type.STRING) callee.name = "print_string";
            else error(SemanticError.ErrorType.TYPE_MISMATCH, "Cannot print type: " + argType, currentContext(), node.line, node.column);

            node.resolvedType = Type.VOID;
            return Type.VOID;
        }

        if (funcName.equals("printf")) {
            for (ExpressionNode arg : node.arguments) {
                arg.accept(this);
            }
            node.resolvedType = Type.INT;
            return Type.INT;
        }

        if (funcName.equals("scanf")) {
            for (int i = 0; i < node.arguments.size(); i++) {
                ExpressionNode arg = node.arguments.get(i);
                if (i > 0 && arg instanceof IdentifierExprNode idNode) {
                    symbolTable.lookup(idNode.name).ifPresent(s -> s.initialized = true);
                }
                arg.accept(this);
            }
            node.resolvedType = Type.INT;
            return Type.INT;
        }

        Optional<Symbol> sym = symbolTable.lookup(funcName);
        if (sym.isEmpty()) {
            error(SemanticError.ErrorType.UNDECLARED_IDENTIFIER,
                    "Undeclared function '" + funcName + "'",
                    currentContext(), node.line, node.column);
            node.resolvedType = Type.ERROR;
            return Type.ERROR;
        }

        if (!(sym.get().type instanceof Type.FunctionType)) {
            error(SemanticError.ErrorType.NOT_A_FUNCTION,
                    "'" + funcName + "' is not a function",
                    currentContext(), node.line, node.column);
            node.resolvedType = Type.ERROR;
            return Type.ERROR;
        }

        Type.FunctionType funcType = (Type.FunctionType) sym.get().type;

        if (node.arguments.size() != funcType.paramTypes.size()) {
            error(SemanticError.ErrorType.ARGUMENT_COUNT_MISMATCH,
                    "Function '" + funcName + "' expects " + funcType.paramTypes.size()
                            + " arguments, got " + node.arguments.size(),
                    currentContext(), node.line, node.column);
            node.resolvedType = funcType.returnType;
            return funcType.returnType;
        }

        for (int i = 0; i < node.arguments.size(); i++) {
            Type argType = node.arguments.get(i).accept(this);
            Type paramType = funcType.paramTypes.get(i);
            if (!argType.isError() && !paramType.isCompatibleWith(argType)) {
                error(SemanticError.ErrorType.ARGUMENT_TYPE_MISMATCH,
                        "Argument " + (i + 1) + " of '" + funcName + "': expected '" + paramType
                                + "', got '" + argType + "'",
                        currentContext(), node.line, node.column);
            }
        }
        node.resolvedSymbol = sym.get();
        node.resolvedType = funcType.returnType;
        return funcType.returnType;
    }

    @Override
    public Type visit(ArrayIndexExprNode node) {
        Optional<Symbol> sym = symbolTable.lookup(node.arrayName);
        if (sym.isEmpty()) {
            error(SemanticError.ErrorType.UNDECLARED_IDENTIFIER, "Undeclared array '" + node.arrayName + "'", currentContext(), node.line, node.column);
            return Type.ERROR;
        }

        if (!(sym.get().type instanceof Type.ArrayType)) {
            error(SemanticError.ErrorType.TYPE_MISMATCH, "'" + node.arrayName + "' is not an array", currentContext(), node.line, node.column);
            return Type.ERROR;
        }

        Type indexType = node.index.accept(this);
        if (indexType != Type.INT) {
            error(SemanticError.ErrorType.TYPE_MISMATCH, "Array index must be int, got " + indexType, currentContext(), node.line, node.column);
        }

        Type.ArrayType at = (Type.ArrayType) sym.get().type;
        node.resolvedType = at.elementType;
        return at.elementType;
    }

    @Override
    public Type visit(StmtWrapper node) {
        return node.statement.accept(this);
    }

    private Type resolveType(String typeName, int line, int column) {
        Type primitive = Type.fromString(typeName);
        if (primitive != null) return primitive;

        Optional<Symbol> structSym = symbolTable.lookup(typeName);
        if (structSym.isPresent() && structSym.get().kind == Symbol.Kind.STRUCT) {
            return structSym.get().type;
        }

        error(SemanticError.ErrorType.UNDECLARED_IDENTIFIER,
                "Unknown type '" + typeName + "'",
                currentContext(), line, column);
        return Type.ERROR;
    }

    private Type checkBinaryOp(String op, Type left, Type right, int line, int column) {
        switch (op) {
            case "+": case "-": case "*": case "/": case "%":
                if (!left.isNumeric() || !right.isNumeric()) {
                    error(SemanticError.ErrorType.TYPE_MISMATCH,
                            "Operator '" + op + "' requires numeric operands, got '"
                                    + left + "' and '" + right + "'",
                            currentContext(), line, column);
                    return Type.ERROR;
                }
                return (left == Type.FLOAT || right == Type.FLOAT) ? Type.FLOAT : Type.INT;

            case "<": case "<=": case ">": case ">=":
                if (!left.isNumeric() || !right.isNumeric()) {
                    error(SemanticError.ErrorType.TYPE_MISMATCH,
                            "Operator '" + op + "' requires numeric operands",
                            currentContext(), line, column);
                    return Type.ERROR;
                }
                return Type.BOOL;

            case "==": case "!=":
                if (!left.isCompatibleWith(right) && !right.isCompatibleWith(left)) {
                    error(SemanticError.ErrorType.TYPE_MISMATCH,
                            "Cannot compare '" + left + "' with '" + right + "'",
                            currentContext(), line, column);
                    return Type.ERROR;
                }
                return Type.BOOL;

            case "&&": case "||":
                if (left != Type.BOOL || right != Type.BOOL) {
                    error(SemanticError.ErrorType.TYPE_MISMATCH,
                            "Operator '" + op + "' requires bool operands, got '"
                                    + left + "' and '" + right + "'",
                            currentContext(), line, column);
                    return Type.ERROR;
                }
                return Type.BOOL;

            default:
                return Type.ERROR;
        }
    }

    private void checkCondition(Type condType, int line, int column) {
        if (!condType.isError() && condType != Type.BOOL) {
            error(SemanticError.ErrorType.INVALID_CONDITION_TYPE,
                    "Condition must be of type 'bool', got '" + condType + "'",
                    currentContext(), line, column);
        }
    }

    private void error(SemanticError.ErrorType type, String message, String context, int line, int column) {
        errors.add(new SemanticError(type, message, filename, context, line, column));
    }

    private String currentContext() {
        if (currentFunctionName != null) return "in function '" + currentFunctionName + "'";
        return null;
    }
}