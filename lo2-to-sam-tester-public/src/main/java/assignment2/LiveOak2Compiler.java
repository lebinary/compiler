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

    static String compiler(String fileName) throws Exception {
        CompilerUtils.clearTokens(); // Clear the list before starting
        symbolTable.clear(); // reset symbol table
        methodTable.clear(); // reset symbol table
        MainMethod.reset(); // reset main method

        //returns SaM code for program in file
        try {
            SamTokenizer f = new SamTokenizer(
                fileName,
                SamTokenizer.TokenizerOptions.PROCESS_STRINGS
            );
            String pgm = getProgram(f);

            return pgm;
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
            // CompilerUtils.printTokens();
            throw e;
        }
    }

    static String getProgram(SamTokenizer f) throws CompilerException {
        String pgm = "";
        pgm += "PUSHIMM 0\n";
        pgm += "LINK\n";
        pgm += "JSR main\n";
        pgm += "UNLINK\n";
        pgm += "STOP\n";

        // Declare methods
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
        String name = getIdentifier(f);

        // Check if the method is already defined
        if (methodTable.containsKey(name)) {
            throw new CompilerException(
                "Method '" + name + "' is already defined",
                f.lineNo()
            );
        }

        // Valid method, start generating...
        sam += name + ":\n";

        // MethodDecl -> Type MethodName (...
        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "get method expects '(' at start of get formals",
                f.lineNo()
            );
        }

        // Init method
        Method method = null;
        if (name == "main") {
            method = MainMethod.getInstance();
        } else {
            // create Method object
            method = new Method(name, returnType);

            // Save params in symbol table and method object
            getFormals(f, method);
        }
        // Save method in method table
        methodTable.put(name, method);

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

        return sam;
    }

    static void getFormals(SamTokenizer f, Method method)
        throws CompilerException {
        while (f.peekAtKind() == TokenType.WORD) {
            // Formals -> Type ...
            Type formalType = getType(f);

            // Formals -> Type Identifier
            String formalName = getIdentifier(f);

            // put formals in symbol table
            int nextAddress = method.getNextParamAddress();
            Node formal = new Node(formalName, formalType, nextAddress, method);
            symbolTable.put(formalName, formal);

            // put formals in method object
            method.parameters.add(formal);

            if (!CompilerUtils.check(f, ',')) {
                break;
            }
        }
    }

    static String getBody(SamTokenizer f, Method method)
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

    static String getBlock(SamTokenizer f, Method method) throws CompilerException {
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

    static String getStmt(SamTokenizer f, Method method)
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
            sam += getIfStmt(f);
        } else if (f.test("while")) {
            sam += getWhileStmt(f);
        } else {
            sam += getVarStmt(f);
        }

        return sam;
    }

    static String getReturnStmt(SamTokenizer f, Method method)
        throws CompilerException {
        if (!CompilerUtils.check(f, "return")) {
            throw new SyntaxErrorException(
                "getReturnStmt expects 'return' at beginining",
                f.lineNo()
            );
        }

        String sam = "";

        Expression expr = getExpr(f);
        sam += expr.samCode;

        // Return whatever on top of the stack
        sam += "STOREOFF " + method.returnAddress() + "\n";
        sam += "ADDSP -" + method.numLocalVariables() + "\n";
        sam += "RST\n";

        return sam;
    }
}
