package ir;

import parser.ast.*;
import parser.ast.decl.*;
import parser.ast.expr.*;
import parser.ast.stmt.*;
import semantic.Type;
import utils.ASTVisitor;

import java.util.*;

public class IRGenerator implements ASTVisitor<Operand> {
    private final IRProgram program = new IRProgram();
    private IRFunction currentFunction;
    private BasicBlock currentBlock;
    private int tempCounter = 0;
    private int labelCounter = 0;
    private final Set<String> usedLabels = new HashSet<>();
    private final Deque<String> breakLabels = new ArrayDeque<>();
    private final Deque<String> continueLabels = new ArrayDeque<>();
    private int stringCounter = 0;

    public IRProgram generate(ProgramNode program) {
        for (DeclarationNode decl : program.declarations) {
            decl.accept(this);
        }
        return this.program;
    }

    public IRProgram getProgram() { return program; }

    private Operand.Temporary newTemp() {
        return new Operand.Temporary(tempCounter++);
    }

    private String newLabel(String name) {
        if (usedLabels.contains(name)) {
            String unique = name + "_" + (labelCounter++);
            usedLabels.add(unique);
            return unique;
        }
        usedLabels.add(name);
        return name;
    }

    private void emit(Instruction instr) {
        if (currentBlock != null) {
            currentBlock.addInstruction(instr);
        }
    }

    private BasicBlock startBlock(String label) {
        BasicBlock block = new BasicBlock(label);
        currentFunction.addBlock(block);
        currentBlock = block;
        return block;
    }

    private void connectBlocks(BasicBlock from, BasicBlock to) {
        from.addSuccessor(to);
    }

    private void jumpIfNotTerminated(BasicBlock block, String label) {
        if (!block.isTerminated()) {
            block.addInstruction(new Instruction.Jump(label));
        }
    }

    @Override
    public Operand visit(FunctionDeclNode node) {
        String returnType = node.returnType != null ? node.returnType : "void";
        currentFunction = new IRFunction(node.name, returnType);
        program.addFunction(currentFunction);

        tempCounter = 0;
        labelCounter = 0;
        usedLabels.clear();
        usedLabels.add("entry");

        startBlock("entry");

        for (ParamNode param : node.params) {
            currentFunction.paramNames.add(param.type + " " + param.name);
            currentFunction.variables.put(param.name, new Operand.Variable(param.name));
        }

        node.body.accept(this);

        if (!currentBlock.isTerminated()) {
            emit(new Instruction.Return(null));
        }

        currentFunction = null;
        currentBlock = null;
        return null;
    }

    @Override
    public Operand visit(StructDeclNode node) { return null; }

    @Override
    public Operand visit(VarDeclWrapper node) { return null; }

    @Override
    public Operand visit(BlockStmtNode node) {
        for (StatementNode stmt : node.statements) {
            stmt.accept(this);
            if (currentBlock != null && currentBlock.isTerminated()) break;
        }
        return null;
    }

    @Override
    public Operand visit(VarDeclStmtNode n) {
        if (n.size >= 0) {
            currentFunction.variables.put(n.name + "$size$" + n.size, new Operand.Variable(n.name));
            return null;
        }
        currentFunction.variables.put(n.name, new Operand.Variable(n.name));
        if (n.initializer != null) {
            Operand val = n.initializer.accept(this);
            emit(new Instruction.Store(new Operand.Variable(n.name), val));
        }
        return null;
    }

    @Override
    public Operand visit(ExprStmtNode node) {
        if (currentBlock == null) return null;
        node.expression.accept(this);
        return null;
    }

