package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*** Code generator based on grammar
 ***/
public class CodeGenerator {

    private static ClassSymbol symbolTable = null;

    // Main program generation
    public static String getProgram(SamTokenizer f, ClassSymbol symbolTable)
        throws CompilerException {
        // set symbol table
        CodeGenerator.symbolTable = symbolTable;

        StringBuilder sam = new StringBuilder();

        /*** DATA SEGMENT: only vtables for now
         ***/
        sam.append(DataGenerator.generateStaticData(symbolTable));

        /*** CODE SEGMENT: get main program
         ***/
        sam.append(getMainProgram(f));

        // Process class declarations
        while (f.peekAtKind() != TokenType.EOF) {
            sam.append(getClassDecl(f));
        }

        return sam.toString();
    }

    private static String getMainProgram(SamTokenizer f)
        throws CompilerException {
        // Get main class and main method from symbol table
        ClassSymbol mainClass = symbolTable.lookupSymbol(
            "Main",
            ClassSymbol.class
        );
        if (mainClass == null) {
            throw new CompilerException("Missing 'Main' class", f.lineNo());
        }
        MethodSymbol mainMethod = mainClass.lookupSymbol(
            "main",
            MethodSymbol.class
        );
        if (mainMethod == null) {
            throw new CompilerException("Missing 'Main' method", f.lineNo());
        }

        StringBuilder sam = new StringBuilder();

        // program return value
        sam.append("PUSHIMM 0\n");

        // instantiate main class to pass in as "this" parameter
        sam.append("PUSHIMM 1\n");
        sam.append("MALLOC\n");
        // assign main class's vtable
        sam.append("DUP\n");
        sam.append("PUSHABS ").append(mainClass.vtableAddress).append("\n");
        sam.append("STOREIND\n");

        // run main method
        sam.append("LINK\n");
        sam.append("JSR ").append(mainMethod.getLabelName()).append("\n");
        sam.append("UNLINK\n");
        sam.append("ADDSP -").append(mainMethod.numParameters()).append("\n");
        sam.append(DataGenerator.freeStaticData(symbolTable));
        sam.append("STOP\n");

        return sam.toString();
    }

