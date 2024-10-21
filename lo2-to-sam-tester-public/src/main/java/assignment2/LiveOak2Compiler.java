package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiveOak2Compiler {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println(
                "Error: Two arguments are required - input file name and output file name."
            );
            throw new Error();
        }
        String inFileName = args[0];
        String outFileName = args[1];

        try {
            String samCode = compiler(inFileName);
            try (
                BufferedWriter writer = new BufferedWriter(
                    new FileWriter(outFileName)
                )
            ) {
                writer.write(samCode);
            }
        } catch (IOException e) {
            String errorMessage =
                "Failed to compile src/test/resources/LO-2/InvalidPrograms/" +
                inFileName;
            System.err.println(errorMessage);
            throw new Error(errorMessage, e);
        } catch (CompilerException e) {
            String errorMessage =
                "Failed to compile src/test/resources/LO-2/InvalidPrograms/" +
                inFileName;
            System.err.println(errorMessage);
            // CompilerUtils.printTokens();
            throw new Error(errorMessage, e);
        } catch (Error e) {
            String errorMessage =
                "Failed to compile src/test/resources/LO-2/InvalidPrograms/" +
                inFileName;
            System.err.println(errorMessage);
            // CompilerUtils.printTokens();
            throw new Error(errorMessage, e);
        } catch (Exception e) {
            String errorMessage =
                "Failed to compile src/test/resources/LO-2/InvalidPrograms/" +
                inFileName;
            System.err.println(errorMessage);
            // CompilerUtils.printTokens();
            throw new Error(errorMessage, e);
        }
    }

    //             globalSymbol
    //             /         \
    //       mainMethod      anotherMethod
    //      /         \
    //   local1       local2

    public static Symbol globalSymbol = new Symbol();

    public static void reset() {
        CompilerUtils.clearTokens();
        globalSymbol = new Symbol();
        MainMethod.resetInstance();
    }

    static String compiler(String fileName) throws Exception {
        try {
            reset(); // Clear the list before starting

            //returns SaM code for program in file
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
            String errorMessage = createErrorMessage(fileName);
            System.err.println(errorMessage);
            // CompilerUtils.printTokens();
            throw new Error(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = createErrorMessage(fileName);
            System.err.println(errorMessage);
            // CompilerUtils.printTokens();
            throw new Error(errorMessage, e);
        }
    }

    private static String createErrorMessage(String fileName) {
        String normalizedPath = Paths.get(fileName).normalize().toString();
        String errorMessage = "Failed to compile " + normalizedPath;
        return errorMessage;
    }

    /*** FIRST PASS: POPULATE SYMBOL TABLE
     ***/
    static void populateSymbolTable(SamTokenizer f) throws CompilerException {
        // First pass: populate symbolTable
        while (f.peekAtKind() != TokenType.EOF) {
            populateMethod(f);
        }

        // Make sure there is a main method and it has no arguments
        MethodSymbol mainMethod = globalSymbol.lookupSymbol(
            "main",
            MethodSymbol.class
        );
        if (mainMethod == null) {
            throw new CompilerException(
                "Main method missing",
                f.lineNo()
            );
        }
        if (mainMethod.numParameters() != 0) {
            throw new CompilerException(
                "Main method should not have any parameters",
                f.lineNo()
            );
        }
        CompilerUtils.clearTokens();
    }

    static void populateMethod(SamTokenizer f) throws CompilerException {
        // MethodDecl -> Type ...
        Type returnType = getType(f);

        // MethodDecl -> Type MethodName ...
        String methodName = getIdentifier(f);

        // Check if the method is already defined
        if (globalSymbol.existSymbol(methodName)) {
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
        MethodSymbol method = null;
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
            method = new MethodSymbol(methodName, returnType);

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
        globalSymbol.addChild(method);

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

    static void populateParams(SamTokenizer f, MethodSymbol method)
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

            // set Formal as child of MethodSymbol
            VariableSymbol paramSymbol = new VariableSymbol(
                formalName,
                formalType,
                true
            );
            method.addChild(paramSymbol);

            if (!CompilerUtils.check(f, ',')) {
                break;
            }
        }
    }

    static void populateLocals(SamTokenizer f, MethodSymbol method)
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

                // save local variable as child of methodSymbol
                VariableSymbol variable = new VariableSymbol(
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
        // Check if main method exists
        MethodSymbol mainMethod = globalSymbol.lookupSymbol(
            "main",
            MethodSymbol.class
        );
        if (mainMethod == null) {
            throw new CompilerException("Main method not found", f.lineNo());
        }

        // Check if main method has the correct signature (no parameters)
        if (mainMethod.numParameters() != 0) {
            throw new CompilerException(
                "Main method should not have parameters",
                f.lineNo()
            );
        }

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
        MethodSymbol method = globalSymbol.lookupSymbol(
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

        // Check if any return method
        if (!method.hasStatement(Statement.RETURN)) {
            throw new SyntaxErrorException(
                "get method missing return statement",
                f.lineNo()
            );
        }

        // Check return method at the end
        if (method.peekStatement() != Statement.RETURN) {
            throw new SyntaxErrorException(
                "get method missing return statement at the end",
                f.lineNo()
            );
        }

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
        sam += getBlock(f, method);

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
        if (!expr.type.isCompatibleWith(method.type)) {
            throw new TypeErrorException(
                "Return type mismatch: expected " +
                method.type +
                ", but got " +
                expr.type,
                f.lineNo()
            );
        }
        sam += expr.samCode;

        // Jump to clean up
        // CompilerUtils.printTokens();
        Label returnLabel = method.mostRecent(LabelType.RETURN);
        if (returnLabel == null) {
            throw new CompilerException(
                "getReturnStmt missing exit label",
                f.lineNo()
            );
        }
        sam += "JUMP " + returnLabel.name + "\n";

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
        Symbol variable = getVar(f, method);

        if (!CompilerUtils.check(f, '=')) {
            throw new SyntaxErrorException(
                "getStmt expects '=' after variable",
                f.lineNo()
            );
        }

        Expression expr = getExpr(f, method);
        // Type check
        if (!expr.type.isCompatibleWith(variable.type)) {
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

        // Store item on the stack to Symbol
        sam += "STOREOFF " + variable.address + "\n";

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

    static Expression getUnopExpr(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        // Not an operator, raise
        if (f.peekAtKind() != TokenType.OPERATOR) {
            throw new TypeErrorException(
                "getUnopExpr expects an OPERATOR",
                f.lineNo()
            );
        }
        char op = CompilerUtils.getOp(f);

        // getExpr() would return "exactly" one value on the stack
        Expression expr = getExpr(f, method);

        /*** Special case
         ***/
        if (op == '~' && expr.type == Type.STRING) {
            expr.samCode += reverseString();
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
            expr.samCode += getUnop(op);
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
            op == '+' &&
            prevExpr.type == Type.STRING &&
            expr.type == Type.STRING
        ) {
            expr.samCode += concatString();
            expr.type = Type.STRING;
        }
        // String comparison
        else if (
            getBinopType(op) == BinopType.COMPARISON &&
            prevExpr.type == Type.STRING &&
            expr.type == Type.STRING
        ) {
            expr.samCode += compareString(op);
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
        MethodSymbol scopeMethod,
        MethodSymbol callingMethod
    ) throws CompilerException {
        String sam = "";
        int paramCount = callingMethod.numParameters();
        int argCount = 0;

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

            // save value in symbol
            currParam.value = expr.value;

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
                Symbol symbol = method.lookupSymbol(name);

                if (symbol == null) {
                    throw new CompilerException(
                        "getTerminal trying to access symbol that has not been declared: Symbol " +
                        symbol,
                        f.lineNo()
                    );
                }

                // Expr -> MethodName ( Actuals )
                if (symbol instanceof MethodSymbol) {
                    return getMethodCallExpr(f, method, (MethodSymbol) symbol);
                }
                // Expr -> Var
                else if (symbol instanceof VariableSymbol) {
                    return new Expression(
                        "PUSHOFF " + symbol.address + "\n",
                        symbol.type,
                        symbol.value
                    );
                } else {
                    throw new CompilerException(
                        "getTerminal trying to access invalid symbol: Symbol " +
                        symbol,
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

    static Type getType(SamTokenizer f) throws CompilerException {
        // typeString = "int" | "bool" | "String"
        String typeString = CompilerUtils.getWord(f);
        Type type = Type.fromString(typeString);

        // typeString != INT | BOOL | STRING
        if (type == null) {
            throw new TypeErrorException(
                "Invalid type: " + typeString,
                f.lineNo()
            );
        }

        return type;
    }

    static String getIdentifier(SamTokenizer f) throws CompilerException {
        String identifier = CompilerUtils.getWord(f);
        if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new SyntaxErrorException(
                "Invalid identifier: " + identifier,
                f.lineNo()
            );
        }
        return identifier;
    }

    /*** Non-recursive operations. Override "getVar", inherit the rest from LiveOak0Compiler
     ***/
    static Symbol getVar(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        // Not a var, raise
        if (f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getVar should starts with a WORD",
                f.lineNo()
            );
        }

        String varName = CompilerUtils.getWord(f);
        Symbol variable = method.lookupSymbol(varName);
        if (variable == null) {
            throw new SyntaxErrorException(
                "getVar trying to access variable that has not been declared: Variable" +
                varName,
                f.lineNo()
            );
        }
        return variable;
    }

    /*** HELPERS
     ***/
    public static final Pattern IDENTIFIER_PATTERN = Pattern.compile(
        "^[a-zA-Z]([a-zA-Z0-9'_'])*$"
    );

    public static String getUnop(char op) throws CompilerException {
        switch (op) {
            // TODO: string bitwise
            case '~':
                return "PUSHIMM -1\nTIMES\n";
            case '!':
                return "PUSHIMM 1\nADD\nPUSHIMM 2\nMOD\n";
            default:
                throw new TypeErrorException(
                    "getUnop received invalid input: " + op,
                    -1
                );
        }
    }

    public static String getBinop(char op) throws CompilerException {
        switch (op) {
            case '+':
                return "ADD\n";
            case '-':
                return "SUB\n";
            case '*':
                return "TIMES\n";
            case '/':
                return "DIV\n";
            case '%':
                return "MOD\n";
            case '&':
                return "AND\n";
            case '|':
                return "OR\n";
            case '>':
                return "GREATER\n";
            case '<':
                return "LESS\n";
            case '=':
                return "EQUAL\n";
            default:
                throw new TypeErrorException(
                    "getBinop received invalid input: " + op,
                    -1
                );
        }
    }

    public static BinopType getBinopType(char op) throws CompilerException {
        switch (op) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '%':
                return BinopType.ARITHMETIC;
            case '&':
            case '|':
                return BinopType.BITWISE;
            case '>':
            case '<':
            case '=':
                return BinopType.COMPARISON;
            default:
                throw new TypeErrorException(
                    "categorizeBinop received invalid input: " + op,
                    -1
                );
        }
    }

    public static String repeatString(
        Type firstInputType,
        Type secondInputType
    ) {
        // expects parameters already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();
        Label returnLabel = new Label();
        Label invalidParamLabel = new Label();

        String sam = "";

        // prepare params, String always on top
        if (firstInputType == Type.STRING) {
            sam += "SWAP\n";
        }

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -1\n"; // free second param, only first param remain with new value
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 1: loop counter
        sam += "PUSHIMM 0\n"; // local 2: increment address
        sam += "PUSHIMM 0\n"; // local 3: return address

        // validate param, if n < 0 -> return
        sam += "PUSHOFF -2\n";
        sam += "ISNEG\n";
        sam += "JUMPC " + invalidParamLabel.name + "\n";

        // allocate memory for new string -> Address
        sam += "PUSHOFF -1\n";
        sam += getStringLength();
        sam += "PUSHOFF -2\n";
        sam += "TIMES\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "MALLOC\n";
        sam += "STOREOFF 3\n";

        // return this address
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF 4\n";

        // loop...
        sam += startLoopLabel.name + ":\n";
        // check if done
        sam += "PUSHOFF 2\n";
        sam += "PUSHOFF -2\n";
        sam += "EQUAL\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // append str to memory
        sam += "PUSHIMM 0\n"; // will return next address
        sam += "PUSHOFF 3\n"; // param1: starting memory address
        sam += "PUSHOFF -1\n"; // param2: string
        sam += appendStringHeap();
        sam += "STOREOFF 3\n";

        // increase counter
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 2\n";

        // Continue loop
        sam += "JUMP " + startLoopLabel.name + "\n";

        // Stop loop
        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 4\n";
        sam += "STOREOFF -2\n";
        sam += "JUMP " + returnLabel.name + "\n";

        // Invalid param, return empty string
        sam += invalidParamLabel.name + ":\n";
        sam += "PUSHIMMSTR \"\"";
        sam += "STOREOFF -2\n";
        sam += "JUMP " + returnLabel.name + "\n";

        // Return func
        sam += returnLabel.name + ":\n";
        sam += "ADDSP -3\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }

    public static String getStringLength() {
        // expects parameters already on the stack
        Label startCountLabel = new Label();
        Label stopCountLabel = new Label();
        String sam = "";

        sam += "DUP\n";

        // START
        sam += startCountLabel.name + ":\n";
        sam += "DUP\n";
        sam += "PUSHIND\n";

        // check end of string
        sam += "ISNIL\n";
        sam += "JUMPC " + stopCountLabel.name + "\n";

        // increament count and continue loop
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "JUMP " + startCountLabel.name + "\n";

        // STOP
        sam += stopCountLabel.name + ":\n";
        sam += "SWAP\n";
        sam += "SUB\n";

        return sam;
    }

    public static String appendStringHeap() {
        // expects parameters already on the stack, String on top, Mempry address
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -2\n";
        sam += "JUMP " + exitFuncLabel.name + "\n";

        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHOFF -2\n";
        sam += "PUSHOFF -1\n";

        sam += startLoopLabel.name + ":\n";
        // put char in TOS
        // end loop if nil
        sam += "PUSHOFF 3\n";
        sam += "PUSHIND\n";
        sam += "ISNIL\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // Save to allocated memory
        sam += "PUSHOFF 2\n";
        sam += "PUSHOFF 3\n";
        sam += "PUSHIND\n";
        sam += "STOREIND\n";

        // increase address current string
        sam += "PUSHOFF 3\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 3\n";

        // increase final address string
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 2\n";

        sam += "JUMP " + startLoopLabel.name + "\n";

        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMMCH '\\0'" + "\n";
        sam += "STOREIND\n";
        sam += "PUSHOFF 2\n";
        sam += "STOREOFF -3\n";
        sam += "ADDSP -2\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }

    public static String concatString() {
        // expects parameters (2 strings) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -1\n"; // free second param, only first param remain with new value
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 2: increment address
        sam += "PUSHIMM 0\n"; // local 3: return address

        // allocate space for resulting string
        sam += "PUSHOFF -1\n";
        sam += getStringLength();
        sam += "PUSHOFF -2\n";
        sam += getStringLength();
        sam += "ADD\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "MALLOC\n";
        sam += "STOREOFF 2\n";

        // return this address
        sam += "PUSHOFF 2\n";
        sam += "STOREOFF 3\n";

        // append first string to memory
        sam += "PUSHIMM 0\n"; // will return next address
        sam += "PUSHOFF 2\n"; // param1: starting memory address
        sam += "PUSHOFF -2\n"; // param2: string
        sam += appendStringHeap();
        sam += "STOREOFF 2\n";

        // append second string to memory
        sam += "PUSHIMM 0\n";
        sam += "PUSHOFF 2\n";
        sam += "PUSHOFF -1\n";
        sam += appendStringHeap();
        sam += "STOREOFF 2\n";

        // store in the first string pos
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF -2\n";

        // clean local vars
        sam += "ADDSP -2\n";
        // return
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }

    public static String compareString(char op) throws CompilerException {
        if (getBinopType(op) != BinopType.COMPARISON) {
            throw new SyntaxErrorException(
                "compareString receive invalid operation: " + op,
                -1
            );
        }

        // expects parameters (2 strings) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -1\n"; // free second param, only first param remain with new value
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 2: counter
        sam += "PUSHIMM 0\n"; // local 3: result

        // loop...
        sam += startLoopLabel.name + ":\n";
        // reach end of string 1?
        sam += "PUSHOFF -2\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";
        sam += "ISNIL\n";

        // reach end of string 2?
        sam += "PUSHOFF -1\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";
        sam += "ISNIL\n";

        // reach end of both string, is equal
        sam += "AND\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // not end, comparing char by char
        // get char of string 1
        sam += "PUSHOFF -2\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";

        // get char of string 2
        sam += "PUSHOFF -1\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";

        // compare and store result
        sam += "CMP\n";
        sam += "STOREOFF 3\n";

        // check if done
        sam += "PUSHOFF 3\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // not done, continue to next char
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 2\n";
        sam += "JUMP " + startLoopLabel.name + "\n";

        // Stop loop
        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF -2\n";
        sam += "ADDSP -2\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        if (op == '<') {
            sam += "PUSHIMM 1\n";
        } else if (op == '>') {
            sam += "PUSHIMM -1\n";
        } else {
            sam += "PUSHIMM 0\n";
        }
        sam += "EQUAL\n";

        return sam;
    }

    public static String reverseString() {
        // expects parameter (1 string) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 2: counter
        sam += "PUSHIMM 0\n"; // local 3: increment address
        sam += "PUSHIMM 0\n"; // local 4: result

        // get string length and store in local 2
        sam += "PUSHOFF -1\n";
        sam += getStringLength();
        sam += "STOREOFF 2\n";

        // allocate space for resulting string
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "MALLOC\n";
        sam += "STOREOFF 3\n";

        // return this address
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF 4\n";

        // set EOS char first
        sam += "PUSHOFF 3\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIMMCH '\\0'" + "\n";
        sam += "STOREIND\n";

        // loop (backward)...
        sam += startLoopLabel.name + ":\n";

        // end loop if counter == 0
        sam += "PUSHOFF 2\n";
        sam += "ISNIL\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // get current address
        sam += "PUSHOFF 3\n";

        // get current char
        sam += "PUSHOFF -1\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIMM 1\n"; // subtract 1 because indexing
        sam += "SUB\n";
        sam += "PUSHIND\n";

        // store char in address
        sam += "STOREIND\n";

        // increment address
        sam += "PUSHOFF 3\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 3\n";

        // decrement counter
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "SUB\n";
        sam += "STOREOFF 2\n";

        // Continue loop
        sam += "JUMP " + startLoopLabel.name + "\n";

        // Stop loop
        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 4\n";
        sam += "STOREOFF -1\n";
        sam += "ADDSP -3\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }
}
