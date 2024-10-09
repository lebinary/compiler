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
import java.util.List;
import java.util.Map;

public class LiveOak0Compiler {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java ProgramName inputFile outputFile");
            System.exit(1);
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
        } catch (CompilerException e) {
            System.err.println("Compiler error: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // maps identifier -> variable
    public static Map<String, Node> symbolTable = new HashMap<String, Node>();

    static String compiler(String fileName) throws Exception {
        CompilerUtils.clearTokens(); // Clear the list before starting
        symbolTable.clear(); // reset symbol table

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
            CompilerUtils.printTokens("debug");
            throw e;
        } catch (Exception e) {
            String errorMessage = String.format(
                "Failed to compile %s.\nError Message: %s\n",
                fileName,
                e.getMessage()
            );
            System.err.println(errorMessage);
            CompilerUtils.printTokens("debug");
            throw e;
        }
    }

    static String getProgram(SamTokenizer f) throws CompilerException {
        String endProgramLabel = CompilerUtils.generateLabel();

        String pgm = "";

        // LiveOak-0
        while (f.peekAtKind() != TokenType.EOF) {
            pgm += getBody(f);
        }
        pgm += "JUMP " + endProgramLabel + "\n";

        // define all the methods
        // pgm += CompilerUtils.getMethodsSam(symbolTable);

        pgm += endProgramLabel + ":\n";
        pgm += "STOP\n";

        return pgm;
    }

    /** LiveOak 0
     **/
    static String getBody(SamTokenizer f) throws CompilerException {
        String sam = "";

        // while start with "int | bool | String"
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl will store variable in Hashmap: identifier -> { type: TokenType, relative_address: int }
            sam += parseVarDecl(f);
        }

        // check EOF
        if (f.peekAtKind() == TokenType.EOF) {
            return sam;
        }

        // Then, get Block
        sam += getBlock(f);

        return sam;
    }

    static String parseVarDecl(SamTokenizer f) throws CompilerException {
        String sam = "";

        // typeString = "int" | "bool" | "String"
        String typeString = f.getWord();

        Type varType = Type.fromString(typeString);

        // typeString != INT | BOOL | STRING
        if (varType == null) {
            throw new TypeErrorException(
                "Invalid type: " + typeString,
                f.lineNo()
            );
        }

        // while varName = a | b | c | ...
        while (f.peekAtKind() == TokenType.WORD) {
            String varName = CompilerUtils.getWord(f);

            // put variable in hashmap
            int address = CompilerUtils.getNextAddress(symbolTable);
            Node variable = new Node(varName, varType, null, address);
            symbolTable.put(varName, variable);

            // write same code
            sam += "PUSHOFF " + variable.getAddress() + "\n";

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

        return sam + "\n";
    }

    static String getBlock(SamTokenizer f) throws CompilerException {
        String sam = "";

        if (!CompilerUtils.check(f, '{')) {
            throw new SyntaxErrorException(
                "getBlock expects '{' at start of block",
                f.lineNo()
            );
        }

        // while not "}"
        while (!CompilerUtils.check(f, '}')) {
            sam += getStmt(f);
        }

        return sam;
    }

    static String getStmt(SamTokenizer f) throws CompilerException {
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

        String word = CompilerUtils.getWord(f);
        if (word.equals("if")) {
            sam += getIfStmt(f);
        } else if (word.equals("while")) {
            sam += getWhileStmt(f);
        } else {
            sam += getVarStmt(f);
        }

        return sam;
    }

    static String getIfStmt(SamTokenizer f) throws CompilerException {
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

        sam += getExpr(f).samCode;

        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "if statement expects ')' at end of condition",
                f.lineNo()
            );
        }

        sam += "ISNIL\n";
        sam += "JUMPC " + false_block + "\n";

        // Truth block:  // if ( Expr ) Block ...
        sam += getBlock(f);
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
        sam += getBlock(f);

        // Done if statement
        sam += stop_stmt + ":\n";

        return sam;
    }

    static String getWhileStmt(SamTokenizer f) throws CompilerException {
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
        sam += getExpr(f).samCode;

        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "while statement expects ')' at end of condition",
                f.lineNo()
            );
        }

        sam += "ISNIL\n";
        sam += "JUMPC " + stop_loop + "\n";

        // Continue loop
        sam += getBlock(f);
        sam += "JUMP " + start_loop + "\n";

        // Stop loop
        sam += stop_loop + ":\n";

        return sam;
    }

    static String getVarStmt(SamTokenizer f) throws CompilerException {
        f.pushBack();

        String sam = "";
        Node variable = getVar(f);

        if (!CompilerUtils.check(f, '=')) {
            throw new SyntaxErrorException(
                "getStmt expects '=' after variable",
                f.lineNo()
            );
        }

        // getExpr() would return "exactly" one value on the stack
        sam += getExpr(f).samCode;

        // Store item on the stack to Node
        sam += "STOREOFF " + variable.getAddress() + "\n";

        if (!CompilerUtils.check(f, ';')) {
            throw new SyntaxErrorException(
                "getStmt expects ';' at end of statement",
                f.lineNo()
            );
        }

        return sam;
    }

    static Node getVar(SamTokenizer f) throws CompilerException {
        // Not a var, raise
        if (f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getVar should starts with a WORD",
                f.lineNo()
            );
        }

        String varName = f.getWord();

        // Trying to access var that has not been declared
        Node variable = symbolTable.get(varName);
        if (variable == null) {
            throw new SyntaxErrorException(
                "getVar trying to access variable that has not been declared",
                f.lineNo()
            );
        }

        return variable;
    }

    static Expression getExpr(SamTokenizer f) throws CompilerException {
        // TODO: Before getTerminal and getUnopExpr, make sure the FBR on TOS
        // OR: maybe simplify this shit and remove all the JSR

        if (CompilerUtils.check(f, '(')) {
            // Expr -> ( Unop Expr )
            try {
                return getUnopExpr(f);
            } catch (TypeErrorException e) {
                // Expr -> ( Expr (...) )
                Expression expr = getExpr(f);

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
                        expr.samCode += getTernaryExpr(f).samCode;
                    }
                    // Exprt -> (Expr Binop Expr)
                    else {
                        expr.samCode += getBinopExpr(f, expr).samCode;
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
            return getTerminal(f);
        }
    }

    static Expression getTerminal(SamTokenizer f) throws CompilerException {
        TokenType type = f.peekAtKind();
        switch (type) {
            case INTEGER:
                int value = CompilerUtils.getInt(f);
                return new Expression("PUSHIMM " + value + "\n", Type.INT);
            case STRING:
                String strValue = CompilerUtils.getString(f);
                return new Expression(
                    "PUSHIMMSTR " + strValue + "\n",
                    Type.STRING
                );
            case WORD:
                String boolOrVar = CompilerUtils.getWord(f);

                // Expr -> Literal(bool)
                if (boolOrVar.equals("true")) {
                    return new Expression("PUSHIMM 1\n", Type.BOOL);
                }
                if (boolOrVar.equals("false")) {
                    return new Expression("PUSHIMM 0\n", Type.BOOL);
                }

                // Expr -> Var
                Node variable = symbolTable.get(boolOrVar);
                if (variable == null) {
                    throw new SyntaxErrorException(
                        "getVar trying to access variable that has not been declared",
                        f.lineNo()
                    );
                }

                if (variable.hasValue()) {
                    switch (variable.getType()) {
                        case INT:
                            return new Expression(
                                "PUSHIMM " + variable.getVal() + "\n",
                                Type.INT
                            );
                        case BOOL:
                            return new Expression(
                                variable.getVal().equals("true")
                                    ? "PUSHIMM 1\n"
                                    : "PUSHIMM 0\n",
                                Type.BOOL
                            );
                        case STRING:
                            return new Expression(
                                "PUSHIMMSTR " + variable.getVal() + "\n",
                                Type.STRING
                            );
                        default:
                            throw new TypeErrorException(
                                "getExpr received invalid type",
                                f.lineNo()
                            );
                    }
                } else {
                    return new Expression(
                        "PUSHOFF " + variable.getAddress() + "\n",
                        variable.getType()
                    );
                }
            default:
                throw new TypeErrorException(
                    "getTerminal received invalid type " + type,
                    f.lineNo()
                );
        }
    }

    static Expression getUnopExpr(SamTokenizer f) throws CompilerException {
        // unop sam code
        String unop_sam = getUnop(f);

        // getExpr() would return "exactly" one value on the stack
        Expression expr = getExpr(f);

        // apply unop on expression
        expr.samCode += unop_sam;

        return expr;
    }

    static Expression getBinopExpr(SamTokenizer f, Expression prevExpr)
        throws CompilerException {
        // binop sam code
        String binop_sam = getBinop(f);

        // // labels used
        // String binop_label = CompilerUtils.generateLabel();

        // // Start Frame
        // sam += binop_label + ":\n";
        // sam += "LINK\n";

        // sam += "PUSHOFF -2\n";
        Expression expr = getExpr(f);

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

        // // Stop Frame
        // sam += "STOREOFF -2\n"; // store result on TOS
        // sam += "UNLINK\n";
        // sam += "RST\n";

        // // Save the method in symbol table
        // int address = CompilerUtils.getNextAddress(symbolTable);
        // Node sam_func = new Node(binop_label, Type.SAM, sam, address);
        // symbolTable.put(binop_label, sam_func);

        return expr;
    }

    static Expression getTernaryExpr(SamTokenizer f) throws CompilerException {
        // Generate sam code
        Expression expr = new Expression();

        // // labels used
        // String start_ternary = CompilerUtils.generateLabel();
        String stop_ternary = CompilerUtils.generateLabel();
        String false_expr = CompilerUtils.generateLabel();

        // // Start Frame
        // sam += start_ternary + ":\n";
        // sam += "LINK\n";

        // // Expr ? (...) : (...)
        expr.samCode += "ISNIL\n";
        expr.samCode += "JUMPC " + false_expr + "\n";

        // Truth expression:  (...) ? Expr : (..)
        expr.samCode += getExpr(f).samCode;
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
        expr.samCode += getExpr(f).samCode;

        // Stop Frame
        expr.samCode += stop_ternary + ":\n";

        // // Save the method in symbol table
        // int address = CompilerUtils.getNextAddress(symbolTable);
        // Node sam_func = new Node(start_ternary, Type.SAM, sam, address);
        // symbolTable.put(start_ternary, sam_func);

        return expr;
    }

    /** PRIVATE
     **/
    private static boolean isBool(String bool) {
        return List.of("true", "false").contains(bool);
    }

    private static boolean isUnop(char op) {
        return "~!".indexOf(op) != -1;
    }

    private static String getUnop(SamTokenizer f) throws CompilerException {
        if (CompilerUtils.check(f, '~')) {
            return "PUSHIMM -1\nTIMES\nPUSHIMM 1\nSUB\n";
        } else if (CompilerUtils.check(f, '!')) {
            return "PUSHIMM 1\nADD\nPUSHIMM 2\nMOD\n";
        } else {
            throw new TypeErrorException(
                "getUnop received invalid input",
                f.lineNo()
            );
        }
    }

    private static boolean isBinop(char op) {
        return "+-*/%&|<>=".indexOf(op) != -1;
    }

    private static String getBinop(SamTokenizer f) throws CompilerException {
        if (CompilerUtils.check(f, '+')) {
            return "ADD\n";
        } else if (CompilerUtils.check(f, '-')) {
            return "SUB\n";
        } else if (CompilerUtils.check(f, '*')) {
            return "TIMES\n";
        } else if (CompilerUtils.check(f, '/')) {
            return "DIV\n";
        } else if (CompilerUtils.check(f, '%')) {
            return "MOD\n";
        } else if (CompilerUtils.check(f, '&')) {
            return "AND\n";
        } else if (CompilerUtils.check(f, '|')) {
            return "OR\n";
        } else if (CompilerUtils.check(f, '>')) {
            return "GREATER\n";
        } else if (CompilerUtils.check(f, '<')) {
            return "LESS\n";
        } else if (CompilerUtils.check(f, '=')) {
            return "EQUAL\n";
        } else {
            throw new TypeErrorException(
                "getBinop received invalid input",
                f.lineNo()
            );
        }
    }
}
