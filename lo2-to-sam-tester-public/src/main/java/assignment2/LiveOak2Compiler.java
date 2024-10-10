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
        String sam = "PUSHIMM 0\n"; // return value

        // MethodDecl -> Type ...
        Type returnType = getType(f);

        // MethodDecl -> Type MethodName ...
        String name = getIdentifier(f);

        // create Method object
        Method method = new Method(name, returnType);

        // MethodDecl -> Type MethodName (...
        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "get method expects '(' at start of get formals",
                f.lineNo()
            );
        }

        // Save params in symbol table and method object
        getFormals(f, method);

        // MethodDecl -> Type MethodName (...
        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "get method expects ')' at end of get formals",
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
            int nextAddress = method.getNextAddress();
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
