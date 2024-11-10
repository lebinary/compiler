package compiler;

import compiler.errors.CompilerException;
import compiler.errors.SyntaxErrorException;
import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;
import java.util.ArrayDeque;
import java.util.Deque;

public class SymbolTableBuilder {

    static ClassSymbol createSymbolTable(SamTokenizer f)
        throws CompilerException {
        /*** Symbol table's structure:

                            globalsymbol
                            /            \
                       Class1            Class2
                     /        \
               method     another_method
              /      \
           local1    local2

        ***/
        ClassSymbol symbolTable = new ClassSymbol("Object");
        populate(f, symbolTable);
        return symbolTable;
    }

    /*** FIRST PASS: POPULATE SYMBOL TABLE
     ***/
    static void populate(SamTokenizer f, ClassSymbol symbolTable)
        throws CompilerException {
        // First pass: populate symbolTable
        while (f.peekAtKind() != TokenType.EOF) {
            populateClass(f, symbolTable);
        }
        // Make sure there is a main class
        ClassSymbol mainClass = symbolTable.lookupSymbol(
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
        if (
            !(mainMethod.numParameters() == 1 &&
                mainMethod.parameters.get(0).name == "this")
        ) {
            throw new CompilerException(
                "Main method should not have any parameters",
                f.lineNo()
            );
        }
    }

    static void populateClass(SamTokenizer f, ClassSymbol symbolTable)
        throws CompilerException {
        // ClassDecl -> class...
        if (!Tokenizer.check(f, "class")) {
            throw new SyntaxErrorException(
                "populateClass expects 'class' at the start",
                f.lineNo()
            );
        }

        // ClassDecl -> class ClassName ...
        String className = getIdentifier(f);

        // Check if the class is already defined
        if (symbolTable.lookupSymbol(className, ClassSymbol.class) != null) {
            throw new CompilerException(
                "Class '" + className + "' is already defined",
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

        // create ClassSymbol object
        ClassSymbol classSym = new ClassSymbol(className);

        // Populate all the instance properties
        while (f.peekAtKind() == TokenType.WORD) {
            populateClassProperties(f, classSym);
        }

        // ClassDecl -> class className ( VarDecl? ) ...
        if (!Tokenizer.check(f, ')')) {
            throw new SyntaxErrorException(
                "populateClass expects ')' at end of get VarDecl",
                f.lineNo()
            );
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
            populateMethod(f, classSym);
        }

        // ClassDecl -> class ClassName ( Formals? ) { MethodDecl...}
        if (!Tokenizer.check(f, '}')) {
            throw new SyntaxErrorException(
                "populateClass expects '}' at end of get class body",
                f.lineNo()
            );
        }

        // Save Class in global scope
        symbolTable.addChild(classSym);
    }

    static String populateClassProperties(
        SamTokenizer f,
        ClassSymbol classSymbol
    ) throws CompilerException {
        String sam = "";

        // VarDecl -> Type ...
        Type propType = populateType(f);

        // while varName = a | b | c | ...
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl -> Type Identifier1, Identifier2
            String propName = getIdentifier(f);
            VariableSymbol propSymbol = new VariableSymbol(propName, propType);
            classSymbol.addChild(propSymbol);

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

        return sam;
    }

    static void populateMethod(SamTokenizer f, ClassSymbol classSymbol)
        throws CompilerException {
        // MethodDecl -> Type ...
        Type returnType = populateType(f);

        // MethodDecl -> Type MethodName ...
        String methodName = getIdentifier(f);

        // Check if method is a constructor
        boolean isConstructor = methodName.equals(classSymbol.name);
        boolean isVirual = isConstructor ? false : true;

        // Check if the method is already defined
        if (classSymbol.lookupSymbol(methodName, MethodSymbol.class) != null) {
            throw new CompilerException(
                "Method '" + methodName + "' is already defined",
                f.lineNo()
            );
        }

        // MethodDecl -> Type MethodName (...
        if (!Tokenizer.check(f, '(')) {
            throw new SyntaxErrorException(
                "populateMethod expects '(' at start of get formals",
                f.lineNo()
            );
        }

        // create Method object
        MethodSymbol method = new MethodSymbol(
            methodName,
            returnType,
            isVirual
        );

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
        if (!Tokenizer.check(f, ')')) {
            throw new SyntaxErrorException(
                "get method expects ')' at end of get formals",
                f.lineNo()
            );
        }

        // Save Method in class scope
        classSymbol.addChild(method);

        // MethodDecl -> Type MethodName ( Formals? ) { ...
        if (!Tokenizer.check(f, '{')) {
            throw new SyntaxErrorException(
                "populateMethod expects '{' at start of body",
                f.lineNo()
            );
        }

        // MethodDecl -> Type MethodName ( Formals? ) { Body ...
        populateLocals(f, method);

        // MethodDecl -> Type MethodName ( Formals? ) { Body }
        if (!Tokenizer.check(f, '}')) {
            throw new SyntaxErrorException(
                "populateMethod expects '}' at end of body",
                f.lineNo()
            );
        }
    }

    static Type populateType(SamTokenizer f) throws CompilerException {
        // typeString = "int" | "bool" | "String"
        String typeString = Tokenizer.getWord(f);
        Type type = Type.createClassType(typeString);

        return type;
    }

    static void populateParams(SamTokenizer f, Symbol symbol)
        throws CompilerException {
        while (f.peekAtKind() == TokenType.WORD) {
            // Formals -> Type ...
            Type formalType = populateType(f);

            // Formals -> Type Identifier
            String formalName = getIdentifier(f);

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

            if (!Tokenizer.check(f, ',')) {
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
                String varName = getIdentifier(f);

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
                VariableSymbol variable = new VariableSymbol(varName, varType);
                method.addChild(variable);

                if (Tokenizer.check(f, ',')) {
                    continue;
                } else if (Tokenizer.check(f, ';')) {
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

        if (!Tokenizer.check(f, '{')) {
            throw new SyntaxErrorException(
                "skipBlock expects '{' at start of block",
                f.lineNo()
            );
        }
        stack.push('{');

        while (stack.size() > 0 && f.peekAtKind() != TokenType.EOF) {
            if (Tokenizer.check(f, '{')) {
                stack.push('{');
            } else if (Tokenizer.check(f, '}')) {
                stack.pop();
            } else {
                Tokenizer.skipToken(f);
            }
        }
        if (stack.size() > 0) {
            throw new SyntaxErrorException(
                "skipBlock missed a closing parentheses",
                f.lineNo()
            );
        }
    }

    private static String getIdentifier(SamTokenizer f)
        throws CompilerException {
        return Helper.getIdentifier(f);
    }
}
