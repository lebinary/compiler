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
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbolTableBuilder {

    /*** FIRST PASS: POPULATE SYMBOL TABLE
     ***/
    static void populate(SamTokenizer f, Symbol globalSymbol)
        throws CompilerException {
        // First pass: populate symbolTable
        while (f.peekAtKind() != TokenType.EOF) {
            populateMethod(f, globalSymbol);
        }

        // Make sure there is a main method and it has no arguments
        MethodSymbol mainMethod = globalSymbol.lookupSymbol(
            "main",
            MethodSymbol.class
        );
        if (mainMethod == null) {
            throw new CompilerException("Main method missing", f.lineNo());
        }
        if (mainMethod.numParameters() != 0) {
            throw new CompilerException(
                "Main method should not have any parameters",
                f.lineNo()
            );
        }
        CompilerUtils.clearTokens();
    }

    static void populateMethod(SamTokenizer f, Symbol globalSymbol)
        throws CompilerException {
        // MethodDecl -> Type ...
        Type returnType = CodeGenerator.getType(f);

        // MethodDecl -> Type MethodName ...
        String methodName = CodeGenerator.getIdentifier(f);

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
            Type formalType = CodeGenerator.getType(f);

            // Formals -> Type Identifier
            String formalName = CodeGenerator.getIdentifier(f);

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
            Type varType = CodeGenerator.getType(f);

            // while varName = a | b | c | ...
            while (f.peekAtKind() == TokenType.WORD) {
                // VarDecl -> Type Identifier1, Identifier2
                String varName = CodeGenerator.getIdentifier(f);

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
}