    @Override
    public Operand visit(IfStmtNode node) {
        boolean hasElse = node.elseBranch != null;

        String thenLabel = newLabel("L_true");
        String elseLabel = hasElse ? newLabel("L_false") : newLabel("L_endif");
        String mergeLabel = hasElse ? newLabel("L_endif") : elseLabel;

        Operand cond = node.condition.accept(this);
        BasicBlock condBlock = currentBlock;

        emit(new Instruction.JumpIf(cond, thenLabel));
        emit(new Instruction.Jump(elseLabel));

        BasicBlock thenBlock = startBlock(thenLabel);
        connectBlocks(condBlock, thenBlock);
        node.thenBranch.accept(this);
        BasicBlock afterThen = currentBlock;
        boolean thenTerminated = afterThen.isTerminated();

        if (hasElse) {
            BasicBlock elseBlock = startBlock(elseLabel);
            connectBlocks(condBlock, elseBlock);
            node.elseBranch.accept(this);
            BasicBlock afterElse = currentBlock;
            boolean elseTerminated = afterElse.isTerminated();

            if (thenTerminated && elseTerminated) {
                return null;
            }

            if (!thenTerminated) jumpIfNotTerminated(afterThen, mergeLabel);
            if (!elseTerminated) jumpIfNotTerminated(afterElse, mergeLabel);

            BasicBlock mergeBlock = startBlock(mergeLabel);
            if (!thenTerminated) connectBlocks(afterThen, mergeBlock);
            if (!elseTerminated) connectBlocks(afterElse, mergeBlock);
        } else {
            if (!thenTerminated) {
                jumpIfNotTerminated(afterThen, mergeLabel);
            }

            BasicBlock endifBlock = startBlock(mergeLabel);
            connectBlocks(condBlock, endifBlock);
            if (!thenTerminated) connectBlocks(afterThen, endifBlock);
        }

        return null;
    }

    @Override
    public Operand visit(WhileStmtNode node) {
        String headerLabel = newLabel("L_loop");
        String bodyLabel = newLabel("L_body");
        String exitLabel = newLabel("L_endloop");
        breakLabels.push(exitLabel);
        continueLabels.push(headerLabel);

        BasicBlock beforeLoop = currentBlock;
        emit(new Instruction.Jump(headerLabel));

        BasicBlock headerBlock = startBlock(headerLabel);
        connectBlocks(beforeLoop, headerBlock);

        Operand cond = node.condition.accept(this);
        emit(new Instruction.JumpIf(cond, bodyLabel));
        emit(new Instruction.Jump(exitLabel));

        BasicBlock bodyBlock = startBlock(bodyLabel);
        connectBlocks(headerBlock, bodyBlock);
        node.body.accept(this);

        BasicBlock afterBody = currentBlock;
        jumpIfNotTerminated(afterBody, headerLabel);
        connectBlocks(afterBody, headerBlock);

        BasicBlock exitBlock = startBlock(exitLabel);
        connectBlocks(headerBlock, exitBlock);

        breakLabels.pop();
        continueLabels.pop();
        return null;
    }
    @Override
    public Operand visit(BreakStmtNode node) {
        emit(new Instruction.Jump(breakLabels.peek()));
        return null;
    }

    @Override
    public Operand visit(ContinueStmtNode node) {
        emit(new Instruction.Jump(continueLabels.peek()));
        return null;
    }

    @Override
    public Operand visit(ForStmtNode node) {
        if (node.initializer != null) node.initializer.accept(this);

        String headerLabel = newLabel("L_for");
        String bodyLabel = newLabel("L_forbody");
        String updateLabel = newLabel("L_for_update");
        String exitLabel = newLabel("L_endfor");

        breakLabels.push(exitLabel);
        continueLabels.push(updateLabel);

        BasicBlock beforeLoop = currentBlock;
        emit(new Instruction.Jump(headerLabel));

        BasicBlock headerBlock = startBlock(headerLabel);
        connectBlocks(beforeLoop, headerBlock);

        if (node.condition != null) {
            Operand cond = node.condition.accept(this);
            emit(new Instruction.JumpIf(cond, bodyLabel));
            emit(new Instruction.Jump(exitLabel));
        } else {
            emit(new Instruction.Jump(bodyLabel));
        }

        BasicBlock bodyBlock = startBlock(bodyLabel);
        connectBlocks(headerBlock, bodyBlock);
        node.body.accept(this);

        if (node.update != null) node.update.accept(this);

        BasicBlock afterBody = currentBlock;
        jumpIfNotTerminated(afterBody, headerLabel);
        connectBlocks(afterBody, headerBlock);

        BasicBlock exitBlock = startBlock(exitLabel);
        connectBlocks(headerBlock, exitBlock);
        breakLabels.pop();
        continueLabels.pop();
        return null;
    }

