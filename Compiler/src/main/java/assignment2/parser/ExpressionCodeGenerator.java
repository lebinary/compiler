package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import assignment2.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;

public class ExpressionCodeGenerator extends CodeGenerator {

    protected static Expression getExpr(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        // Expr -> this
        if (Tokenizer.check(f, "this")) {
            VariableSymbol thisSymbol = method.getThisSymbol();
            return new Expression(
                Backend.pushOffset(thisSymbol.address),
                thisSymbol.type
            );
        }

        // Expr -> null
        if (Tokenizer.check(f, "null")) {
            return new Expression(Backend.pushImmediate(0), Type.VOID);
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
                    expr.samCode = expr.samCode + ternaryExpr.samCode;
                    expr.type = ternaryExpr.type;
                }
                // Exprt -> (Expr Binop Expr)
                else {
                    Expression binopExpr = getBinopExpr(f, expr, method);
                    expr.samCode = expr.samCode + binopExpr.samCode;
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

        String className = getIdentifier(f);
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
            String sam = initObject(classSymbol);

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
            expr.samCode = expr.samCode + StringHelper.reverseString();
        } /*** Basic cases ***/else {
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
            expr.samCode = expr.samCode + getUnop(op);
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
                .append(Backend.isPositive())
                .append(Backend.dup())
                .append(Backend.jumpConditional(doneLabel.name));
        }

        Expression expr = getExpr(f, method);
        expr.samCode = prevSam.toString() + expr.samCode;

        /*** Special cases: ***/
        // String repeat
        if (
            op == '*' &&
            ((prevExpr.type == Type.STRING && expr.type == Type.INT) ||
                (prevExpr.type == Type.INT && expr.type == Type.STRING))
        ) {
            expr.samCode =
                expr.samCode +
                StringHelper.repeatString(prevExpr.type, expr.type);
            expr.type = Type.STRING;
        }
        // String concatenation
        else if (
            op == '+' &&
            prevExpr.type == Type.STRING &&
            expr.type == Type.STRING
        ) {
            expr.samCode = expr.samCode + StringHelper.concatString();
            expr.type = Type.STRING;
        }
        // String comparison
        else if (
            Helper.getBinopType(op) == BinopType.COMPARISON &&
            prevExpr.type == Type.STRING &&
            expr.type == Type.STRING
        ) {
            expr.samCode = expr.samCode + StringHelper.compareString(op);
            expr.type = Type.STRING;
        } else {
            /*** Basic cases ***/
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
                Helper.getBinopType(op) == BinopType.BITWISE &&
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
            StringBuilder divZeroCheck = new StringBuilder();
            if (op == '/' && expr.type == Type.INT) {
                divZeroCheck
                    .append(Backend.dup())
                    .append(Backend.isNil())
                    .append(Backend.jumpConditional(handleDivideZero.name));
            }
            expr.samCode =
                expr.samCode + divZeroCheck.toString() + getBinop(op);
        }

        // Change return type to boolean if binop is Comparison
        if (Helper.getBinopType(op) == BinopType.COMPARISON) {
            expr.type = Type.BOOL;
        }

        expr.samCode =
            expr.samCode +
            Backend.jump(doneLabel.name) +
            Backend.label(handleDivideZero.name) +
            Backend.swap() +
            Backend.divide() +
            Backend.label(doneLabel.name);

        return expr;
    }

    private static Expression getTernaryExpr(
        SamTokenizer f,
        MethodSymbol method
    ) throws CompilerException {
        // Generate sam code
        Expression expr = new Expression();

        Label stopTernary = new Label();
        Label falseLabel = new Label();

        StringBuilder samBuilder = new StringBuilder()
            .append(Backend.isNil())
            .append(Backend.jumpConditional(falseLabel.name));

        // Truth expression: (...) ? Expr : (..)
        Expression truthExpr = getExpr(f, method);
        samBuilder
            .append(truthExpr.samCode)
            .append(Backend.jump(stopTernary.name));

        // Checks ':'
        if (!Tokenizer.check(f, ':')) {
            throw new SyntaxErrorException(
                "Ternary expects ':' between expressions",
                f.lineNo()
            );
        }

        // False expression: (...) ? (...) : Expr
        samBuilder.append(Backend.label(falseLabel.name)); // Use falseLabel here
        Expression falseExpr = getExpr(f, method);
        samBuilder.append(falseExpr.samCode);

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
        samBuilder.append(Backend.label(stopTernary.name));
        expr.samCode = samBuilder.toString();

        return expr;
    }

