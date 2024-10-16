package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class LiveOak2Compiler extends LiveOak0Compiler {

    //             globalNode
    //             /         \
    //       mainMethod      anotherMethod
    //      /         \
    //   local1       local2

    public static Node globalNode = new Node();

    public static void reset() {
        CompilerUtils.clearTokens();
        globalNode = new Node();
        MainMethod.resetInstance();
    }

    static String compiler(String fileName) throws Exception {
        reset(); // Clear the list before starting

        //returns SaM code for program in file
        try {
            SamTokenizer firstPass = new SamTokenizer(
                fileName,
                SamTokenizer.TokenizerOptions.PROCESS_STRINGS
            );
            populateSymbolTable(firstPass);

            SamTokenizer secondPass = new SamTokenizer(
                fileName,
                SamTokenizer.TokenizerOptions.PROCESS_STRINGS
            );
            String program = getProgram(secondPass);

            return program;
        } catch (CompilerException e) {
            String errorMessage = String.format(
                "Failed to compile %s.\nError Message: %s\n",
                fileName,
                e.getMessage()
            );
            System.err.println(errorMessage);
            CompilerUtils.printTokens();
            throw new Error(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = String.format(
                "Failed to compile %s.\nError Message: %s\n",
                fileName,
                e.getMessage()
            );
            System.err.println(errorMessage);
            // CompilerUtils.printTokens();
            throw new Error(errorMessage, e);
        }
    }

    /*** FIRST PASS: POPULATE SYMBOL TABLE
     ***/
    static void populateSymbolTable(SamTokenizer f) throws CompilerException {
        // First pass: populate symbolTable
        while (f.peekAtKind() != TokenType.EOF) {
            populateMethod(f);
        }
        CompilerUtils.clearTokens();
    }

    static void populateMethod(SamTokenizer f) throws CompilerException {
        // MethodDecl -> Type ...
        Type returnType = getType(f);

        // MethodDecl -> Type MethodName ...
        String methodName = getIdentifier(f);

        // Check if the method is already defined
        if (globalNode.existSymbol(methodName)) {
            throw new CompilerException(
                "Method '" + methodName + "' is already defined",
                f.lineNo()
            );
        }

        // MethodDecl -> Type MethodName (...
        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "populateMethod expects '(' at start of get formals",
                f.lineNo()
            );
        }

        // Init method
        MethodNode method = null;
        if (methodName.equals("main")) {
            // MethodDecl -> Type main() ...
            if (!CompilerUtils.check(f, ')')) {
                throw new SyntaxErrorException(
                    "main method should not receive formals",
                    f.lineNo()
                );
            }
            method = MainMethod.getInstance();
            method.type = returnType; // update return type for main method
        } else {
            // create Method object
            method = new MethodNode(methodName, returnType);

            // Save params in symbol table and method object
            populateParams(f, method);

            // MethodDecl -> Type MethodName ( Formals? ) ...
            if (!CompilerUtils.check(f, ')')) {
                throw new SyntaxErrorException(
                    "get method expects ')' at end of get formals",
                    f.lineNo()
                );
            }
        }
        // Save Method in global scope
        globalNode.addChild(method);

        // MethodDecl -> Type MethodName ( Formals? ) { ...
        if (!CompilerUtils.check(f, '{')) {
            throw new SyntaxErrorException(
                "populateMethod expects '{' at start of body",
                f.lineNo()
            );
        }

        // MethodDecl -> Type MethodName ( Formals? ) { Body ...
        populateLocals(f, method);

        // MethodDecl -> Type MethodName ( Formals? ) { Body }
        if (!CompilerUtils.check(f, '}')) {
            throw new SyntaxErrorException(
                "populateMethod expects '}' at end of body",
                f.lineNo()
            );
        }
    }

    static void populateParams(SamTokenizer f, MethodNode method)
        throws CompilerException {
        while (f.peekAtKind() == TokenType.WORD) {
            // Formals -> Type ...
            Type formalType = getType(f);

            // Formals -> Type Identifier
            String formalName = getIdentifier(f);

            // Check if the formal has already defined
            if (method.existSymbol(formalName)) {
                throw new CompilerException(
                    "populateParams: Param '" +
                    formalName +
                    "' has already defined",
                    f.lineNo()
                );
            }

            // set Formal as child of MethodNode
            VariableNode paramNode = new VariableNode(
                formalName,
                formalType,
                true
            );
            method.addChild(paramNode);

            if (!CompilerUtils.check(f, ',')) {
                break;
            }
        }
    }

    static void populateLocals(SamTokenizer f, MethodNode method)
        throws CompilerException {
        // while start with "int | bool | String"
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl -> Type ...
            Type varType = getType(f);

            // while varName = a | b | c | ...
            while (f.peekAtKind() == TokenType.WORD) {
                // VarDecl -> Type Identifier1, Identifier2
                String varName = getIdentifier(f);

                // Check if the variable is already defined in the current scope
                if (method.existSymbol(varName)) {
                    throw new CompilerException(
                        "populateLocals: Variable '" +
                        varName +
                        "' has already defined in this scope",
                        f.lineNo()
                    );
                }

                // save local variable as child of methodNode
                VariableNode variable = new VariableNode(
                    varName,
                    varType,
                    false
                );
                method.addChild(variable);

                if (CompilerUtils.check(f, ',')) {
                    continue;
                } else if (CompilerUtils.check(f, ';')) {
                    break;
                } else {
                    throw new SyntaxErrorException(
                        "populateLocals: Expected ',' or `;` after each variable declaration",
                        f.lineNo()
                    );
                }
            }
        }

        // MethodDecl -> Type MethodName ( Formals? ) { VarDecl [skipBlock] }
        skipBlock(f);
    }

    static void skipBlock(SamTokenizer f) throws CompilerException {
        // Skip the entire block in first pass
        Deque<Character> stack = new ArrayDeque<>();

        if (!CompilerUtils.check(f, '{')) {
            throw new SyntaxErrorException(
                "skipBlock expects '{' at start of block",
                f.lineNo()
            );
        }
        stack.push('{');

        while (stack.size() > 0 && f.peekAtKind() != TokenType.EOF) {
            if (CompilerUtils.check(f, '{')) {
                stack.push('{');
            } else if (CompilerUtils.check(f, '}')) {
                stack.pop();
            } else {
                CompilerUtils.skipToken(f);
            }
        }
        if (stack.size() > 0) {
            throw new SyntaxErrorException(
                "skipBlock missed a closing parentheses",
                f.lineNo()
            );
        }
    }

    /*** SECOND PASS: CODEGEN
     ***/

    static String getProgram(SamTokenizer f) throws CompilerException {
        String pgm = "";
        pgm += "PUSHIMM 0\n";
        pgm += "LINK\n";
        pgm += "JSR main\n";
        pgm += "UNLINK\n";
        pgm += "STOP\n";

        // LiveOak-2
        while (f.peekAtKind() != TokenType.EOF) {
            pgm += getMethodDecl(f);
        }
        return pgm;
    }

    static String getMethodDecl(SamTokenizer f) throws CompilerException {
        // Generate sam code
        String sam = "\n";

        // MethodDecl -> Type ...
        Type returnType = getType(f);

        // MethodDecl -> Type MethodName ...
        String methodName = getIdentifier(f);

        // Pull method from global scope
        MethodNode method = globalNode.lookupSymbol(
            methodName,
            MethodNode.class
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
        sam += methodName + ":\n";

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
            String formalName = getIdentifier(f);

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

        // Check return method at the end
        if (
            method.peekStatement() != Statement.RETURN ||
            !method.hasStatement(Statement.RETURN)
        ) {
            throw new SyntaxErrorException(
                "get method missing return statement",
                f.lineNo()
            );
        }

        return sam;
    }

    /*** Recursive operations. Override all
     ***/
    static String getBody(SamTokenizer f, MethodNode method)
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

        String cleanupLabel = CompilerUtils.generateLabel();
        method.pushExitLabel(cleanupLabel);

        // Then, get Block
        sam += getBlock(f, method);

        // Cleanup procedure
        sam += cleanupLabel + ":\n";
        sam += "STOREOFF " + method.returnAddress() + "\n";
        sam += "ADDSP -" + method.numLocalVariables() + "\n";
        sam += "RST\n";
        method.popExitLabel();

        return sam;
    }

    static String getVarDecl(SamTokenizer f, MethodNode method)
        throws CompilerException {
        String sam = "";

        // VarDecl -> Type ...
        Type varType = getType(f);

        // while varName = a | b | c | ...
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl -> Type Identifier1, Identifier2
            String varName = getIdentifier(f);

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

    static String getBlock(SamTokenizer f, MethodNode method)
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

    static String getStmt(SamTokenizer f, MethodNode method)
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

    static String getBreakStmt(SamTokenizer f, MethodNode method)
        throws CompilerException {
        if (!CompilerUtils.check(f, "break")) {
            throw new SyntaxErrorException(
                "break statement expects 'break'",
                f.lineNo()
            );
        }

        String exitLabel = method.peekExitLabel();
        if (exitLabel == null) {
            throw new SyntaxErrorException(
                "break statement outside of a loop",
                f.lineNo()
            );
        }

        return "JUMP " + exitLabel + "\n";
    }

    static String getReturnStmt(SamTokenizer f, MethodNode method)
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
        if (!expr.type.isCompatibleWith(method.type)) {
            throw new TypeErrorException(
                "Return statement type mismatch: " +
                method.type +
                " and " +
                expr.type,
                f.lineNo()
            );
        }
        sam += expr.samCode;

        // Jump to clean up
        String cleanupLabel = method.peekExitLabel();
        if (cleanupLabel == null) {
            throw new CompilerException(
                "getReturnStmt missing exit label",
                f.lineNo()
            );
        }
        sam += "JUMP " + cleanupLabel + "\n";

        return sam;
    }

    static String getIfStmt(SamTokenizer f, MethodNode method)
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
        String stop_stmt = CompilerUtils.generateLabel();
        String false_block = CompilerUtils.generateLabel();

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
        sam += "JUMPC " + false_block + "\n";

        // Truth block:  // if ( Expr ) Block ...
        sam += getBlock(f, method);
        sam += "JUMP " + stop_stmt + "\n";

        // Checks 'else'
        if (!CompilerUtils.getWord(f).equals("else")) {
            throw new SyntaxErrorException(
                "if statement expects 'else' between expressions",
                f.lineNo()
            );
        }

        // False block: (...) ? (...) : Expr
        sam += false_block + ":\n";
        sam += getBlock(f, method);

        // Done if statement
        sam += stop_stmt + ":\n";

        return sam;
    }

    static String getWhileStmt(SamTokenizer f, MethodNode method)
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
        String start_loop = CompilerUtils.generateLabel();
        String stop_loop = CompilerUtils.generateLabel();

        // Push exit label to use for break statement
        method.pushExitLabel(stop_loop);

        // while ( Expr ) ...
        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "while statement expects '(' at beginining of condition",
                f.lineNo()
            );
        }

        sam += start_loop + ":\n";
        sam += getExpr(f, method).samCode;

        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "while statement expects ')' at end of condition",
                f.lineNo()
            );
        }

        sam += "ISNIL\n";
        sam += "JUMPC " + stop_loop + "\n";

        // Continue loop
        sam += getBlock(f, method);
        sam += "JUMP " + start_loop + "\n";

        // Stop loop
        sam += stop_loop + ":\n";

        // Pop label when done
        method.popExitLabel();

        return sam;
    }

    static String getVarStmt(SamTokenizer f, MethodNode method)
        throws CompilerException {
        String sam = "";
        Node variable = getVar(f, method);

        if (!CompilerUtils.check(f, '=')) {
            throw new SyntaxErrorException(
                "getStmt expects '=' after variable",
                f.lineNo()
            );
        }

        Expression expr = getExpr(f, method);
        // Type check
        if (!expr.type.isCompatibleWith(variable.type)) {
            System.out.println(expr.samCode);
            throw new TypeErrorException(
                "getVarStmt type mismatch: " +
                variable.type +
                " and " +
                expr.type,
                f.lineNo()
            );
        }

        // write sam code
        sam += expr.samCode;

        // update value in symbol
        variable.value = expr.value;

        // Store item on the stack to Node
        sam += "STOREOFF " + variable.address + "\n";

        if (!CompilerUtils.check(f, ';')) {
            throw new SyntaxErrorException(
                "getStmt expects ';' at end of statement",
                f.lineNo()
            );
        }

        return sam;
    }

    static Expression getExpr(SamTokenizer f, MethodNode method)
        throws CompilerException {
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
        // Expr -> MethodName | Var | Literal
        else {
            return getTerminal(f, method);
        }
    }

    static Expression getUnopExpr(SamTokenizer f, MethodNode method)
        throws CompilerException {
        // Not an operator, raise
        if (f.peekAtKind() != TokenType.OPERATOR) {
            throw new TypeErrorException(
                "getUnopExpr expects an OPERATOR",
                f.lineNo()
            );
        }
        char op = CompilerUtils.getOp(f);

        // unop sam code
        String unop_sam = getUnop(op);

        // getExpr() would return "exactly" one value on the stack
        Expression expr = getExpr(f, method);

        // Type check
        if (op == '~' && expr.type != Type.INT && expr.type != Type.STRING) {
            throw new TypeErrorException(
                "Bitwise NOT operation requires INT | STRING operand, but got " +
                expr.type,
                f.lineNo()
            );
        }

        // apply unop on expression
        expr.samCode += unop_sam;

        return expr;
    }

    static Expression getBinopExpr(
        SamTokenizer f,
        Expression prevExpr,
        MethodNode method
    ) throws CompilerException {
        // Not an operator, raise
        if (f.peekAtKind() != TokenType.OPERATOR) {
            throw new TypeErrorException(
                "getBinopExpr expects an OPERATOR",
                f.lineNo()
            );
        }
        char op = CompilerUtils.getOp(f);

        Expression expr = getExpr(f, method);

        /*** Special cases:
         ***/
        // String repeat
        if (
            op == '*' &&
            ((prevExpr.type == Type.STRING && expr.type == Type.INT) ||
                (prevExpr.type == Type.INT && expr.type == Type.STRING))
        ) {
            expr.samCode += repeatString(prevExpr.type, expr.type);
            expr.type = Type.STRING;
        }
        // String concatenation
        else if (
            op == '+' && prevExpr.type == Type.STRING && expr.type == Type.STRING
        ) {
            expr.samCode += concatString();
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
                getBinopType(op) == BinopType.BITWISE &&
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

            // basic binop sam code
            expr.samCode += getBinop(op);
        }

        // Change return type to boolean if binop is Comparison
        if (getBinopType(op) == BinopType.COMPARISON) {
            expr.type = Type.BOOL;
        }

        return expr;
    }

    static Expression getTernaryExpr(SamTokenizer f, MethodNode method)
        throws CompilerException {
        // Generate sam code
        Expression expr = new Expression();

        // // labels used
        // String start_ternary = CompilerUtils.generateLabel();
        String stop_ternary = CompilerUtils.generateLabel();
        String false_expr = CompilerUtils.generateLabel();

        // // Expr ? (...) : (...)
        expr.samCode += "ISNIL\n";
        expr.samCode += "JUMPC " + false_expr + "\n";

        // Truth expression:  (...) ? Expr : (..)
        Expression truthExpr = getExpr(f, method);
        expr.samCode += truthExpr.samCode;
        expr.samCode += "JUMP " + stop_ternary + "\n";

        // Checks ':'
        if (!CompilerUtils.check(f, ':')) {
            throw new SyntaxErrorException(
                "Ternary expects ':' between expressions",
                f.lineNo()
            );
        }

        // False expression: (...) ? (...) : Expr
        expr.samCode += false_expr + ":\n";
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
        expr.samCode += stop_ternary + ":\n";

        return expr;
    }

    static Expression getMethodCallExpr(
        SamTokenizer f,
        MethodNode scopeMethod,
        MethodNode callingMethod
    ) throws CompilerException {
        String sam = "";
        sam += "PUSHIMM 0\n"; // return value

        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "getMethodCallExpr expects '(' at the start of actuals",
                f.lineNo()
            );
        }

        sam += getActuals(f, scopeMethod, callingMethod);
        sam += "LINK\n";
        sam += "JSR " + callingMethod.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -" + callingMethod.numParameters() + "\n";

        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "getMethodCallExpr expects ')' at the end of actuals",
                f.lineNo()
            );
        }

        return new Expression(sam, callingMethod.type);
    }

    static String getActuals(
        SamTokenizer f,
        MethodNode scopeMethod,
        MethodNode callingMethod
    ) throws CompilerException {
        String sam = "";
        int paramCount = callingMethod.numParameters();
        int argCount = 0;

        do {
            if (argCount >= paramCount) {
                throw new SyntaxErrorException(
                    "Too many arguments provided for method '" +
                    callingMethod.name +
                    "'. Expected " +
                    paramCount +
                    " but got more.",
                    f.lineNo()
                );
            }

            VariableNode currParam = callingMethod.parameters.get(argCount);
            Expression expr = getExpr(f, scopeMethod);

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

            // save value in symbol
            currParam.value = expr.value;

            argCount++;
        } while (CompilerUtils.check(f, ','));

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
    static Expression getTerminal(SamTokenizer f, MethodNode method)
        throws CompilerException {
        TokenType type = f.peekAtKind();
        switch (type) {
            // Expr -> Literal -> Num
            case INTEGER:
                int value = CompilerUtils.getInt(f);
                return new Expression(
                    "PUSHIMM " + value + "\n",
                    Type.INT,
                    value
                );
            // Expr -> Literal -> String
            case STRING:
                String strValue = CompilerUtils.getString(f);
                return new Expression(
                    "PUSHIMMSTR \"" + strValue + "\"\n",
                    Type.STRING,
                    strValue
                );
            // Expr -> MethodName | Var | Literal
            case WORD:
                String name = CompilerUtils.getWord(f);

                // Expr -> Literal -> "true" | "false"
                if (name.equals("true")) {
                    return new Expression("PUSHIMM 1\n", Type.BOOL, true);
                }
                if (name.equals("false")) {
                    return new Expression("PUSHIMM 0\n", Type.BOOL, false);
                }

                // Expr -> MethodName | Var
                Node node = method.lookupSymbol(name);

                if (node == null) {
                    throw new CompilerException(
                        "getTerminal trying to access symbol that has not been declared: Node " +
                        node,
                        f.lineNo()
                    );
                }

                // Expr -> MethodName ( Actuals )
                if (node instanceof MethodNode) {
                    return getMethodCallExpr(f, method, (MethodNode) node);
                }
                // Expr -> Var
                else if (node instanceof VariableNode) {
                    return new Expression(
                        "PUSHOFF " + node.address + "\n",
                        node.type,
                        node.value
                    );
                } else {
                    throw new CompilerException(
                        "getTerminal trying to access invalid symbol: Node " +
                        node,
                        f.lineNo()
                    );
                }
            default:
                throw new TypeErrorException(
                    "getTerminal received invalid type " + type,
                    f.lineNo()
                );
        }
    }

    /*** Non-recursive operations. Override "getVar", inherit the rest from LiveOak0Compiler
     ***/
    static Node getVar(SamTokenizer f, MethodNode method)
        throws CompilerException {
        // Not a var, raise
        if (f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getVar should starts with a WORD",
                f.lineNo()
            );
        }

        String varName = CompilerUtils.getWord(f);
        Node variable = method.lookupSymbol(varName);
        if (variable == null) {
            throw new SyntaxErrorException(
                "getVar trying to access variable that has not been declared: Variable" +
                varName,
                f.lineNo()
            );
        }
        return variable;
    }
    /*** HELPERS. Inherit all the helpers from LiveOak0Compiler
     ***/
}
