package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;

public class StatementCodeGenerator extends CodeGenerator {

    protected static String getStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        StringBuilder sam = new StringBuilder();

        // Stmt -> ;
        if (Tokenizer.check(f, ';')) {
            return sam.toString(); // Null statement
        }

        if (f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getStmt expects TokenType.WORD at beginning of statement",
                f.lineNo()
            );
        }

        // Stmt -> break;
        if (f.test("break")) {
            method.pushStatement(Statement.BREAK);
            sam.append(getBreakStmt(f, method));
        }
        // Stmt -> return Expr;
        else if (f.test("return")) {
            // TODO: ONLY 1 return STMT at the end, all other return STMT "jump" to that end
            method.pushStatement(Statement.RETURN);
            sam.append(getReturnStmt(f, method));
        }
        // Stmt -> if (Expr) Block else Block;
        else if (f.test("if")) {
            method.pushStatement(Statement.CONDITIONAL);
            sam.append(getIfStmt(f, method));
        }
        // Stmt -> while (Expr) Block;
        else if (f.test("while")) {
            method.pushStatement(Statement.LOOP);
            sam.append(getWhileStmt(f, method));
        }
        // Stmt -> Var = Expr;
        else {
            method.pushStatement(Statement.ASSIGN);
            sam.append(getVarStmt(f, method));
        }

        return sam.toString();
    }

    private static String getBreakStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        if (!Tokenizer.check(f, "break")) {
            throw new SyntaxErrorException(
                "break statement expects 'break'",
                f.lineNo()
            );
        }

        Label breakLabel = method.mostRecent(LabelType.BREAK);
        if (breakLabel == null) {
            throw new SyntaxErrorException(
                "break statement outside of a loop",
                f.lineNo()
            );
        }

        if (!Tokenizer.check(f, ';')) {
            throw new SyntaxErrorException(
                "getBreakStmt expects ';' at end of statement",
                f.lineNo()
            );
        }

        return Backend.jump(breakLabel.name);
    }

    private static String getReturnStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        if (!Tokenizer.check(f, "return")) {
            throw new SyntaxErrorException(
                "getReturnStmt expects 'return' at beginining",
                f.lineNo()
            );
        }

        StringBuilder sam = new StringBuilder();

        Expression expr = getExpr(f, method);

        // Type check
        if (!expr.type.isCompatibleWith(method.returnType)) {
            throw new TypeErrorException(
                "Return type mismatch: expected " +
                method.returnType +
                ", but got " +
                expr.type,
                f.lineNo()
            );
        }
        sam.append(expr.samCode);

        // Jump to clean up
        Label returnLabel = method.mostRecent(LabelType.RETURN);
        if (returnLabel == null) {
            throw new CompilerException(
                "getReturnStmt missing exit label",
                f.lineNo()
            );
        }
        sam.append(Backend.jump(returnLabel.name));

        if (!Tokenizer.check(f, ';')) {
            throw new SyntaxErrorException(
                "getReturnStmt expects ';' at end of statement",
                f.lineNo()
            );
        }
        return sam.toString();
    }

    private static String getIfStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        if (!Tokenizer.check(f, "if")) {
            throw new SyntaxErrorException(
                "if statement expects 'if' at beginining",
                f.lineNo()
            );
        }

        StringBuilder sam = new StringBuilder();

        // labels used
        Label stopStmt = new Label();
        Label falseBlock = new Label();

        // if ( Expr ) ...
        if (!Tokenizer.check(f, '(')) {
            throw new SyntaxErrorException(
                "if statement expects '(' at beginining of condition",
                f.lineNo()
            );
        }

        sam.append(getExpr(f, method).samCode);

        if (!Tokenizer.check(f, ')')) {
            throw new SyntaxErrorException(
                "if statement expects ')' at end of condition",
                f.lineNo()
            );
        }

        // Generate condition check and jump
        sam
            .append(Backend.isNil())
            .append(Backend.jumpConditional(falseBlock.name));

        // Truth block
        sam.append(getBlock(f, method)).append(Backend.jump(stopStmt.name));

        // Checks 'else'
        if (!Tokenizer.getWord(f).equals("else")) {
            throw new SyntaxErrorException(
                "if statement expects 'else' between expressions",
                f.lineNo()
            );
        }

        // False block
        sam.append(Backend.label(falseBlock.name)).append(getBlock(f, method));

        // Done if statement
        sam.append(Backend.label(stopStmt.name));

        return sam.toString();
    }

    private static String getWhileStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        if (!Tokenizer.check(f, "while")) {
            throw new SyntaxErrorException(
                "while statement expects 'while' at beginining",
                f.lineNo()
            );
        }

        StringBuilder sam = new StringBuilder();

        // labels used
        Label startLoop = new Label();
        Label stopLoop = new Label(LabelType.BREAK);

        // Push exit label to use for break statement
        method.pushLabel(stopLoop);

        // while ( Expr ) ...
        if (!Tokenizer.check(f, '(')) {
            throw new SyntaxErrorException(
                "while statement expects '(' at beginining of condition",
                f.lineNo()
            );
        }

        sam
            .append(Backend.label(startLoop.name))
            .append(getExpr(f, method).samCode);

        if (!Tokenizer.check(f, ')')) {
            throw new SyntaxErrorException(
                "while statement expects ')' at end of condition",
                f.lineNo()
            );
        }

        sam
            .append(Backend.isNil())
            .append(Backend.jumpConditional(stopLoop.name));

        // Continue loop
        sam.append(getBlock(f, method)).append(Backend.jump(startLoop.name));

        // Stop loop
        sam.append(Backend.label(stopLoop.name));

        // Pop label when done
        method.popLabel();

        return sam.toString();
    }

    private static String getVarStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        StringBuilder sam = new StringBuilder();
        VariableSymbol variable = getVar(f, method);

        if (!Tokenizer.check(f, '=')) {
            throw new SyntaxErrorException(
                "getStmt expects '=' after variable",
                f.lineNo()
            );
        }

        Expression expr = getExpr(f, method);
        // Type check
        if (!variable.type.isCompatibleWith(expr.type)) {
            System.out.println(expr.type);
            throw new TypeErrorException(
                "getVarStmt type mismatch: " +
                variable.type +
                " and " +
                expr.type,
                f.lineNo()
            );
        }

        // Store item on the stack to VariableSymbol
        if (variable.isInstanceVariable()) {
            sam
                .append(Backend.pushOffset(method.getThisAddress()))
                .append(Backend.pushImmediate(variable.address))
                .append(Backend.add())
                .append(expr.samCode)
                .append(Backend.storeIndirect());
        } else {
            sam
                .append(expr.samCode)
                .append(Backend.storeOffset(variable.address));
        }

        if (!Tokenizer.check(f, ';')) {
            throw new SyntaxErrorException(
                "getStmt expects ';' at end of statement",
                f.lineNo()
            );
        }

        return sam.toString();
    }

    private static VariableSymbol getVar(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        // Not a var, raise
        if (f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getVar should starts with a WORD",
                f.lineNo()
            );
        }

        String varName = Tokenizer.getWord(f);
        VariableSymbol variable = method.lookupSymbol(
            varName,
            VariableSymbol.class
        );
        if (variable == null) {
            throw new SyntaxErrorException(
                new StringBuilder()
                    .append(
                        "getVar trying to access variable that has not been declared: Variable"
                    )
                    .append(varName)
                    .toString(),
                f.lineNo()
            );
        }
        return variable;
    }
}