    @Override
    public Operand visit(ReturnStmtNode node) {
        if (node.value != null) {
            Operand value = node.value.accept(this);
            emit(new Instruction.Return(value));
        } else {
            emit(new Instruction.Return(null));
        }
        return null;
    }

    @Override
    public Operand visit(LiteralExprNode node) {
        return switch (node.literalType) {
            case "string" -> new Operand.StringLiteral((String) node.value, "L_str_" + (stringCounter++));
            case "int" -> new Operand.IntLiteral(((Number) node.value).intValue());
            case "float" -> new Operand.FloatLiteral(((Number) node.value).doubleValue());
            case "bool" -> new Operand.BoolLiteral((Boolean) node.value);
            default -> new Operand.Variable("\"" + node.value + "\"");
        };
    }

    @Override
    public Operand visit(IdentifierExprNode node) {
        if (isParameter(node.name)) return new Operand.Parameter(node.name);
        Operand.Variable var = new Operand.Variable(node.name);
        Operand.Temporary dest = newTemp();
        emit(new Instruction.Load(dest, var));
        return dest;
    }

    private boolean isParameter(String name) {
        if (currentFunction == null) return false;
        for (String param : currentFunction.paramNames) {
            String paramName = param.substring(param.lastIndexOf(' ') + 1);
            if (paramName.equals(name)) return true;
        }
        return false;
    }

    @Override
    public Operand visit(BinaryExprNode node) {
        String op = node.operator.lexeme;

        if (op.equals("&&")) {
            String rightLabel = newLabel("and_right");
            String endLabel = newLabel("and_end");
            Operand.Temporary result = newTemp();

            Operand left = node.left.accept(this);
            emit(new Instruction.JumpIf(left, rightLabel));
            emit(new Instruction.Move(result, new Operand.BoolLiteral(false)));
            emit(new Instruction.Jump(endLabel));

            startBlock(rightLabel);
            Operand right = node.right.accept(this);
            emit(new Instruction.Move(result, right));
            emit(new Instruction.Jump(endLabel));

            startBlock(endLabel);
            return result;
        }

        if (op.equals("||")) {
            String trueLabel = newLabel("or_true");
            String rightLabel = newLabel("or_right");
            String endLabel = newLabel("or_end");
            Operand.Temporary result = newTemp();

            Operand left = node.left.accept(this);
            emit(new Instruction.JumpIf(left, trueLabel));

            startBlock(rightLabel);
            Operand right = node.right.accept(this);
            emit(new Instruction.Move(result, right));
            emit(new Instruction.Jump(endLabel));

            startBlock(trueLabel);
            emit(new Instruction.Move(result, new Operand.BoolLiteral(true)));
            emit(new Instruction.Jump(endLabel));

            startBlock(endLabel);
            return result;
        }

        Operand left = node.left.accept(this);
        Operand right = node.right.accept(this);

        Instruction.BinaryOp.Op irOp = mapToIrOp(op);
        Instruction.Compare.Op cmpOp = mapToCmpOp(op);
        if (irOp != null) {
            Operand folded = IROptimizer.foldBinary(irOp, left, right);
            if (folded != null) return folded;
        } else if (cmpOp != null) {
            Operand folded = IROptimizer.foldCompare(cmpOp, left, right);
            if (folded != null) return folded;
        }

        Operand.Temporary dest = newTemp();
        switch (node.operator.lexeme) {
            case "+": emit(new Instruction.BinaryOp(dest, Instruction.BinaryOp.Op.ADD, left, right)); break;
            case "-": emit(new Instruction.BinaryOp(dest, Instruction.BinaryOp.Op.SUB, left, right)); break;
            case "*": emit(new Instruction.BinaryOp(dest, Instruction.BinaryOp.Op.MUL, left, right)); break;
            case "/": emit(new Instruction.BinaryOp(dest, Instruction.BinaryOp.Op.DIV, left, right)); break;
            case "%": emit(new Instruction.BinaryOp(dest, Instruction.BinaryOp.Op.MOD, left, right)); break;
            case "==": emit(new Instruction.Compare(dest, Instruction.Compare.Op.EQ, left, right)); break;
            case "!=": emit(new Instruction.Compare(dest, Instruction.Compare.Op.NE, left, right)); break;
            case "<": emit(new Instruction.Compare(dest, Instruction.Compare.Op.LT, left, right)); break;
            case "<=": emit(new Instruction.Compare(dest, Instruction.Compare.Op.LE, left, right)); break;
            case ">": emit(new Instruction.Compare(dest, Instruction.Compare.Op.GT, left, right)); break;
            case ">=": emit(new Instruction.Compare(dest, Instruction.Compare.Op.GE, left, right)); break;
            default: emit(new Instruction.Move(dest, left));
        }
        return dest;
    }

