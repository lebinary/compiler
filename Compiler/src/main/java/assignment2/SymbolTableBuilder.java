package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.util.ArrayDeque;
import java.util.Deque;

public class SymbolTableBuilder {

    static ClassSymbol globalSymbol = null;

    /*** FIRST PASS: POPULATE SYMBOL TABLE
     ***/
    static void populate(SamTokenizer f, ClassSymbol globalSymbol)
        throws CompilerException {
        SymbolTableBuilder.globalSymbol = globalSymbol;

        // First pass: populate symbolTable
        while (f.peekAtKind() != TokenType.EOF) {
            populateClass(f);
        }

        // Make sure there is a main class
        ClassSymbol mainClass = globalSymbol.lookupSymbol(
            "Main",
            ClassSymbol.class
        );
        if (mainClass == null) {
            throw new CompilerException("Main class missing", f.lineNo());
        }

        // Make sure the main method has no arguments
        MethodSymbol mainMethod = mainClass.lookupSymbol(
            "main",
            MethodSymbol.class
        );
        if (mainMethod == null) {
            throw new CompilerException("Main method missing", f.lineNo());
        }
        if (!(mainMethod.numParameters() == 1 && mainMethod.parameters.get(0).name == "this")) {
            throw new CompilerException(
                "Main method should not have any parameters",
                f.lineNo()
            );
        }
        CompilerUtils.clearTokens();
    }

    static void populateClass(SamTokenizer f) throws CompilerException {
        // ClassDecl -> class...
        if (!CompilerUtils.check(f, "class")) {
            throw new SyntaxErrorException(
                "populateClass expects 'class' at the start",
                f.lineNo()
            );
        }

        // ClassDecl -> class ClassName ...
        String className = CodeGenerator.getIdentifier(f);

        // Check if the class is already defined
        if (globalSymbol.lookupSymbol(className, ClassSymbol.class) != null) {
            throw new CompilerException(
                "Class '" + className + "' is already defined",
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

        // create ClassSymbol object
        ClassSymbol classSym = new ClassSymbol(className);

        // ClassDecl -> class className ( Formals? ...
        populateParams(f, classSym);

        // ClassDecl -> class className ( Formals? ) ...
        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "get method expects ')' at end of get formals",
                f.lineNo()
            );
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
            populateMethod(f, classSym);
        }

        // ClassDecl -> class ClassName ( Formals? ) { MethodDecl...}
        if (!CompilerUtils.check(f, '}')) {
            throw new SyntaxErrorException(
                "populateClass expects '}' at end of get class body",
                f.lineNo()
            );
        }

        // Save Class in global scope
        globalSymbol.addChild(classSym);
    }

    static void populateMethod(SamTokenizer f, ClassSymbol classSymbol)
        throws CompilerException {
        // MethodDecl -> Type ...
        Type returnType = populateType(f);

        // MethodDecl -> Type MethodName ...
        String methodName = CodeGenerator.getIdentifier(f);

        // Check if the method is already defined
        if (classSymbol.lookupSymbol(methodName, MethodSymbol.class) != null) {
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

        // create Method object
        MethodSymbol method = new MethodSymbol(methodName, returnType);

        // first param in the method is reserved for "this" object
        VariableSymbol thisVariable = new VariableSymbol(
            "this",
            Type.createClassType(classSymbol.name),
            true
        );
        method.addChild(thisVariable);

        // Save params in symbol table and method object
        populateParams(f, method);

        // MethodDecl -> Type MethodName ( Formals? ) ...
        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "get method expects ')' at end of get formals",
                f.lineNo()
            );
        }

        // Save Method in class scope
        classSymbol.addChild(method);

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

    static Type populateType(SamTokenizer f) throws CompilerException {
        // typeString = "int" | "bool" | "String"
        String typeString = CompilerUtils.getWord(f);
        Type type = Type.createClassType(typeString);

        return type;
    }

    static void populateParams(SamTokenizer f, Symbol symbol)
        throws CompilerException {
        while (f.peekAtKind() == TokenType.WORD) {
            // Formals -> Type ...
            Type formalType = populateType(f);

            // Formals -> Type Identifier
            String formalName = CodeGenerator.getIdentifier(f);

            // Check if the formal has already defined in this scope
            if (symbol.lookupSymbol(formalName, VariableSymbol.class) != null) {
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
            symbol.addChild(paramSymbol);

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
            Type varType = populateType(f);

            // while varName = a | b | c | ...
            while (f.peekAtKind() == TokenType.WORD) {
                // VarDecl -> Type Identifier1, Identifier2
                String varName = CodeGenerator.getIdentifier(f);

                // Check if the variable is already defined in the current scope
                if (
                    method.lookupSymbol(varName, VariableSymbol.class) != null
                ) {
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
}