    private static Expression getMethodCallExpr(
        SamTokenizer f,
        MethodSymbol scopeMethod,
        VariableSymbol scopeVariable,
        MethodSymbol callingMethod
    ) throws CompilerException {
        StringBuilder sam = new StringBuilder()
            .append(Backend.pushImmediate(0)); // return value

        if (!Tokenizer.check(f, '(')) {
            throw new SyntaxErrorException(
                "getMethodCallExpr expects '(' at the start of actuals",
                f.lineNo()
            );
        }

        sam
            .append(getActuals(f, scopeMethod, scopeVariable, callingMethod))
            .append(Backend.link())
            .append(Backend.jumpSubroutine(callingMethod.getLabelName()))
            .append(Backend.unlink())
            .append(Backend.addStackPointer(-callingMethod.numParameters()));

        if (!Tokenizer.check(f, ')')) {
            throw new SyntaxErrorException(
                "getMethodCallExpr expects ')' at the end of actuals",
                f.lineNo()
            );
        }

        return new Expression(sam.toString(), callingMethod.returnType);
    }

    private static Expression getTerminal(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        TokenType type = f.peekAtKind();
        switch (type) {
            // Expr -> Literal -> Num
            case INTEGER:
                int value = Tokenizer.getInt(f);
                return new Expression(Backend.pushImmediate(value), Type.INT);
            // Expr -> Literal -> String
            case STRING:
                String strValue = Tokenizer.getString(f);
                return new Expression(
                    Backend.pushImmediateString(strValue),
                    Type.STRING
                );
            // Expr -> Var | Literal
            case WORD:
                String name = Tokenizer.getWord(f);

                // Expr -> Literal -> "true" | "false"
                if (name.equals("true")) {
                    return new Expression(Backend.pushImmediate(1), Type.BOOL);
                }
                if (name.equals("false")) {
                    return new Expression(Backend.pushImmediate(0), Type.BOOL);
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
                            .append(Backend.pushOffset(method.getThisAddress()))
                            .append(Backend.pushImmediate(varSymbol.address))
                            .append(Backend.add())
                            .append(Backend.pushIndirect());
                    } else {
                        sam.append(Backend.pushOffset(varSymbol.address));
                    }

                    return new Expression(sam.toString(), varSymbol.type);
                }
            default:
                throw new TypeErrorException(
                    "getTerminal received invalid type " + type,
                    f.lineNo()
                );
        }
    }

    private static String getUnop(char op) throws CompilerException {
        StringBuilder sam = new StringBuilder();

        switch (op) {
            // TODO: string bitwise
            case '~':
                return sam
                    .append(Backend.pushImmediate(-1))
                    .append(Backend.multiply())
                    .toString();
            case '!':
                return sam
                    .append(Backend.pushImmediate(1))
                    .append(Backend.add())
                    .append(Backend.pushImmediate(2))
                    .append(Backend.mod())
                    .toString();
            default:
                throw new TypeErrorException(
                    "getUnop received invalid input: " + op,
                    -1
                );
        }
    }

    private static String getBinop(char op) throws CompilerException {
        switch (op) {
            case '+':
                return Backend.add();
            case '-':
                return Backend.subtract();
            case '*':
                return Backend.multiply();
            case '/':
                return Backend.divide();
            case '%':
                return Backend.mod();
            case '&':
                return Backend.and();
            case '|':
                return Backend.or();
            case '>':
                return Backend.greater();
            case '<':
                return Backend.less();
            case '=':
                return Backend.equal();
            default:
                throw new TypeErrorException(
                    "getBinop received invalid input: " + op,
                    -1
                );
        }
    }
}