    private Instruction.BinaryOp.Op mapToIrOp(String op) {
        return switch (op) {
            case "+" -> Instruction.BinaryOp.Op.ADD;
            case "-" -> Instruction.BinaryOp.Op.SUB;
            case "*" -> Instruction.BinaryOp.Op.MUL;
            case "/" -> Instruction.BinaryOp.Op.DIV;
            case "%" -> Instruction.BinaryOp.Op.MOD;
            case "&&" -> Instruction.BinaryOp.Op.AND;
            case "||" -> Instruction.BinaryOp.Op.OR;
            default -> null;
        };
    }
    private Instruction.Compare.Op mapToCmpOp(String op) {
        return switch (op) {
            case "==" -> Instruction.Compare.Op.EQ;
            case "!=" -> Instruction.Compare.Op.NE;
            case "<"  -> Instruction.Compare.Op.LT;
            case "<=" -> Instruction.Compare.Op.LE;
            case ">"  -> Instruction.Compare.Op.GT;
            case ">=" -> Instruction.Compare.Op.GE;
            default -> null;
        };
    }

    @Override
    public Operand visit(UnaryExprNode node) {
        Operand operand = node.operand.accept(this);
        Operand.Temporary dest = newTemp();

        switch (node.operator.lexeme) {
            case "-": emit(new Instruction.UnaryOp(dest, Instruction.UnaryOp.Op.NEG, operand)); break;
            case "!": emit(new Instruction.UnaryOp(dest, Instruction.UnaryOp.Op.NOT, operand)); break;
        }
        return dest;
    }

    @Override
    public Operand visit(GroupingExprNode node) {
        return node.expression.accept(this);
    }

    @Override
    public Operand visit(AssignmentExprNode node) {
        Operand value = node.value.accept(this);
        if (node.target instanceof IdentifierExprNode) {
            String varName = ((IdentifierExprNode) node.target).name;
            emit(new Instruction.Store(new Operand.Variable(varName), value));
        } else if (node.target instanceof ArrayIndexExprNode arrayIdx) {
            Operand index = arrayIdx.index.accept(this);
            emit(new Instruction.StoreIndex(arrayIdx.arrayName, index, value));
        }
        return value;
    }

    @Override
    public Operand visit(StmtWrapper node) {
        return node.statement.accept(this);
    }

    @Override
    public Operand visit(CallExprNode node) {
        String funcName = ((IdentifierExprNode) node.callee).name;
        List<Operand> args = new ArrayList<>();
        for (int i = 0; i < node.arguments.size(); i++) {
            if (funcName.equals("scanf") && i > 0 && node.arguments.get(i) instanceof IdentifierExprNode id) {
                args.add(new Operand.Variable(id.name));
            } else {
                args.add(node.arguments.get(i).accept(this));
            }
        }

        for (int i = 0; i < args.size(); i++) {
            emit(new Instruction.Param(i, args.get(i), funcName));
        }

        Operand.Temporary dest = (node.resolvedType == null || node.resolvedType == semantic.Type.VOID) ? null : newTemp();
        emit(new Instruction.Call(dest, funcName, args.size()));
        return dest;
    }

    @Override
    public Operand visit(ArrayIndexExprNode node) {
        Operand index = node.index.accept(this);
        Operand.Temporary dest = newTemp();

        emit(new Instruction.LoadIndex(dest, node.arrayName, index));
        return dest;
    }
}