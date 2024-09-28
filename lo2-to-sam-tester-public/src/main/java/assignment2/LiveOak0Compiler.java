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
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // maps identifier -> variable
    public static Map<String, Variable> variables = new HashMap<
        String,
        Variable
    >();

    static String compiler(String fileName) {
        CompilerUtils.clearTokens(); // Clear the list before starting
        variables.clear(); // reset symbol table

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
                "COMPILE ERROR:\n" + "File: %s\n" + "Message: %s\n",
                fileName,
                e.getMessage()
            );
            System.out.println(errorMessage);
            CompilerUtils.printTokens();
            return "STOP\n";
        } catch (Exception e) {
            String errorMessage = String.format(
                "UNEXPECTED ERROR:\n" +
                "File: %s\n" +
                "Type: %s\n" +
                "Message: %s\n",
                fileName,
                e.getClass().getSimpleName(),
                e.getMessage()
            );
            System.out.println(errorMessage);
            CompilerUtils.printTokens();
            return "STOP\n";
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
        pgm += CompilerUtils.getMethodsSam(variables);

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

        // typeString = int | bool | String
        String typeString = f.getWord();

        Type varType = Type.fromString(typeString);

        // typeString != int | bool | String
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
            int address = CompilerUtils.getNextAddress(variables);
            Variable variable = new Variable(varName, varType, null, address);
            variables.put(varName, variable);

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

        Variable variable = getVar(f);

        if (!CompilerUtils.check(f, '=')) {
            throw new SyntaxErrorException(
                "getStmt expects '=' after variable",
                f.lineNo()
            );
        }

        // getExpr() would return "exactly" one value on the stack
        sam += getExpr(f);

        // Store item on the stack to Variable
        sam += "STOREOFF " + variable.getAddress() + "\n";

        if (!CompilerUtils.check(f, ';')) {
            throw new SyntaxErrorException(
                "getStmt expects ';' at end of statement",
                f.lineNo()
            );
        }

        return sam;
    }

    static Variable getVar(SamTokenizer f) throws CompilerException {
        // Not a var, raise
        if (f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getVar should starts with a WORD",
                f.lineNo()
            );
        }

        String varName = CompilerUtils.getWord(f);

        // Trying to access var that has not been declared
        Variable variable = variables.get(varName);
        if (variable == null) {
            throw new SyntaxErrorException(
                "getVar trying to access variable that has not been declared",
                f.lineNo()
            );
        }

        return variable;
    }

    static String getExpr(SamTokenizer f) throws CompilerException {
        // Expr -> (...)
        String sam = "";

        // TODO: Before getTerminal and getUnopExpr, make sure the FBR on TOS
        // OR: maybe simplify this shit and remove all the JSR

        if (CompilerUtils.check(f, '(')) {
            // Expr -> ( Unop Expr )
            try {
                sam += getUnopExpr(f);
            } catch (TypeErrorException e) {
                // Expr -> ( Expr (...) )
                sam += getExpr(f);

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
                        sam += getTernaryExpr(f);
                    }
                    // Exprt -> (Expr Binop Expr)
                    else {
                        sam += getBinopExpr(f);
                    }

                    // Check closing ')'
                    if (!CompilerUtils.check(f, ')')) {
                        throw new SyntaxErrorException(
                            "getExpr expects ')' at end of Expr -> ( Expr (...) )",
                            f.lineNo()
                        );
                    }
                }
            }
        }
        // Expr -> Var | Literal
        else {
            sam += getTerminal(f);
        }

        return sam;
    }

    static String getTerminal(SamTokenizer f) throws CompilerException {
        TokenType type = f.peekAtKind();
        switch (type) {
            case INTEGER:
                int value = CompilerUtils.getInt(f);
                return "PUSHIMM " + value + "\n";
            case STRING:
                String strValue = CompilerUtils.getString(f);
                return "PUSHIMMSTR \"" + strValue + "\"\n";
            case WORD:
                String boolOrVar = CompilerUtils.getWord(f);

                // Expr -> Literal(bool)
                if (boolOrVar.equals("true")) {
                    return "PUSHIMM 1\n";
                }
                if (boolOrVar.equals("false")) {
                    return "PUSHIMM 0\n";
                }

                // Expr -> Var
                Variable variable = variables.get(boolOrVar);
                if (variable == null) {
                    throw new SyntaxErrorException(
                        "getVar trying to access variable that has not been declared",
                        f.lineNo()
                    );
                }

                if (variable.hasValue()) {
                    switch (variable.getType()) {
                        case INT:
                            return "PUSHIMM " + variable.getVal() + "\n";
                        case BOOL:
                            return variable.getVal().equals("true")
                                ? "PUSHIMM 1\n"
                                : "PUSHIMM 0\n";
                        case STRING:
                            return "PUSHIMMSTR \"" + variable.getVal() + "\"\n";
                        default:
                            throw new TypeErrorException(
                                "getExpr received invalid type",
                                f.lineNo()
                            );
                    }
                } else {
                    return "PUSHOFF " + variable.getAddress() + "\n";
                }
            default:
                throw new TypeErrorException(
                    "getTerminal received invalid type " + type,
                    f.lineNo()
                );
        }
    }

    static String getUnopExpr(SamTokenizer f) throws CompilerException {
        String sam = "";

        // unop sam code
        String unop_sam = getUnop(f);

        // getExpr() would return "exactly" one value on the stack
        sam += getExpr(f);

        // apply unop on expression
        sam += unop_sam;

        return sam;
    }

    static String getBinopExpr(SamTokenizer f) throws CompilerException {
        // binop sam code
        String binop_sam = getBinop(f);

        // Generate sam code
        String sam = "";

        // // labels used
        // String binop_label = CompilerUtils.generateLabel();

        // // Start Frame
        // sam += binop_label + ":\n";
        // sam += "LINK\n";

        // sam += "PUSHOFF -2\n";
        sam += getExpr(f);
        sam += binop_sam;

        // // Stop Frame
        // sam += "STOREOFF -2\n"; // store result on TOS
        // sam += "UNLINK\n";
        // sam += "RST\n";

        // // Save the method in symbol table
        // int address = CompilerUtils.getNextAddress(variables);
        // Variable sam_func = new Variable(binop_label, Type.SAM, sam, address);
        // variables.put(binop_label, sam_func);

        return sam;
    }

    static String getTernaryExpr(SamTokenizer f) throws CompilerException {
        // Generate sam code
        String sam = "";

        // // labels used
        // String start_ternary = CompilerUtils.generateLabel();
        String stop_ternary = CompilerUtils.generateLabel();
        String false_expr = CompilerUtils.generateLabel();

        // // Start Frame
        // sam += start_ternary + ":\n";
        // sam += "LINK\n";

        // // Expr ? (...) : (...)
        sam += "ISNIL\n";
        sam += "JUMPC " + false_expr + "\n";

        // Truth expression:  (...) ? Expr : (..)
        sam += getExpr(f);
        sam += "JUMP " + stop_ternary + "\n";

        // Checks ':'
        if (!CompilerUtils.check(f, ':')) {
            throw new SyntaxErrorException(
                "Ternary expects ':' between expressions",
                f.lineNo()
            );
        }

        // False expression: (...) ? (...) : Expr
        sam += false_expr + ":\n";
        sam += getExpr(f);

        // Stop Frame
        sam += stop_ternary + ":\n";

        // // Save the method in symbol table
        // int address = CompilerUtils.getNextAddress(variables);
        // Variable sam_func = new Variable(start_ternary, Type.SAM, sam, address);
        // variables.put(start_ternary, sam_func);

        return sam;
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
