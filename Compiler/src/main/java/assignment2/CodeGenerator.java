package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeGenerator {

    static String getProgram(SamTokenizer f) throws CompilerException {
        /*** Data segment
         **/
        String sam = DataGenerator.generateStaticData();

        /*** Code segment
         ***/

        // Get main class and main method from symbol table
        ClassSymbol mainClass = LiveOak3Compiler.globalSymbol.lookupSymbol(
            "Main",
            ClassSymbol.class
        );
        MethodSymbol mainMethod = mainClass.lookupSymbol(
            "main",
            MethodSymbol.class
        );

        // program return value
        sam += "PUSHIMM 0\n";

        // instanciate main class to pass in as "this" parameter
        sam += "PUSHIMM 1\n";
        sam += "MALLOC\n";
        // assign main class's vtable
        sam += "DUP\n";
        sam += "PUSHABS " + mainClass.vtableAddress + "\n";
        sam += "STOREIND\n";

        // run main method
        sam += "LINK\n";
        sam += "JSR " + mainMethod.getLabelName() + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -" + mainMethod.numParameters() + "\n";
        sam += DataGenerator.freeStaticData();
        sam += "STOP\n";

        // LiveOak-3
        while (f.peekAtKind() != TokenType.EOF) {
            sam += getClassDecl(f);
        }
        return sam;
    }

    static String getClassDecl(SamTokenizer f) throws CompilerException {
        // Generate sam code
        String sam = "";

        // ClassDecl -> class...
        if (!CompilerUtils.check(f, "class")) {
            throw new SyntaxErrorException(
                "populateClass expects 'class' at the start",
                f.lineNo()
            );
        }

        // ClassDecl -> class ClassName ...
        String className = Helpers.getIdentifier(f);

        // Pull class from global scope
        ClassSymbol classSymbol = LiveOak3Compiler.globalSymbol.lookupSymbol(
            className,
            ClassSymbol.class
        );
        if (classSymbol == null) {
            throw new CompilerException(
                "get class cannot find class " + className + " in symbol table",
                f.lineNo()
            );
        }

        // ClassDecl -> class ClassName (...
        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "populateClass expects '(' at start of get formals",
                f.lineNo()
            );
        }
        // ClassDecl -> class className ( Formals? ) ...
        while (!CompilerUtils.check(f, ')')) {
            CompilerUtils.skipToken(f);
        }

        // ClassDecl -> class ClassName ( Formals? ) {...
        if (!CompilerUtils.check(f, '{')) {
            throw new SyntaxErrorException(
                "populateClass expects '{' at start of get class body",
                f.lineNo()
            );
        }

        // ClassDecl -> class ClassName ( Formals? ) { MethodDecl...
        while (f.peekAtKind() == TokenType.WORD) {
            sam += getMethodDecl(f, classSymbol);
        }

        // ClassDecl -> class ClassName ( Formals? ) { MethodDecl...}
        if (!CompilerUtils.check(f, '}')) {
            throw new SyntaxErrorException(
                "populateClass expects '}' at end of get class body",
                f.lineNo()
            );
        }

        return sam;
    }

    static String getMethodDecl(SamTokenizer f, ClassSymbol classSymbol)
        throws CompilerException {
        // Generate sam code
        String sam = "\n";

        // MethodDecl -> Type ...
        Type returnType = getType(f);

        // MethodDecl -> Type MethodName ...
        String methodName = Helpers.getIdentifier(f);

        // Pull method from class scope
        MethodSymbol method = classSymbol.lookupSymbol(
            methodName,
            MethodSymbol.class
        );
        if (method == null) {
            throw new CompilerException(
                "get method cannot find method " +
                methodName +
                " in symbol table",
                f.lineNo()
            );
        }

        // Valid method, start generating...
        sam += method.getLabelName() + ":\n";

        // MethodDecl -> Type MethodName (...
        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "get method expects '(' at start of get formals",
                f.lineNo()
            );
        }

        // MethodDecl -> Type MethodName ( Formals? ) ...
        while (f.peekAtKind() == TokenType.WORD) {
            // Formals -> Type ...
            Type formalType = getType(f);

            // Formals -> Type Identifier
            String formalName = Helpers.getIdentifier(f);

            if (!CompilerUtils.check(f, ',')) {
                break;
            }
        }
        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "get method expects ')' at end of get formals",
                f.lineNo()
            );
        }

        // MethodDecl -> Type MethodName ( Formals? ) { ...
        if (!CompilerUtils.check(f, '{')) {
            throw new SyntaxErrorException(
                "get method expects '{' at start of body",
                f.lineNo()
            );
        }

        // MethodDecl -> Type MethodName ( Formals? ) { Body ...
        sam += getBody(f, method);

        // MethodDecl -> Type MethodName ( Formals? ) { Body }
        if (!CompilerUtils.check(f, '}')) {
            throw new SyntaxErrorException(
                "get method expects '}' at end of body",
                f.lineNo()
            );
        }

        // Check if any return method
        // if (!method.hasStatement(Statement.RETURN)) {
        //     throw new SyntaxErrorException(
        //         "get method missing return statement",
        //         f.lineNo()
        //     );
        // }

        return sam;
    }

    /*** Recursive operations. Override all
     ***/
    static String getBody(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        String sam = "";

        // while start with "int | bool | String"
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl will store variable in Hashmap: identifier -> { type: TokenType, relative_address: int }
            sam += getVarDecl(f, method);
        }

        // check EOF
        if (f.peekAtKind() == TokenType.EOF) {
            return sam;
        }

        Label returnLabel = new Label(LabelType.RETURN);
        method.pushLabel(returnLabel);

        // Then, get Block
        if (!CompilerUtils.check(f, '{')) {
            throw new SyntaxErrorException(
                "getBlock expects '{' at start of block",
                f.lineNo()
            );
        }
        List<Integer> endWithReturnStmt = new ArrayList<>();
        while (!CompilerUtils.check(f, '}')) {
            if (f.test("return")) {
                endWithReturnStmt.add(1);
            } else {
                endWithReturnStmt.add(0);
            }

            sam += getStmt(f, method);
        }

        // if method is constructor, always return the "this" object
        if (method.isConstructor()) {
            // push "this" on to stack for return
            sam += "PUSHOFF " + method.getThisAddress() + "\n";
        }
        // else, check for return statement as usual
        else if (
            method.returnType != Type.VOID &&
            endWithReturnStmt.get(endWithReturnStmt.size() - 1) != 1
        ) {
            throw new SyntaxErrorException(
                "get method missing return statement at the end",
                f.lineNo()
            );
        }

        // Cleanup procedure
        sam += returnLabel.name + ":\n";
        sam += "STOREOFF " + method.returnAddress() + "\n";
        sam += "ADDSP -" + method.numLocalVariables() + "\n";
        sam += "RST\n";
        method.popLabel();

        return sam;
    }

    static String getVarDecl(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        String sam = "";

        // VarDecl -> Type ...
        Type varType = getType(f);

        // while varName = a | b | c | ...
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl -> Type Identifier1, Identifier2
            String varName = Helpers.getIdentifier(f);

            // write sam code
            sam += "PUSHIMM 0\n";

            if (CompilerUtils.check(f, ',')) {
                continue;
            } else if (CompilerUtils.check(f, ';')) {
                break;
            } else {
                throw new SyntaxErrorException(
                    "Expected ',' or `;` after each variable declaration",
                    f.lineNo()
                );
            }
        }

        return sam;
    }

    static String getBlock(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        String sam = "";

        if (!CompilerUtils.check(f, '{')) {
            throw new SyntaxErrorException(
                "getBlock expects '{' at start of block",
                f.lineNo()
            );
        }

        while (!CompilerUtils.check(f, '}')) {
            sam += getStmt(f, method);
        }

        return sam;
    }

    static String getStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        String sam = "";

        // Stmt -> ;
        if (CompilerUtils.check(f, ';')) {
            return sam; // Null statement
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
            sam += getBreakStmt(f, method);
        }
        // Stmt -> return Expr;
        else if (f.test("return")) {
            // TODO: ONLY 1 return STMT at the end, all other return STMT "jump" to that end
            method.pushStatement(Statement.RETURN);
            sam += getReturnStmt(f, method);
            // Stmt -> if (Expr) Block else Block;
        } else if (f.test("if")) {
            method.pushStatement(Statement.CONDITIONAL);
            sam += getIfStmt(f, method);
            // Stmt -> while (Expr) Block;
        } else if (f.test("while")) {
            method.pushStatement(Statement.LOOP);
            sam += getWhileStmt(f, method);
            // Stmt -> Var = Expr;
        } else {
            method.pushStatement(Statement.ASSIGN);
            sam += getVarStmt(f, method);
        }

        return sam;
    }

    static String getBreakStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        if (!CompilerUtils.check(f, "break")) {
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

        if (!CompilerUtils.check(f, ';')) {
            throw new SyntaxErrorException(
                "getBreakStmt expects ';' at end of statement",
                f.lineNo()
            );
        }
        return "JUMP " + breakLabel.name + "\n";
    }

    static String getReturnStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        if (!CompilerUtils.check(f, "return")) {
            throw new SyntaxErrorException(
                "getReturnStmt expects 'return' at beginining",
                f.lineNo()
            );
        }

        String sam = "";

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
        sam += expr.samCode;

        // Jump to clean up
        Label returnLabel = method.mostRecent(LabelType.RETURN);
        if (returnLabel == null) {
            throw new CompilerException(
                "getReturnStmt missing exit label",
                f.lineNo()
            );
        }
        sam += "JUMP " + returnLabel.name + "\n";

        if (!CompilerUtils.check(f, ';')) {
            throw new SyntaxErrorException(
                "getReturnStmt expects ';' at end of statement",
                f.lineNo()
            );
        }
        return sam;
    }

    static String getIfStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        if (!CompilerUtils.check(f, "if")) {
            throw new SyntaxErrorException(
                "if statement expects 'if' at beginining",
                f.lineNo()
            );
        }

        // Generate sam code
        String sam = "";

        // labels used
        Label stop_stmt = new Label();
        Label false_block = new Label();

        // if ( Expr ) ...
        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "if statement expects '(' at beginining of condition",
                f.lineNo()
            );
        }

        sam += getExpr(f, method).samCode;

        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "if statement expects ')' at end of condition",
                f.lineNo()
            );
        }

        sam += "ISNIL\n";
        sam += "JUMPC " + false_block.name + "\n";

        // Truth block:  // if ( Expr ) Block ...
        sam += getBlock(f, method);
        sam += "JUMP " + stop_stmt.name + "\n";

        // Checks 'else'
        if (!CompilerUtils.getWord(f).equals("else")) {
            throw new SyntaxErrorException(
                "if statement expects 'else' between expressions",
                f.lineNo()
            );
        }

        // False block: (...) ? (...) : Expr
        sam += false_block.name + ":\n";
        sam += getBlock(f, method);

        // Done if statement
        sam += stop_stmt.name + ":\n";

        return sam;
    }

    static String getWhileStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        if (!CompilerUtils.check(f, "while")) {
            throw new SyntaxErrorException(
                "while statement expects 'while' at beginining",
                f.lineNo()
            );
        }

        // Generate sam code
        String sam = "";

        // labels used
        Label start_loop = new Label();
        Label stop_loop = new Label(LabelType.BREAK);

        // Push exit label to use for break statement
        method.pushLabel(stop_loop);

        // while ( Expr ) ...
        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "while statement expects '(' at beginining of condition",
                f.lineNo()
            );
        }

        sam += start_loop.name + ":\n";
        sam += getExpr(f, method).samCode;

        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "while statement expects ')' at end of condition",
                f.lineNo()
            );
        }

        sam += "ISNIL\n";
        sam += "JUMPC " + stop_loop.name + "\n";

        // Continue loop
        sam += getBlock(f, method);
        sam += "JUMP " + start_loop.name + "\n";

        // Stop loop
        sam += stop_loop.name + ":\n";

        // Pop label when done
        method.popLabel();

        return sam;
    }

    static String getVarStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        String sam = "";
        VariableSymbol variable = getVar(f, method);

        if (!CompilerUtils.check(f, '=')) {
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
            sam += "PUSHOFF " + method.getThisAddress() + "\n";
            sam += "PUSHIMM " + variable.address + "\n";
            sam += "ADD\n";
            sam += expr.samCode;
            sam += "STOREIND\n";
        } else {
            sam += expr.samCode;
            sam += "STOREOFF " + variable.address + "\n";
        }

        if (!CompilerUtils.check(f, ';')) {
            throw new SyntaxErrorException(
                "getStmt expects ';' at end of statement",
                f.lineNo()
            );
        }

        return sam;
    }

    static Expression getExpr(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        // Expr -> this
        if (CompilerUtils.check(f, "this")) {
            VariableSymbol thisSymbol = method.getThisSymbol();
            return new Expression(
                "PUSHOFF " + thisSymbol.address + "\n",
                thisSymbol.type
            );
        }

        // Expr -> null
        if (CompilerUtils.check(f, "null")) {
            return new Expression("PUSHIMM 0\n", Type.VOID);
        }

        //Expr -> new ClassName (Actuals)
        if (f.test("new")) {
            return getConstructorExpr(f, method);
        }

        if (CompilerUtils.check(f, '(')) {
            Expression expr = null;
            // Expr -> ( Unop Expr )
            if (f.test('~') || f.test('!')) {
                expr = getUnopExpr(f, method);
            } else {
                // Expr -> ( Expr (...) )
                expr = getExpr(f, method);

                // Raise if Expr -> ( Expr NOT('?' | ')' | Binop) )
                if (f.peekAtKind() != TokenType.OPERATOR) {
                    throw new SyntaxErrorException(
                        "Expr -> Expr (...) expects '?' | ')' | Binop",
                        f.lineNo()
                    );
                }

                // Expr -> ( Expr ) , ends early
                if (CompilerUtils.check(f, ')')) {
                    return expr;
                }

                // Exprt -> (Expr ? Expr : Expr)
                if (CompilerUtils.check(f, '?')) {
                    Expression ternaryExpr = getTernaryExpr(f, method);
                    expr.samCode += ternaryExpr.samCode;
                    expr.type = ternaryExpr.type;
                }
                // Exprt -> (Expr Binop Expr)
                else {
                    Expression binopExpr = getBinopExpr(f, expr, method);
                    expr.samCode += binopExpr.samCode;
                    expr.type = binopExpr.type;
                }
            }

            // Check closing ')'
            if (!CompilerUtils.check(f, ')')) {
                throw new SyntaxErrorException(
                    "getExpr expects ')' at end of Expr -> ( Expr (...) )",
                    f.lineNo()
                );
            }

            return expr;
        }
        // Expr -> Var | Literal
        else {
            return getTerminal(f, method);
        }
    }

    static Expression getConstructorExpr(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        if (!CompilerUtils.check(f, "new")) {
            throw new SyntaxErrorException(
                "getInstance expects 'new' at the beginning",
                f.lineNo()
            );
        }

        String className = Helpers.getIdentifier(f);
        ClassSymbol classSymbol = LiveOak3Compiler.globalSymbol.lookupSymbol(
            className,
            ClassSymbol.class
        );
        if (classSymbol == null) {
            throw new CompilerException(
                "instanciating class that doesn't exist",
                f.lineNo()
            );
        }
        MethodSymbol constructor = classSymbol.lookupSymbol(
            className,
            MethodSymbol.class
        );
        if (constructor == null) {
            // No constructor was declared for this Class, instanciate it anyway
            String sam = Helpers.initObject(classSymbol);

            if (!CompilerUtils.check(f, '(')) {
                throw new SyntaxErrorException(
                    "getMethodCallExpr expects '(' at the start of actuals",
                    f.lineNo()
                );
            }
            while (!CompilerUtils.check(f, ')')) {
                CompilerUtils.skipToken(f);
            }

            return new Expression(sam, Type.getType(classSymbol.name));
        }

        return getMethodCallExpr(f, method, null, constructor);
    }

    static Expression getUnopExpr(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        // Not an operator, raise
        if (f.peekAtKind() != TokenType.OPERATOR) {
            throw new TypeErrorException(
                "Helpers.getUnopExpr expects an OPERATOR",
                f.lineNo()
            );
        }
        char op = CompilerUtils.getOp(f);

        // getExpr() would return "exactly" one value on the stack
        Expression expr = getExpr(f, method);

        /*** Special case
         ***/
        if (op == '~' && expr.type == Type.STRING) {
            expr.samCode += Helpers.reverseString();
        } /*** Basic cases
         ***/else {
            // Type check
            if (
                op == '~' && expr.type != Type.INT && expr.type != Type.STRING
            ) {
                throw new TypeErrorException(
                    "Bitwise NOT operation requires INT | STRING operand, but got " +
                    expr.type,
                    f.lineNo()
                );
            }

            // apply unop on expression
            expr.samCode += Helpers.getUnop(op);
        }

        return expr;
    }

    static Expression getBinopExpr(
        SamTokenizer f,
        Expression prevExpr,
        MethodSymbol method
    ) throws CompilerException {
        // Not an operator, raise
        if (f.peekAtKind() != TokenType.OPERATOR) {
            throw new TypeErrorException(
                "Helpers.getBinopExpr expects an OPERATOR",
                f.lineNo()
            );
        }
        Label doneLabel = new Label();
        Label handleDivideZero = new Label();

        char op = CompilerUtils.getOp(f);
        String prevSam = "";

        // Optimisation: if op is an OR operator and prevExpr is truthy, early return
        if (op == '|' && prevExpr.type == Type.BOOL) {
            prevSam += "ISPOS\n";
            prevSam += "DUP\n";
            prevSam += "JUMPC " + doneLabel.name + "\n";
        }

        Expression expr = getExpr(f, method);
        expr.samCode = prevSam + expr.samCode;

        /*** Special cases:
         ***/
        // String repeat
        if (
            op == '*' &&
            ((prevExpr.type == Type.STRING && expr.type == Type.INT) ||
                (prevExpr.type == Type.INT && expr.type == Type.STRING))
        ) {
            expr.samCode += Helpers.repeatString(prevExpr.type, expr.type);
            expr.type = Type.STRING;
        }
        // String concatenation
        else if (
            op == '+' &&
            prevExpr.type == Type.STRING &&
            expr.type == Type.STRING
        ) {
            expr.samCode += Helpers.concatString();
            expr.type = Type.STRING;
        }
        // String comparison
        else if (
            Helpers.getBinopType(op) == BinopType.COMPARISON &&
            prevExpr.type == Type.STRING &&
            expr.type == Type.STRING
        ) {
            expr.samCode += Helpers.compareString(op);
            expr.type = Type.STRING;
        } else {
            /*** Basic cases
             ***/
            // Type check return
            if (!expr.type.isCompatibleWith(prevExpr.type)) {
                throw new TypeErrorException(
                    "Binop expr type mismatch: " +
                    prevExpr.type +
                    " and " +
                    expr.type,
                    f.lineNo()
                );
            }

            // Type check for Logical operations
            if (
                Helpers.getBinopType(op) == BinopType.BITWISE &&
                (prevExpr.type != Type.BOOL || expr.type != Type.BOOL)
            ) {
                throw new TypeErrorException(
                    "Logical operation '" +
                    op +
                    "' requires BOOL operands, but got " +
                    prevExpr.type +
                    " and " +
                    expr.type,
                    f.lineNo()
                );
            }

            // Handle divide by zero
            if (op == '/' && expr.type == Type.INT) {
                expr.samCode += "DUP\n";
                expr.samCode += "ISNIL\n";
                expr.samCode += "JUMPC " + handleDivideZero.name + "\n";
            }

            // basic binop sam code
            expr.samCode += Helpers.getBinop(op);
        }

        // Change return type to boolean if binop is Comparison
        if (Helpers.getBinopType(op) == BinopType.COMPARISON) {
            expr.type = Type.BOOL;
        }

        expr.samCode += "JUMP " + doneLabel.name + "\n";

        expr.samCode += handleDivideZero.name + ":\n";
        expr.samCode += "SWAP\n";
        expr.samCode += "DIV\n";

        expr.samCode += doneLabel.name + ":\n";

        return expr;
    }

    static Expression getTernaryExpr(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        // Generate sam code
        Expression expr = new Expression();

        // // labels used
        // String start_ternary = new Label();
        Label stop_ternary = new Label();
        Label false_expr = new Label();

        // // Expr ? (...) : (...)
        expr.samCode += "ISNIL\n";
        expr.samCode += "JUMPC " + false_expr.name + "\n";

        // Truth expression:  (...) ? Expr : (..)
        Expression truthExpr = getExpr(f, method);
        expr.samCode += truthExpr.samCode;
        expr.samCode += "JUMP " + stop_ternary.name + "\n";

        // Checks ':'
        if (!CompilerUtils.check(f, ':')) {
            throw new SyntaxErrorException(
                "Ternary expects ':' between expressions",
                f.lineNo()
            );
        }

        // False expression: (...) ? (...) : Expr
        expr.samCode += false_expr.name + ":\n";
        Expression falseExpr = getExpr(f, method);
        expr.samCode += falseExpr.samCode;

        // Type check return
        if (!truthExpr.type.isCompatibleWith(falseExpr.type)) {
            throw new TypeErrorException(
                "Ternary expr type mismatch: " +
                truthExpr.type +
                " and " +
                falseExpr.type,
                f.lineNo()
            );
        }
        expr.type = truthExpr.type;

        // Stop Frame
        expr.samCode += stop_ternary.name + ":\n";

        return expr;
    }

    static Expression getMethodCallExpr(
        SamTokenizer f,
        MethodSymbol scopeMethod,
        VariableSymbol callingVariable,
        MethodSymbol callingMethod
    ) throws CompilerException {
        String sam = "";
        sam += "PUSHIMM 0\n"; // return value

        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "getMethodCallExpr expects '(' at the start of actuals",
                f.lineNo()
            );
        }

        sam += getActuals(f, scopeMethod, callingVariable, callingMethod);
        sam += "LINK\n";
        sam += "JSR " + callingMethod.getLabelName() + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -" + callingMethod.numParameters() + "\n";

        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "getMethodCallExpr expects ')' at the end of actuals",
                f.lineNo()
            );
        }

        return new Expression(sam, callingMethod.returnType);
    }

    static String getActuals(
        SamTokenizer f,
        MethodSymbol scopeMethod,
        VariableSymbol callingVariable,
        MethodSymbol callingMethod
    ) throws CompilerException {
        String sam = "";
        int paramCount = callingMethod.numParameters();

        // check if callingMethod is a constructor
        if (callingMethod.isConstructor() && callingVariable == null) {
            // instanciate class to pass in as "this" parameter
            sam += Helpers.initObject((ClassSymbol) callingMethod.parent);
        } else {
            if (callingVariable == null) {
                throw new CompilerException(
                    "Cannot invoke method from null instance",
                    f.lineNo()
                );
            }
            if (callingVariable.isInstanceVariable()) {
                sam += "PUSHOFF " + callingMethod.getThisAddress() + "\n";
                sam += "PUSHIMM " + callingVariable.address + "\n";
                sam += "ADD\n";
                sam += "PUSHIND\n";
            } else {
                sam += "PUSHOFF " + callingVariable.address + "\n";
            }
        }

        // start from 1, because "this" is the first param
        int argCount = 1;
        do {
            // check done processing all the actuals
            if (f.test(')')) {
                break;
            }
            // too many actuals provided
            if (argCount > paramCount) {
                throw new SyntaxErrorException(
                    "Too many arguments provided for method '" +
                    callingMethod.name +
                    "'. Expected " +
                    paramCount +
                    " but got more.",
                    f.lineNo()
                );
            }

            Expression expr = getExpr(f, scopeMethod);
            VariableSymbol currParam = callingMethod.parameters.get(argCount);

            // Type check
            if (!expr.type.isCompatibleWith(currParam.type)) {
                throw new TypeErrorException(
                    "Argument type mismatch for parameter '" +
                    currParam.name +
                    "': expected " +
                    currParam.type +
                    " but got " +
                    expr.type,
                    f.lineNo()
                );
            }

            // write sam code
            sam += expr.samCode;

            argCount++;
        } while (CompilerUtils.check(f, ','));

        // too few actuals provided
        if (argCount < paramCount) {
            throw new SyntaxErrorException(
                "Not enough arguments provided for method '" +
                callingMethod.name +
                "'. Expected " +
                paramCount +
                " but got " +
                argCount,
                f.lineNo()
            );
        }

        return sam;
    }

    // getTerminal is now a recursive operation
    static Expression getTerminal(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        TokenType type = f.peekAtKind();
        switch (type) {
            // Expr -> Literal -> Num
            case INTEGER:
                int value = CompilerUtils.getInt(f);
                return new Expression("PUSHIMM " + value + "\n", Type.INT);
            // Expr -> Literal -> String
            case STRING:
                String strValue = CompilerUtils.getString(f);
                return new Expression(
                    "PUSHIMMSTR \"" + strValue + "\"\n",
                    Type.STRING
                );
            // Expr -> MethodName | Var | Literal
            case WORD:
                String name = CompilerUtils.getWord(f);

                // Expr -> Literal -> "true" | "false"
                if (name.equals("true")) {
                    return new Expression("PUSHIMM 1\n", Type.BOOL);
                }
                if (name.equals("false")) {
                    return new Expression("PUSHIMM 0\n", Type.BOOL);
                }

                VariableSymbol varSymbol = method.lookupSymbol(
                    name,
                    VariableSymbol.class
                );
                if (varSymbol == null) {
                    throw new CompilerException(
                        "getTerminal trying to access symbol that has not been declared: Symbol " +
                        varSymbol,
                        f.lineNo()
                    );
                }

                // Expr -> Var.MethodName(Actuals)
                if (CompilerUtils.check(f, '.')) {
                    ClassSymbol classSymbol =
                        LiveOak3Compiler.globalSymbol.lookupSymbol(
                            varSymbol.type.toString(),
                            ClassSymbol.class
                        );

                    String methodName = CompilerUtils.getWord(f);
                    MethodSymbol callingMethod = classSymbol.lookupSymbol(
                        methodName,
                        MethodSymbol.class
                    );
                    return getMethodCallExpr(
                        f,
                        method,
                        varSymbol,
                        callingMethod
                    );
                }
                // Expr -> Var
                else {
                    String sam = "";

                    if (varSymbol.isInstanceVariable()) {
                        sam += "PUSHOFF " + method.getThisAddress() + "\n";
                        sam += "PUSHIMM " + varSymbol.address + "\n";
                        sam += "ADD\n";
                        sam += "PUSHIND\n";
                    } else {
                        sam += "PUSHOFF " + varSymbol.address + "\n";
                    }

                    return new Expression(sam, varSymbol.type);
                }
            default:
                throw new TypeErrorException(
                    "getTerminal received invalid type " + type,
                    f.lineNo()
                );
        }
    }

    static Type getType(SamTokenizer f) throws CompilerException {
        // typeString = "int" | "bool" | "String"
        String typeString = CompilerUtils.getWord(f);
        Type type = Type.getType(typeString);

        // typeString != INT | BOOL | STRING
        if (type == null) {
            throw new TypeErrorException(
                "Invalid type: " + typeString,
                f.lineNo()
            );
        }

        return type;
    }

    /*** Non-recursive operations. Override "getVar", inherit the rest from LiveOak0Compiler
     ***/
    static VariableSymbol getVar(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        // Not a var, raise
        if (f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getVar should starts with a WORD",
                f.lineNo()
            );
        }

        String varName = CompilerUtils.getWord(f);
        VariableSymbol variable = method.lookupSymbol(
            varName,
            VariableSymbol.class
        );
        if (variable == null) {
            throw new SyntaxErrorException(
                "getVar trying to access variable that has not been declared: Variable" +
                varName,
                f.lineNo()
            );
        }
        return variable;
    }
}
