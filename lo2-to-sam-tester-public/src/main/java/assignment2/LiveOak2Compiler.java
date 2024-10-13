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
import java.util.HashMap;
import java.util.Map;

public class LiveOak2Compiler extends LiveOak0Compiler {

    //             globalNode
    //             /         \
    //       mainMethod      anotherMethod
    //      /         \
    //   local1       local2

    public static Node globalNode = new Node();
    public static MethodNode mainMethod = MainMethod.getInstance();

    public static void reset() {
        CompilerUtils.clearTokens();
        globalNode = new Node();
        MainMethod.resetInstance();
        mainMethod = MainMethod.getInstance();
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
            TreeUtils.printTree(globalNode);

            SamTokenizer secondPass = new SamTokenizer(
                fileName,
                SamTokenizer.TokenizerOptions.PROCESS_STRINGS
            );
            return getProgram(secondPass);
        } catch (CompilerException e) {
            String errorMessage = String.format(
                "Failed to compile %s.\nError Message: %s\n",
                fileName,
                e.getMessage()
            );
            System.err.println(errorMessage);
            CompilerUtils.printTokens();
            throw e;
        } catch (Exception e) {
            String errorMessage = String.format(
                "Failed to compile %s.\nError Message: %s\n",
                fileName,
                e.getMessage()
            );
            System.err.println(errorMessage);
            CompilerUtils.printTokens();
            throw e;
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
        if (methodName == "main") {
            method = MainMethod.getInstance();
        } else {
            // create Method object
            method = new MethodNode(methodName, returnType);

            // Save params in symbol table and method object
            populateParams(f, method);
        }
        // Save Method in global scope
        globalNode.addChild(method);

        // MethodDecl -> Type MethodName ( Formals? ) ...
        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "get method expects ')' at end of get formals",
                f.lineNo()
            );
        }

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

        // check if there is a body
        if (CompilerUtils.check(f, '{')) {
            // Skip the entire body
            while (!CompilerUtils.check(f, '}')) {
                CompilerUtils.skipToken(f);
            }
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
        Node method = globalNode.lookupSymbol(methodName);
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
        sam += getBody(f, (MethodNode) method);

        // MethodDecl -> Type MethodName ( Formals? ) { Body }
        if (!CompilerUtils.check(f, '}')) {
            throw new SyntaxErrorException(
                "get method expects '}' at end of body",
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

        // Then, get Block
        sam += getBlock(f, method);

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

        // while not "}"
        while (!CompilerUtils.check(f, '}')) {
            sam += getStmt(f, method);
        }

        return sam;
    }

    static String getStmt(SamTokenizer f, MethodNode method)
        throws CompilerException {
        String sam = "";

        if (CompilerUtils.check(f, ';')) {
            return sam; // Null statement
        }

        if (f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getStmt expects TokenType.WORD at beginning of statement",
                f.lineNo()
            );
        }

        if (f.test("return")) {
            sam += getReturnStmt(f, method);
        } else if (f.test("if")) {
            sam += getIfStmt(f, method);
        } else if (f.test("while")) {
            sam += getWhileStmt(f, method);
        } else {
            sam += getVarStmt(f, method);
        }

        return sam;
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

        // Return whatever on top of the stack
        sam += "STOREOFF " + method.returnAddress() + "\n";
        sam += "ADDSP -" + method.numLocalVariables() + "\n";
        sam += "RST\n";

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

        // getExpr() would return "exactly" one value on the stack
        Expression resExpr = getExpr(f, method);
        sam += resExpr.samCode;

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
            // Expr -> ( Unop Expr )
            try {
                return getUnopExpr(f, method);
            } catch (TypeErrorException e) {
                // Expr -> ( Expr (...) )
                Expression expr = getExpr(f, method);

                // Raise if Expr -> ( Expr NOT('?' | ')' | Binop) )
                if (f.peekAtKind() != TokenType.OPERATOR) {
                    throw new SyntaxErrorException(
                        "Expr -> Expr (...) expects '?' | ')' | Binop",
                        f.lineNo()
                    );
                }

                // Expr -> ( Expr ) , ends early
                if (!CompilerUtils.check(f, ')')) {
                    // Exprt -> (Expr ? Expr : Expr)
                    if (CompilerUtils.check(f, '?')) {
                        expr.samCode += getTernaryExpr(f, method).samCode;
                    }
                    // Exprt -> (Expr Binop Expr)
                    else {
                        expr.samCode += getBinopExpr(f, expr, method).samCode;
                    }

                    // Check closing ')'
                    if (!CompilerUtils.check(f, ')')) {
                        throw new SyntaxErrorException(
                            "getExpr expects ')' at end of Expr -> ( Expr (...) )",
                            f.lineNo()
                        );
                    }
                }

                return expr;
            }
        }
        // Expr -> Var | Literal
        else {
            return getTerminal(f, method);
        }
    }

    static Expression getUnopExpr(SamTokenizer f, MethodNode method)
        throws CompilerException {
        // unop sam code
        String unop_sam = getUnop(f);

        // getExpr() would return "exactly" one value on the stack
        Expression expr = getExpr(f, method);

        // apply unop on expression
        expr.samCode += unop_sam;

        return expr;
    }

    static Expression getBinopExpr(
        SamTokenizer f,
        Expression prevExpr,
        MethodNode method
    ) throws CompilerException {
        // binop sam code
        String binop_sam = getBinop(f);
        Expression expr = getExpr(f, method);

        // Type check
        if (!expr.type.isCompatibleWith(prevExpr.type)) {
            throw new TypeErrorException(
                "Binop expr type mismatch: " +
                prevExpr.type +
                " and " +
                expr.type,
                f.lineNo()
            );
        }

        expr.samCode += binop_sam;

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
        expr.samCode += getExpr(f, method).samCode;
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
        expr.samCode += getExpr(f, method).samCode;

        // Stop Frame
        expr.samCode += stop_ternary + ":\n";

        return expr;
    }

    /*** Non-recursive operations. Override "gerTerminal" and "getVar", inherit the rest from LiveOak0Compiler
     ***/
    static Expression getTerminal(SamTokenizer f, MethodNode method)
        throws CompilerException {
        TokenType type = f.peekAtKind();
        switch (type) {
            // Literal -> Num
            case INTEGER:
                int value = CompilerUtils.getInt(f);
                return new Expression("PUSHIMM " + value + "\n", Type.INT);
            // Literal -> String
            case STRING:
                String strValue = CompilerUtils.getString(f);
                return new Expression(
                    "PUSHIMMSTR " + strValue + "\n",
                    Type.STRING
                );
            case WORD:
                String boolOrVar = CompilerUtils.getWord(f);

                // Literal -> "true" | "false"
                if (boolOrVar.equals("true")) {
                    return new Expression("PUSHIMM 1\n", Type.BOOL);
                }
                if (boolOrVar.equals("false")) {
                    return new Expression("PUSHIMM 0\n", Type.BOOL);
                }

                // Var -> Identifier
                Node variable = method.lookupSymbol(boolOrVar);
                if (variable == null) {
                    throw new SyntaxErrorException(
                        "getTerminal trying to access variable that has not been declared: Variable" +
                        boolOrVar,
                        f.lineNo()
                    );
                }

                return new Expression(
                    "PUSHOFF " + variable.address + "\n",
                    variable.type
                );
            default:
                throw new TypeErrorException(
                    "getTerminal received invalid type " + type,
                    f.lineNo()
                );
        }
    }

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