    private static String getClassDecl(SamTokenizer f)
        throws CompilerException {
        // Generate sam code
        StringBuilder sam = new StringBuilder();

        // ClassDecl -> class...
        if (!Tokenizer.check(f, "class")) {
            throw new SyntaxErrorException(
                "populateClass expects 'class' at the start",
                f.lineNo()
            );
        }

        // ClassDecl -> class ClassName ...
        String className = Helper.getIdentifier(f);

        // Pull class from global scope
        ClassSymbol classSymbol = symbolTable.lookupSymbol(
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
        if (!Tokenizer.check(f, '(')) {
            throw new SyntaxErrorException(
                "populateClass expects '(' at start of get formals",
                f.lineNo()
            );
        }
        // ClassDecl -> class className ( Formals? ) ...
        while (!Tokenizer.check(f, ')')) {
            Tokenizer.skipToken(f);
        }

        // ClassDecl -> class ClassName ( Formals? ) {...
        if (!Tokenizer.check(f, '{')) {
            throw new SyntaxErrorException(
                "populateClass expects '{' at start of get class body",
                f.lineNo()
            );
        }

        // ClassDecl -> class ClassName ( Formals? ) { MethodDecl...
        while (f.peekAtKind() == TokenType.WORD) {
            sam.append(getMethodDecl(f, classSymbol));
        }

        // ClassDecl -> class ClassName ( Formals? ) { MethodDecl...}
        if (!Tokenizer.check(f, '}')) {
            throw new SyntaxErrorException(
                "populateClass expects '}' at end of get class body",
                f.lineNo()
            );
        }

        return sam.toString();
    }

    private static String getMethodDecl(
        SamTokenizer f,
        ClassSymbol classSymbol
    ) throws CompilerException {
        // Generate sam code
        StringBuilder sam = new StringBuilder("\n");

        // MethodDecl -> Type ...
        Type returnType = getType(f);

        // MethodDecl -> Type MethodName ...
        String methodName = Helper.getIdentifier(f);

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
        sam.append(method.getLabelName()).append(":\n");

        // MethodDecl -> Type MethodName (...
        if (!Tokenizer.check(f, '(')) {
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
            String formalName = Helper.getIdentifier(f);

            if (!Tokenizer.check(f, ',')) {
                break;
            }
        }
        if (!Tokenizer.check(f, ')')) {
            throw new SyntaxErrorException(
                "get method expects ')' at end of get formals",
                f.lineNo()
            );
        }

        // MethodDecl -> Type MethodName ( Formals? ) { ...
        if (!Tokenizer.check(f, '{')) {
            throw new SyntaxErrorException(
                "get method expects '{' at start of body",
                f.lineNo()
            );
        }

        // MethodDecl -> Type MethodName ( Formals? ) { Body ...
        sam.append(getBody(f, method));

        // MethodDecl -> Type MethodName ( Formals? ) { Body }
        if (!Tokenizer.check(f, '}')) {
            throw new SyntaxErrorException(
                "get method expects '}' at end of body",
                f.lineNo()
            );
        }

        return sam.toString();
    }

    private static String getBody(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        StringBuilder sam = new StringBuilder();

        // while start with "int | bool | String"
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl will store variable in Hashmap: identifier -> { type: TokenType, relative_address: int }
            sam.append(getVarDecl(f, method));
        }

        // check EOF
        if (f.peekAtKind() == TokenType.EOF) {
            return sam.toString();
        }

        Label returnLabel = new Label(LabelType.RETURN);
        method.pushLabel(returnLabel);

        // Then, get Block
        if (!Tokenizer.check(f, '{')) {
            throw new SyntaxErrorException(
                "getBlock expects '{' at start of block",
                f.lineNo()
            );
        }
        List<Integer> endWithReturnStmt = new ArrayList<>();
        while (!Tokenizer.check(f, '}')) {
            if (f.test("return")) {
                endWithReturnStmt.add(1);
            } else {
                endWithReturnStmt.add(0);
            }

            sam.append(getStmt(f, method));
        }

        // if method is constructor, always return the "this" object
        if (method.isConstructor()) {
            // push "this" on to stack for return
            sam.append("PUSHOFF ").append(method.getThisAddress()).append("\n");
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
        sam
            .append(returnLabel.name)
            .append(":\n")
            .append("STOREOFF ")
            .append(method.returnAddress())
            .append("\n")
            .append("ADDSP -")
            .append(method.numLocalVariables())
            .append("\n")
            .append("RST\n");
        method.popLabel();

        return sam.toString();
    }

    private static String getVarDecl(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        StringBuilder sam = new StringBuilder();

        // VarDecl -> Type ...
        Type varType = getType(f);

        // while varName = a | b | c | ...
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl -> Type Identifier1, Identifier2
            String varName = Helper.getIdentifier(f);

            // write sam code
            sam.append("PUSHIMM 0\n");

            if (Tokenizer.check(f, ',')) {
                continue;
            } else if (Tokenizer.check(f, ';')) {
                break;
            } else {
                throw new SyntaxErrorException(
                    "Expected ',' or `;` after each variable declaration",
                    f.lineNo()
                );
            }
        }

        return sam.toString();
    }

    private static String getBlock(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        StringBuilder sam = new StringBuilder();

        if (!Tokenizer.check(f, '{')) {
            throw new SyntaxErrorException(
                "getBlock expects '{' at start of block",
                f.lineNo()
            );
        }

        while (!Tokenizer.check(f, '}')) {
            sam.append(getStmt(f, method));
        }

        return sam.toString();
    }

    private static String getStmt(SamTokenizer f, MethodSymbol method)
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

        return new StringBuilder()
            .append("JUMP ")
            .append(breakLabel.name)
            .append("\n")
            .toString();
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
        sam.append("JUMP ").append(returnLabel.name).append("\n");

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

        // Generate sam code
        StringBuilder sam = new StringBuilder();

        // labels used
        Label stop_stmt = new Label();
        Label false_block = new Label();

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

        sam
            .append("ISNIL\n")
            .append("JUMPC ")
            .append(false_block.name)
            .append("\n");

        // Truth block:  // if ( Expr ) Block ...
        sam
            .append(getBlock(f, method))
            .append("JUMP ")
            .append(stop_stmt.name)
            .append("\n");

        // Checks 'else'
        if (!Tokenizer.getWord(f).equals("else")) {
            throw new SyntaxErrorException(
                "if statement expects 'else' between expressions",
                f.lineNo()
            );
        }

