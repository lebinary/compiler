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
        String sam = "";

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

        return "";
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
}
