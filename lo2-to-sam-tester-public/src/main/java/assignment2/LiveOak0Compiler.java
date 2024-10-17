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
import java.util.regex.Pattern;

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

    //             globalNode
    //                 |
    //             mainMethod
    //             /         \
    //          local1       local2 ...

    public static Node globalNode = new Node();
    public static MethodNode mainMethod = MainMethod.getInstance();

    static {
        globalNode.addChild(mainMethod);
    }

    public static void reset() {
        CompilerUtils.clearTokens();
        globalNode = new Node();
        MainMethod.resetInstance();
        mainMethod = MainMethod.getInstance();
        globalNode.addChild(mainMethod);
    }

    static String compiler(String fileName) throws Exception {
        reset(); // Clear the list before starting

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
            throw new Error(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = String.format(
                "Failed to compile %s.\nError Message: %s\n",
                fileName,
                e.getMessage()
            );
            System.err.println(errorMessage);
            CompilerUtils.printTokens();
            throw new Error(errorMessage, e);
        }
    }

    static String getProgram(SamTokenizer f) throws CompilerException {
        String pgm = "";
        pgm += "PUSHIMM 0\n";
        pgm += "LINK\n";
        pgm += "JSR main\n";
        pgm += "UNLINK\n";
        pgm += "STOP\n";

        // LiveOak-0
        pgm += "main:\n";
        pgm += getBody(f);

        // Return whatever on top of the stack
        pgm += "DUP\n";
        pgm += "STOREOFF -1\n";
        pgm += "ADDSP -" + mainMethod.numLocalVariables() + "\n";
        pgm += "RST\n";

        return pgm;
    }

    /*** Recursive operations
     ***/
    static String getBody(SamTokenizer f) throws CompilerException {
        String sam = "";

        // while start with "int | bool | String"
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl will store variable in Hashmap: identifier -> { type: TokenType, relative_address: int }
            sam += getVarDecl(f);
        }

        // check EOF
        if (f.peekAtKind() == TokenType.EOF) {
            return sam;
        }

        // Then, get Block
        sam += getBlock(f);

        return sam;
    }

    static String getVarDecl(SamTokenizer f) throws CompilerException {
        String sam = "";

        // VarDecl -> Type ...
        Type varType = getType(f);

        // while varName = a | b | c | ...
        while (f.peekAtKind() == TokenType.WORD) {
            // VarDecl -> Type Identifier1, Identifier2
            String varName = getIdentifier(f);

            // Check if the variable is already defined in the current scope
            Node existNode = mainMethod.lookupSymbol(varName);
            if (existNode != null) {
                throw new CompilerException(
                    "Variable '" +
                    varName +
                    "' is already defined in this scope",
                    f.lineNo()
                );
            }

            // put variable in symbol table
            VariableNode variable = new VariableNode(varName, varType, false);
            mainMethod.addChild(variable);

            // write sam code
            sam += "PUSHIMM 0\n";

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

        if (f.test("if")) {
            sam += getIfStmt(f);
        } else if (f.test("while")) {
            sam += getWhileStmt(f);
        } else {
            sam += getVarStmt(f);
        }

        return sam;
    }

    static String getIfStmt(SamTokenizer f) throws CompilerException {
        if (!CompilerUtils.check(f, "if")) {
            throw new SyntaxErrorException(
                "if statement expects 'if' at beginining",
                f.lineNo()
            );
        }

        // Generate sam code
        String sam = "";

        // labels used
        Label stop_stmt = new Label();
        Label false_block = new Label();

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
        sam += "JUMPC " + false_block.name + "\n";

        // Truth block:  // if ( Expr ) Block ...
        sam += getBlock(f);
        sam += "JUMP " + stop_stmt.name + "\n";

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
        if (!CompilerUtils.check(f, "while")) {
            throw new SyntaxErrorException(
                "while statement expects 'while' at beginining",
                f.lineNo()
            );
        }

        // Generate sam code
        String sam = "";

        // labels used
        Label start_loop = new Label();
        Label stop_loop = new Label();

        // while ( Expr ) ...
        if (!CompilerUtils.check(f, '(')) {
            throw new SyntaxErrorException(
                "while statement expects '(' at beginining of condition",
                f.lineNo()
            );
        }

        sam += start_loop.name + ":\n";
        sam += getExpr(f).samCode;

        if (!CompilerUtils.check(f, ')')) {
            throw new SyntaxErrorException(
                "while statement expects ')' at end of condition",
                f.lineNo()
            );
        }

        sam += "ISNIL\n";
        sam += "JUMPC " + stop_loop.name + "\n";

        // Continue loop
        sam += getBlock(f);
        sam += "JUMP " + start_loop.name + "\n";

        // Stop loop
        sam += stop_loop.name + ":\n";

        return sam;
    }

    static String getVarStmt(SamTokenizer f) throws CompilerException {
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
        sam += "STOREOFF " + variable.address + "\n";

        if (!CompilerUtils.check(f, ';')) {
            throw new SyntaxErrorException(
                "getStmt expects ';' at end of statement",
                f.lineNo()
            );
        }

        return sam;
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

    static Expression getUnopExpr(SamTokenizer f) throws CompilerException {
        // unop sam code
        String unop_sam = getUnop(CompilerUtils.getOp(f));

        // getExpr() would return "exactly" one value on the stack
        Expression expr = getExpr(f);

        // apply unop on expression
        expr.samCode += unop_sam;

        return expr;
    }

    static Expression getBinopExpr(SamTokenizer f, Expression prevExpr)
        throws CompilerException {
        // binop sam code
        String binop_sam = getBinop(CompilerUtils.getOp(f));

        // // labels used
        // String binop_label = new Label();

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
        // String start_ternary = new Label();
        Label stop_ternary = new Label();
        Label false_expr = new Label();

        // // Start Frame
        // sam += start_ternary + ":\n";
        // sam += "LINK\n";

        // // Expr ? (...) : (...)
        expr.samCode += "ISNIL\n";
        expr.samCode += "JUMPC " + false_expr.name + "\n";

        // Truth expression:  (...) ? Expr : (..)
        expr.samCode += getExpr(f).samCode;
        expr.samCode += "JUMP " + stop_ternary.name + "\n";

        // Checks ':'
        if (!CompilerUtils.check(f, ':')) {
            throw new SyntaxErrorException(
                "Ternary expects ':' between expressions",
                f.lineNo()
            );
        }

        // False expression: (...) ? (...) : Expr
        expr.samCode += false_expr.name + ":\n";
        expr.samCode += getExpr(f).samCode;

        // Stop Frame
        expr.samCode += stop_ternary.name + ":\n";

        // // Save the method in symbol table
        // int address = CompilerUtils.getNextAddress(symbolTable);
        // Node sam_func = new Node(start_ternary, Type.SAM, sam, address);
        // symbolTable.put(start_ternary, sam_func);

        return expr;
    }

    /*** Non-recursive operations
     ***/
    static Node getVar(SamTokenizer f) throws CompilerException {
        // Not a var, raise
        if (f.peekAtKind() != TokenType.WORD) {
            throw new SyntaxErrorException(
                "getVar should starts with a WORD",
                f.lineNo()
            );
        }

        String varName = CompilerUtils.getWord(f);

        // Trying to access var that has not been declared
        Node variable = mainMethod.lookupSymbol(varName);
        if (variable == null) {
            throw new SyntaxErrorException(
                "getVar trying to access variable that has not been declared: Variable " +
                varName,
                f.lineNo()
            );
        }

        return variable;
    }

    static Type getType(SamTokenizer f) throws CompilerException {
        // typeString = "int" | "bool" | "String"
        String typeString = CompilerUtils.getWord(f);
        Type type = Type.fromString(typeString);

        // typeString != INT | BOOL | STRING
        if (type == null) {
            throw new TypeErrorException(
                "Invalid type: " + typeString,
                f.lineNo()
            );
        }

        return type;
    }

    static Expression getTerminal(SamTokenizer f) throws CompilerException {
        TokenType type = f.peekAtKind();
        switch (type) {
            // Literal -> Num
            case INTEGER:
                int value = CompilerUtils.getInt(f);
                return new Expression("PUSHIMM " + value + "\n", Type.INT);
            // Literal -> String
            case STRING:
                String strValue = CompilerUtils.getString(f);
                return new Expression(
                    "PUSHIMMSTR \"" + strValue + "\"\n",
                    Type.STRING
                );
            case WORD:
                String boolOrVar = CompilerUtils.getWord(f);

                // Literal -> "true" | "false"
                if (boolOrVar.equals("true")) {
                    return new Expression("PUSHIMM 1\n", Type.BOOL);
                }
                if (boolOrVar.equals("false")) {
                    return new Expression("PUSHIMM 0\n", Type.BOOL);
                }

                // Var -> Identifier
                Node variable = mainMethod.lookupSymbol(boolOrVar);
                if (variable == null) {
                    throw new SyntaxErrorException(
                        "getVar trying to access variable that has not been declared: Variable " +
                        boolOrVar,
                        f.lineNo()
                    );
                }

                return new Expression(
                    "PUSHOFF " + variable.address + "\n",
                    variable.type
                );
            default:
                throw new TypeErrorException(
                    "getTerminal received invalid type " + type,
                    f.lineNo()
                );
        }
    }

    static String getIdentifier(SamTokenizer f) throws CompilerException {
        String identifier = CompilerUtils.getWord(f);
        if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new SyntaxErrorException(
                "Invalid identifier: " + identifier,
                f.lineNo()
            );
        }
        return identifier;
    }

    /*** HELPERS
     ***/
    public static final Pattern IDENTIFIER_PATTERN = Pattern.compile(
        "^[a-zA-Z]([a-zA-Z0-9'_'])*$"
    );

    public static String getUnop(char op) throws CompilerException {
        switch (op) {
            // TODO: string bitwise
            case '~':
                return "PUSHIMM -1\nTIMES\nPUSHIMM 1\nSUB\n";
            case '!':
                return "PUSHIMM 1\nADD\nPUSHIMM 2\nMOD\n";
            default:
                throw new TypeErrorException(
                    "getUnop received invalid input: " + op,
                    -1
                );
        }
    }

    public static String getBinop(char op) throws CompilerException {
        switch (op) {
            case '+':
                return "ADD\n";
            case '-':
                return "SUB\n";
            case '*':
                return "TIMES\n";
            case '/':
                return "DIV\n";
            case '%':
                return "MOD\n";
            case '&':
                return "AND\n";
            case '|':
                return "OR\n";
            case '>':
                return "GREATER\n";
            case '<':
                return "LESS\n";
            case '=':
                return "EQUAL\n";
            default:
                throw new TypeErrorException(
                    "getBinop received invalid input: " + op,
                    -1
                );
        }
    }

    public static BinopType getBinopType(char op) throws CompilerException {
        switch (op) {
            case '+':
            case '-':
            case '*':
            case '/':
            case '%':
                return BinopType.ARITHMETIC;
            case '&':
            case '|':
                return BinopType.BITWISE;
            case '>':
            case '<':
            case '=':
                return BinopType.COMPARISON;
            default:
                throw new TypeErrorException(
                    "categorizeBinop received invalid input: " + op,
                    -1
                );
        }
    }

    public static String repeatString(
        Type firstInputType,
        Type secondInputType
    ) {
        // expects parameters already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();
        Label returnLabel = new Label();
        Label invalidParamLabel = new Label();

        String sam = "";

        // prepare params, String always on top
        if (firstInputType == Type.STRING) {
            sam += "SWAP\n";
        }

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -1\n"; // free second param, only first param remain with new value
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 1: loop counter
        sam += "PUSHIMM 0\n"; // local 2: increment address
        sam += "PUSHIMM 0\n"; // local 3: return address

        // validate param, if n < 0 -> return
        sam += "PUSHOFF -2\n";
        sam += "ISNEG\n";
        sam += "JUMPC " + invalidParamLabel.name + "\n";

        // allocate memory for new string -> Address
        sam += "PUSHOFF -1\n";
        sam += getStringLength();
        sam += "PUSHOFF -2\n";
        sam += "TIMES\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "MALLOC\n";
        sam += "STOREOFF 3\n";

        // return this address
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF 4\n";

        // loop...
        sam += startLoopLabel.name + ":\n";
        // check if done
        sam += "PUSHOFF 2\n";
        sam += "PUSHOFF -2\n";
        sam += "EQUAL\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // append str to memory
        sam += "PUSHIMM 0\n"; // will return next address
        sam += "PUSHOFF 3\n"; // param1: starting memory address
        sam += "PUSHOFF -1\n"; // param2: string
        sam += appendStringHeap();
        sam += "STOREOFF 3\n";

        // increase counter
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 2\n";

        // Continue loop
        sam += "JUMP " + startLoopLabel.name + "\n";

        // Stop loop
        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 4\n";
        sam += "STOREOFF -2\n";
        sam += "JUMP " + returnLabel.name + "\n";

        // Invalid param, return empty string
        sam += invalidParamLabel.name + ":\n";
        sam += "PUSHIMMSTR \"\"";
        sam += "STOREOFF -2\n";
        sam += "JUMP " + returnLabel.name + "\n";

        // Return func
        sam += returnLabel.name + ":\n";
        sam += "ADDSP -3\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }

    public static String getStringLength() {
        // expects parameters already on the stack
        Label startCountLabel = new Label();
        Label stopCountLabel = new Label();
        String sam = "";

        sam += "DUP\n";

        // START
        sam += startCountLabel.name + ":\n";
        sam += "DUP\n";
        sam += "PUSHIND\n";

        // check end of string
        sam += "ISNIL\n";
        sam += "JUMPC " + stopCountLabel.name + "\n";

        // increament count and continue loop
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "JUMP " + startCountLabel.name + "\n";

        // STOP
        sam += stopCountLabel.name + ":\n";
        sam += "SWAP\n";
        sam += "SUB\n";

        return sam;
    }

    public static String appendStringHeap() {
        // expects parameters already on the stack, String on top, Mempry address
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -2\n";
        sam += "JUMP " + exitFuncLabel.name + "\n";

        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHOFF -2\n";
        sam += "PUSHOFF -1\n";

        sam += startLoopLabel.name + ":\n";
        // put char in TOS
        // end loop if nil
        sam += "PUSHOFF 3\n";
        sam += "PUSHIND\n";
        sam += "ISNIL\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // Save to allocated memory
        sam += "PUSHOFF 2\n";
        sam += "PUSHOFF 3\n";
        sam += "PUSHIND\n";
        sam += "STOREIND\n";

        // increase address current string
        sam += "PUSHOFF 3\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 3\n";

        // increase final address string
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 2\n";

        sam += "JUMP " + startLoopLabel.name + "\n";

        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMMCH '\\0'" + "\n";
        sam += "STOREIND\n";
        sam += "PUSHOFF 2\n";
        sam += "STOREOFF -3\n";
        sam += "ADDSP -2\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }

    public static String concatString() {
        // expects parameters (2 strings) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -1\n"; // free second param, only first param remain with new value
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 2: increment address
        sam += "PUSHIMM 0\n"; // local 3: return address

        // allocate space for resulting string
        sam += "PUSHOFF -1\n";
        sam += getStringLength();
        sam += "PUSHOFF -2\n";
        sam += getStringLength();
        sam += "ADD\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "MALLOC\n";
        sam += "STOREOFF 2\n";

        // return this address
        sam += "PUSHOFF 2\n";
        sam += "STOREOFF 3\n";

        // append first string to memory
        sam += "PUSHIMM 0\n"; // will return next address
        sam += "PUSHOFF 2\n"; // param1: starting memory address
        sam += "PUSHOFF -2\n"; // param2: string
        sam += appendStringHeap();
        sam += "STOREOFF 2\n";

        // append second string to memory
        sam += "PUSHIMM 0\n";
        sam += "PUSHOFF 2\n";
        sam += "PUSHOFF -1\n";
        sam += appendStringHeap();
        sam += "STOREOFF 2\n";

        // store in the first string pos
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF -2\n";

        // clean local vars
        sam += "ADDSP -2\n";
        // return
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }

    public static String compareString(char op) throws CompilerException {
        if (getBinopType(op) != BinopType.COMPARISON) {
            throw new SyntaxErrorException(
                "compareString receive invalid operation: " + op,
                -1
            );
        }

        // expects parameters (2 strings) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "ADDSP -1\n"; // free second param, only first param remain with new value
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 2: counter
        sam += "PUSHIMM 0\n"; // local 3: result

        // loop...
        sam += startLoopLabel.name + ":\n";
        // reach end of string 1?
        sam += "PUSHOFF -2\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";
        sam += "ISNIL\n";

        // reach end of string 2?
        sam += "PUSHOFF -1\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";
        sam += "ISNIL\n";

        // reach end of both string, is equal
        sam += "AND\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // not end, comparing char by char
        // get char of string 1
        sam += "PUSHOFF -2\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";

        // get char of string 2
        sam += "PUSHOFF -1\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIND\n";

        // compare and store result
        sam += "CMP\n";
        sam += "STOREOFF 3\n";

        // check if done
        sam += "PUSHOFF 3\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // not done, continue to next char
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 2\n";
        sam += "JUMP " + startLoopLabel.name + "\n";

        // Stop loop
        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF -2\n";
        sam += "ADDSP -2\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        if (op == '<') {
            sam += "PUSHIMM 1\n";
        } else if (op == '>') {
            sam += "PUSHIMM -1\n";
        } else {
            sam += "PUSHIMM 0\n";
        }
        sam += "EQUAL\n";

        return sam;
    }

    public static String reverseString() {
        // expects parameter (1 string) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        String sam = "";

        // call method
        sam += "LINK\n";
        sam += "JSR " + enterFuncLabel.name + "\n";
        sam += "UNLINK\n";
        sam += "JUMP " + exitFuncLabel.name + "\n";

        // method definition
        sam += enterFuncLabel.name + ":\n";
        sam += "PUSHIMM 0\n"; // local 2: counter
        sam += "PUSHIMM 0\n"; // local 3: increment address
        sam += "PUSHIMM 0\n"; // local 4: result

        // get string length and store in local 2
        sam += "PUSHOFF -1\n";
        sam += getStringLength();
        sam += "STOREOFF 2\n";

        // allocate space for resulting string
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "MALLOC\n";
        sam += "STOREOFF 3\n";

        // return this address
        sam += "PUSHOFF 3\n";
        sam += "STOREOFF 4\n";

        // set EOS char first
        sam += "PUSHOFF 3\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIMMCH '\\0'" + "\n";
        sam += "STOREIND\n";

        // loop (backward)...
        sam += startLoopLabel.name + ":\n";

        // end loop if counter == 0
        sam += "PUSHOFF 2\n";
        sam += "ISNIL\n";
        sam += "JUMPC " + stopLoopLabel.name + "\n";

        // get current address
        sam += "PUSHOFF 3\n";

        // get current char
        sam += "PUSHOFF -1\n";
        sam += "PUSHOFF 2\n";
        sam += "ADD\n";
        sam += "PUSHIMM 1\n"; // subtract 1 because indexing
        sam += "SUB\n";
        sam += "PUSHIND\n";

        // store char in address
        sam += "STOREIND\n";

        // increment address
        sam += "PUSHOFF 3\n";
        sam += "PUSHIMM 1\n";
        sam += "ADD\n";
        sam += "STOREOFF 3\n";

        // decrement counter
        sam += "PUSHOFF 2\n";
        sam += "PUSHIMM 1\n";
        sam += "SUB\n";
        sam += "STOREOFF 2\n";

        // Continue loop
        sam += "JUMP " + startLoopLabel.name + "\n";

        // Stop loop
        sam += stopLoopLabel.name + ":\n";
        sam += "PUSHOFF 4\n";
        sam += "STOREOFF -1\n";
        sam += "ADDSP -3\n";
        sam += "RST\n";

        // Exit method
        sam += exitFuncLabel.name + ":\n";

        return sam;
    }
}
