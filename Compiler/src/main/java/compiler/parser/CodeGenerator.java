package compiler;

import compiler.errors.CompilerException;
import compiler.errors.SyntaxErrorException;
import compiler.errors.TypeErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*** Code generator based on grammar
 ***/
public class CodeGenerator {

    protected static ClassSymbol symbolTable = null;

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

    protected static String getMainProgram(SamTokenizer f)
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
        sam.append(Backend.pushImmediate(0));

        // instantiate main class to pass in as "this" parameter
        sam
            .append(Backend.pushImmediate(1))
            .append(Backend.malloc())
            // assign main class's vtable
            .append(Backend.dup())
            .append(Backend.pushAbsolute(mainClass.vtableAddress))
            .append(Backend.storeIndirect());

        // run main method
        sam
            .append(Backend.link())
            .append(Backend.jumpSubroutine(mainMethod.getLabelName()))
            .append(Backend.unlink())
            .append(Backend.addStackPointer(-mainMethod.numParameters()))
            .append(DataGenerator.freeStaticData(symbolTable))
            .append(Backend.stop());

        return sam.toString();
    }

    protected static String getClassDecl(SamTokenizer f)
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
        String className = getIdentifier(f);

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

        // Process formals and class body tokens
        if (!Tokenizer.check(f, '(')) {
            throw new SyntaxErrorException(
                "populateClass expects '(' at start of get formals",
                f.lineNo()
            );
        }
        while (!Tokenizer.check(f, ')')) {
            Tokenizer.skipToken(f);
        }
        if (!Tokenizer.check(f, '{')) {
            throw new SyntaxErrorException(
                "populateClass expects '{' at start of get class body",
                f.lineNo()
            );
        }

        // Process method declarations
        while (f.peekAtKind() == TokenType.WORD) {
            sam.append(getMethodDecl(f, classSymbol));
        }

        if (!Tokenizer.check(f, '}')) {
            throw new SyntaxErrorException(
                "populateClass expects '}' at end of get class body",
                f.lineNo()
            );
        }

        return sam.toString();
    }

    protected static String getMethodDecl(
        SamTokenizer f,
        ClassSymbol classSymbol
    ) throws CompilerException {
        // Generate sam code
        StringBuilder sam = new StringBuilder("\n"); // Keep newline for readability

        // MethodDecl -> Type ...
        Type returnType = Helper.getType(f);

        // MethodDecl -> Type MethodName ...
        String methodName = getIdentifier(f);

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
        sam.append(Backend.label(method.getLabelName()));

        // Process method declaration syntax
        if (!Tokenizer.check(f, '(')) {
            throw new SyntaxErrorException(
                "get method expects '(' at start of get formals",
                f.lineNo()
            );
        }

        // Process formals
        while (f.peekAtKind() == TokenType.WORD) {
            Type formalType = Helper.getType(f);
            String formalName = getIdentifier(f);

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

        if (!Tokenizer.check(f, '{')) {
            throw new SyntaxErrorException(
                "get method expects '{' at start of body",
                f.lineNo()
            );
        }

        // Get method body
        sam.append(getBody(f, method));

        if (!Tokenizer.check(f, '}')) {
            throw new SyntaxErrorException(
                "get method expects '}' at end of body",
                f.lineNo()
            );
        }

        return sam.toString();
    }

    protected static String getBody(SamTokenizer f, MethodSymbol method)
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
            sam.append(Backend.pushOffset(method.getThisAddress()));
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
            .append(Backend.label(returnLabel.name))
            .append(Backend.storeOffset(method.returnAddress()))
            .append(Backend.addStackPointer(-method.numLocalVariables()))
            .append(Backend.returnSubroutine());
        method.popLabel();

        return sam.toString();
    }

    protected static String getVarDecl(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        StringBuilder sam = new StringBuilder();

        // VarDecl -> Type ...
        Type varType = Helper.getType(f);

        // while varName = a | b | c | ...
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl -> Type Identifier1, Identifier2
            String varName = getIdentifier(f);

            // write sam code
            sam.append(Backend.pushImmediate(0));

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

    protected static String getBlock(SamTokenizer f, MethodSymbol method)
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

    protected static String getActuals(
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
            sam.append(initObject((ClassSymbol) callingMethod.parent));
        } else {
            if (scopeVariable == null) {
                throw new CompilerException(
                    "Cannot invoke method from null instance",
                    f.lineNo()
                );
            }
            if (scopeVariable.isInstanceVariable()) {
                sam
                    .append(Backend.pushOffset(scopeMethod.getThisAddress()))
                    .append(Backend.pushImmediate(scopeVariable.address))
                    .append(Backend.add())
                    .append(Backend.pushIndirect());
            } else {
                sam.append(Backend.pushOffset(scopeVariable.address));
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
            sam.append(expr.samCode);

            argCount++;
        } while (Tokenizer.check(f, ','));

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

        return sam.toString();
    }

    protected static String getIdentifier(SamTokenizer f)
        throws CompilerException {
        return Helper.getIdentifier(f);
    }

    protected static Expression getExpr(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        return ExpressionCodeGenerator.getExpr(f, method);
    }

    protected static String getStmt(SamTokenizer f, MethodSymbol method)
        throws CompilerException {
        return StatementCodeGenerator.getStmt(f, method);
    }

    /*** HELPERS
     ***/
    protected static String initObject(ClassSymbol classSymbol) {
        StringBuilder sam = new StringBuilder();

        // instantiate class to pass in as "this" parameter
        sam
            .append(Backend.pushImmediate(classSymbol.getSize()))
            .append(Backend.malloc())
            // assign main class's vtable
            .append(Backend.dup())
            .append(Backend.pushAbsolute(classSymbol.vtableAddress))
            .append(Backend.storeIndirect());

        return sam.toString();
    }
}