        // False block: (...) ? (...) : Expr
        sam.append(false_block.name).append(":\n").append(getBlock(f, method));

        // Done if statement
        sam.append(stop_stmt.name).append(":\n");

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

        // Generate sam code
        StringBuilder sam = new StringBuilder();

        // labels used
        Label start_loop = new Label();
        Label stop_loop = new Label(LabelType.BREAK);

        // Push exit label to use for break statement
        method.pushLabel(stop_loop);

        // while ( Expr ) ...
        if (!Tokenizer.check(f, '(')) {
            throw new SyntaxErrorException(
                "while statement expects '(' at beginining of condition",
                f.lineNo()
            );
        }

        sam
            .append(start_loop.name)
            .append(":\n")
            .append(getExpr(f, method).samCode);

        if (!Tokenizer.check(f, ')')) {
            throw new SyntaxErrorException(
                "while statement expects ')' at end of condition",
                f.lineNo()
            );
        }

        sam
            .append("ISNIL\n")
            .append("JUMPC ")
            .append(stop_loop.name)
            .append("\n");

        // Continue loop
        sam
            .append(getBlock(f, method))
            .append("JUMP ")
            .append(start_loop.name)
            .append("\n");

        // Stop loop
        sam.append(stop_loop.name).append(":\n");

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
                .append("PUSHOFF ")
                .append(method.getThisAddress())
                .append("\n")
                .append("PUSHIMM ")
                .append(variable.address)
                .append("\n")
                .append("ADD\n")
                .append(expr.samCode)
                .append("STOREIND\n");
        } else {
            sam
                .append(expr.samCode)
                .append("STOREOFF ")
                .append(variable.address)
                .append("\n");
        }

        if (!Tokenizer.check(f, ';')) {
            throw new SyntaxErrorException(
                "getStmt expects ';' at end of statement",
                f.lineNo()
            );
        }

        return sam.toString();
    }

    private static Expression getExpr(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        // Expr -> this
        if (Tokenizer.check(f, "this")) {
            VariableSymbol thisSymbol = method.getThisSymbol();
            return new Expression(
                new StringBuilder()
                    .append("PUSHOFF ")
                    .append(thisSymbol.address)
                    .append("\n")
                    .toString(),
                thisSymbol.type
            );
        }

        // Expr -> null
        if (Tokenizer.check(f, "null")) {
            return new Expression("PUSHIMM 0\n", Type.VOID);
        }

        //Expr -> new ClassName (Actuals)
        if (f.test("new")) {
            return getConstructorExpr(f, method);
        }

        if (Tokenizer.check(f, '(')) {
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
                if (Tokenizer.check(f, ')')) {
                    return expr;
                }

                // Exprt -> (Expr ? Expr : Expr)
                if (Tokenizer.check(f, '?')) {
                    Expression ternaryExpr = getTernaryExpr(f, method);
                    expr.samCode = new StringBuilder(expr.samCode)
                        .append(ternaryExpr.samCode)
                        .toString();
                    expr.type = ternaryExpr.type;
                }
                // Exprt -> (Expr Binop Expr)
                else {
                    Expression binopExpr = getBinopExpr(f, expr, method);
                    expr.samCode = new StringBuilder(expr.samCode)
                        .append(binopExpr.samCode)
                        .toString();
                    expr.type = binopExpr.type;
                }
            }

            // Check closing ')'
            if (!Tokenizer.check(f, ')')) {
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

    private static Expression getConstructorExpr(
        SamTokenizer f,
        MethodSymbol method
    ) throws CompilerException {
        if (!Tokenizer.check(f, "new")) {
            throw new SyntaxErrorException(
                "getInstance expects 'new' at the beginning",
                f.lineNo()
            );
        }

        String className = Helper.getIdentifier(f);
        ClassSymbol classSymbol = symbolTable.lookupSymbol(
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
            // No constructor was declared for this Class, instantiate it anyway
            String sam = Helper.initObject(classSymbol);

            if (!Tokenizer.check(f, '(')) {
                throw new SyntaxErrorException(
                    "getMethodCallExpr expects '(' at the start of actuals",
                    f.lineNo()
                );
            }
            while (!Tokenizer.check(f, ')')) {
                Tokenizer.skipToken(f);
            }

            return new Expression(sam, Type.getType(classSymbol.name));
        }

        return getMethodCallExpr(f, method, null, constructor);
    }

    private static Expression getUnopExpr(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        // Not an operator, raise
        if (f.peekAtKind() != TokenType.OPERATOR) {
            throw new TypeErrorException(
                "Helper.getUnopExpr expects an OPERATOR",
                f.lineNo()
            );
        }
        char op = Tokenizer.getOp(f);

        // getExpr() would return "exactly" one value on the stack
        Expression expr = getExpr(f, method);

        /*** Special case ***/
        if (op == '~' && expr.type == Type.STRING) {
            expr.samCode = new StringBuilder(expr.samCode)
                .append(StringHelper.reverseString())
                .toString();
        } /*** Basic cases ***/else {
            // Type check
            if (
                op == '~' && expr.type != Type.INT && expr.type != Type.STRING
            ) {
                throw new TypeErrorException(
                    new StringBuilder()
                        .append(
                            "Bitwise NOT operation requires INT | STRING operand, but got "
                        )
                        .append(expr.type)
                        .toString(),
                    f.lineNo()
                );
            }

            // apply unop on expression
            expr.samCode = new StringBuilder(expr.samCode)
                .append(Helper.getUnop(op))
                .toString();
        }

        return expr;
    }

    private static Expression getBinopExpr(
        SamTokenizer f,
        Expression prevExpr,
        MethodSymbol method
    ) throws CompilerException {
        // Not an operator, raise
        if (f.peekAtKind() != TokenType.OPERATOR) {
            throw new TypeErrorException(
                "Helper.getBinopExpr expects an OPERATOR",
                f.lineNo()
            );
        }
        Label doneLabel = new Label();
        Label handleDivideZero = new Label();

        char op = Tokenizer.getOp(f);
        StringBuilder prevSam = new StringBuilder();

        // Optimisation: if op is an OR operator and prevExpr is truthy, early return
        if (op == '|' && prevExpr.type == Type.BOOL) {
            prevSam
                .append("ISPOS\n")
                .append("DUP\n")
                .append("JUMPC ")
                .append(doneLabel.name)
                .append("\n");
        }

        Expression expr = getExpr(f, method);
        expr.samCode = new StringBuilder(prevSam)
            .append(expr.samCode)
            .toString();

        /*** Special cases: ***/
        // String repeat
        if (
            op == '*' &&
            ((prevExpr.type == Type.STRING && expr.type == Type.INT) ||
                (prevExpr.type == Type.INT && expr.type == Type.STRING))
        ) {
            expr.samCode = new StringBuilder(expr.samCode)
                .append(StringHelper.repeatString(prevExpr.type, expr.type))
                .toString();
            expr.type = Type.STRING;
        }
        // String concatenation
        else if (
            op == '+' &&
            prevExpr.type == Type.STRING &&
            expr.type == Type.STRING
        ) {
            expr.samCode = new StringBuilder(expr.samCode)
                .append(StringHelper.concatString())
                .toString();
            expr.type = Type.STRING;
        }
        // String comparison
        else if (
            Helper.getBinopType(op) == BinopType.COMPARISON &&
            prevExpr.type == Type.STRING &&
            expr.type == Type.STRING
        ) {
            expr.samCode = new StringBuilder(expr.samCode)
                .append(StringHelper.compareString(op))
                .toString();
            expr.type = Type.STRING;
        } else {
            /*** Basic cases ***/
            // Type check return
            if (!expr.type.isCompatibleWith(prevExpr.type)) {
                throw new TypeErrorException(
                    new StringBuilder()
                        .append("Binop expr type mismatch: ")
                        .append(prevExpr.type)
                        .append(" and ")
                        .append(expr.type)
                        .toString(),
                    f.lineNo()
                );
            }

            // Type check for Logical operations
            if (
                Helper.getBinopType(op) == BinopType.BITWISE &&
                (prevExpr.type != Type.BOOL || expr.type != Type.BOOL)
            ) {
                throw new TypeErrorException(
                    new StringBuilder()
                        .append("Logical operation '")
                        .append(op)
                        .append("' requires BOOL operands, but got ")
                        .append(prevExpr.type)
                        .append(" and ")
                        .append(expr.type)
                        .toString(),
                    f.lineNo()
                );
            }

            // Handle divide by zero
            StringBuilder divZeroCheck = new StringBuilder();
            if (op == '/' && expr.type == Type.INT) {
                divZeroCheck
                    .append("DUP\n")
                    .append("ISNIL\n")
                    .append("JUMPC ")
                    .append(handleDivideZero.name)
                    .append("\n");
            }
            expr.samCode = new StringBuilder(expr.samCode)
                .append(divZeroCheck)
                .append(Helper.getBinop(op))
                .toString();
        }

        // Change return type to boolean if binop is Comparison
        if (Helper.getBinopType(op) == BinopType.COMPARISON) {
            expr.type = Type.BOOL;
        }

        expr.samCode = new StringBuilder(expr.samCode)
            .append("JUMP ")
            .append(doneLabel.name)
            .append("\n")
            .append(handleDivideZero.name)
            .append(":\n")
            .append("SWAP\n")
            .append("DIV\n")
            .append(doneLabel.name)
            .append(":\n")
            .toString();

        return expr;
    }

    private static Expression getTernaryExpr(
        SamTokenizer f,
        MethodSymbol method
    ) throws CompilerException {
        // Generate sam code
        Expression expr = new Expression();

        Label stop_ternary = new Label();
        Label false_expr = new Label();

        StringBuilder samBuilder = new StringBuilder()
            .append("ISNIL\n")
            .append("JUMPC ")
            .append(false_expr.name)
            .append("\n");

        // Truth expression: (...) ? Expr : (..)
        Expression truthExpr = getExpr(f, method);
        samBuilder
            .append(truthExpr.samCode)
            .append("JUMP ")
            .append(stop_ternary.name)
            .append("\n");

        // Checks ':'
        if (!Tokenizer.check(f, ':')) {
            throw new SyntaxErrorException(
                "Ternary expects ':' between expressions",
                f.lineNo()
            );
        }

        // False expression: (...) ? (...) : Expr
        samBuilder.append(false_expr.name).append(":\n");
        Expression falseExpr = getExpr(f, method);
        samBuilder.append(falseExpr.samCode);

        // Type check return
        if (!truthExpr.type.isCompatibleWith(falseExpr.type)) {
            throw new TypeErrorException(
                new StringBuilder()
                    .append("Ternary expr type mismatch: ")
                    .append(truthExpr.type)
                    .append(" and ")
                    .append(falseExpr.type)
                    .toString(),
                f.lineNo()
            );
        }
        expr.type = truthExpr.type;

        // Stop Frame
        samBuilder.append(stop_ternary.name).append(":\n");
        expr.samCode = samBuilder.toString();

        return expr;
    }

    private static Expression getMethodCallExpr(
        SamTokenizer f,
        MethodSymbol scopeMethod,
        VariableSymbol scopeVariable,
        MethodSymbol callingMethod
    ) throws CompilerException {
        StringBuilder sam = new StringBuilder().append("PUSHIMM 0\n"); // return value

        if (!Tokenizer.check(f, '(')) {
            throw new SyntaxErrorException(
                "getMethodCallExpr expects '(' at the start of actuals",
                f.lineNo()
            );
        }

        sam
            .append(getActuals(f, scopeMethod, scopeVariable, callingMethod))
            .append("LINK\n")
            .append("JSR ")
            .append(callingMethod.getLabelName())
            .append("\n")
            .append("UNLINK\n")
            .append("ADDSP -")
            .append(callingMethod.numParameters())
            .append("\n");

        if (!Tokenizer.check(f, ')')) {
            throw new SyntaxErrorException(
                "getMethodCallExpr expects ')' at the end of actuals",
                f.lineNo()
            );
        }

        return new Expression(sam.toString(), callingMethod.returnType);
    }

    private static String getActuals(
        SamTokenizer f,
        MethodSymbol scopeMethod,
        VariableSymbol scopeVariable,
        MethodSymbol callingMethod
    ) throws CompilerException {
        StringBuilder sam = new StringBuilder();
        int paramCount = callingMethod.numParameters();

        // check if callingMethod is a constructor
        if (callingMethod.isConstructor() && scopeVariable == null) {
            // instantiate class to pass in as "this" parameter
            sam.append(Helper.initObject((ClassSymbol) callingMethod.parent));
        } else {
            if (scopeVariable == null) {
                throw new CompilerException(
                    "Cannot invoke method from null instance",
                    f.lineNo()
                );
            }
            if (scopeVariable.isInstanceVariable()) {
                sam
                    .append("PUSHOFF ")
                    .append(scopeMethod.getThisAddress())
                    .append("\n")
                    .append("PUSHIMM ")
                    .append(scopeVariable.address)
                    .append("\n")
                    .append("ADD\n")
                    .append("PUSHIND\n");
            } else {
                sam
                    .append("PUSHOFF ")
                    .append(scopeVariable.address)
                    .append("\n");
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
                    new StringBuilder()
                        .append("Too many arguments provided for method '")
                        .append(callingMethod.name)
                        .append("'. Expected ")
                        .append(paramCount)
                        .append(" but got more.")
                        .toString(),
                    f.lineNo()
                );
            }

            Expression expr = getExpr(f, scopeMethod);
            VariableSymbol currParam = callingMethod.parameters.get(argCount);

            // Type check
            if (!expr.type.isCompatibleWith(currParam.type)) {
                throw new TypeErrorException(
                    new StringBuilder()
                        .append("Argument type mismatch for parameter '")
                        .append(currParam.name)
                        .append("': expected ")
                        .append(currParam.type)
                        .append(" but got ")
                        .append(expr.type)
                        .toString(),
                    f.lineNo()
                );
            }

            // write sam code
            sam.append(expr.samCode);

            argCount++;
        } while (Tokenizer.check(f, ','));

        // too few actuals provided
        if (argCount < paramCount) {
            throw new SyntaxErrorException(
                new StringBuilder()
                    .append("Not enough arguments provided for method '")
                    .append(callingMethod.name)
                    .append("'. Expected ")
                    .append(paramCount)
                    .append(" but got ")
                    .append(argCount)
                    .toString(),
                f.lineNo()
            );
        }

        return sam.toString();
    }

    private static Expression getTerminal(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        TokenType type = f.peekAtKind();
        switch (type) {
            // Expr -> Literal -> Num
            case INTEGER:
                int value = Tokenizer.getInt(f);
                return new Expression(
                    new StringBuilder()
                        .append("PUSHIMM ")
                        .append(value)
                        .append("\n")
                        .toString(),
                    Type.INT
                );
            // Expr -> Literal -> String
            case STRING:
                String strValue = Tokenizer.getString(f);
                return new Expression(
                    new StringBuilder()
                        .append("PUSHIMMSTR \"")
                        .append(strValue)
                        .append("\"\n")
                        .toString(),
                    Type.STRING
                );
            // Expr -> Var | Literal
            case WORD:
                String name = Tokenizer.getWord(f);

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
                        new StringBuilder()
                            .append(
                                "getTerminal trying to access symbol that has not been declared: Symbol "
                            )
                            .append(varSymbol)
                            .toString(),
                        f.lineNo()
                    );
                }
                // Expr -> Var.MethodName(Actuals)
                if (Tokenizer.check(f, '.')) {
                    ClassSymbol classSymbol = symbolTable.lookupSymbol(
                        varSymbol.type.toString(),
                        ClassSymbol.class
                    );

                    String methodName = Tokenizer.getWord(f);
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
                    StringBuilder sam = new StringBuilder();

                    if (varSymbol.isInstanceVariable()) {
                        sam
                            .append("PUSHOFF ")
                            .append(method.getThisAddress())
                            .append("\n")
                            .append("PUSHIMM ")
                            .append(varSymbol.address)
                            .append("\n")
                            .append("ADD\n")
                            .append("PUSHIND\n");
                    } else {
                        sam
                            .append("PUSHOFF ")
                            .append(varSymbol.address)
                            .append("\n");
                    }

                    return new Expression(sam.toString(), varSymbol.type);
                }
            default:
                throw new TypeErrorException(
                    new StringBuilder()
                        .append("getTerminal received invalid type ")
                        .append(type)
                        .toString(),
                    f.lineNo()
                );
        }
    }

    private static Type getType(SamTokenizer f) throws CompilerException {
        // typeString = "int" | "bool" | "String"
        String typeString = Tokenizer.getWord(f);
        Type type = Type.getType(typeString);

        // typeString != INT | BOOL | STRING
        if (type == null) {
            throw new TypeErrorException(
                new StringBuilder()
                    .append("Invalid type: ")
                    .append(typeString)
                    .toString(),
                f.lineNo()
            );
        }

        return type;
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
