package assignment2;

import assignment2.errors.CompilerException;
import assignment2.errors.SyntaxErrorException;

public class StringHelper extends Helper {

    /*** Codegen related to string
     ***/
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

        StringBuilder sam = new StringBuilder();

        // prepare params, String always on top
        if (firstInputType == Type.STRING) {
            sam.append("SWAP\n");
        }

        // call method
        sam
            .append("LINK\n")
            .append("JSR ")
            .append(enterFuncLabel.name)
            .append("\n")
            .append("UNLINK\n")
            .append("ADDSP -1\n") // free second param, only first param remain with new value
            .append("JUMP ")
            .append(exitFuncLabel.name)
            .append("\n");

        // method definition
        sam
            .append(enterFuncLabel.name)
            .append(":\n")
            .append("PUSHIMM 0\n") // local 1: loop counter
            .append("PUSHIMM 0\n") // local 2: increment address
            .append("PUSHIMM 0\n"); // local 3: return address

        // validate param, if n < 0 -> return
        sam
            .append("PUSHOFF -2\n")
            .append("ISNEG\n")
            .append("JUMPC ")
            .append(invalidParamLabel.name)
            .append("\n");

        // allocate memory for new string -> Address
        sam
            .append("PUSHOFF -1\n")
            .append(getStringLength())
            .append("PUSHOFF -2\n")
            .append("TIMES\n")
            .append("PUSHIMM 1\n")
            .append("ADD\n")
            .append("MALLOC\n")
            .append("STOREOFF 3\n");

        // return this address
        sam.append("PUSHOFF 3\n").append("STOREOFF 4\n");

        // loop...
        sam
            .append(startLoopLabel.name)
            .append(":\n")
            // check if done
            .append("PUSHOFF 2\n")
            .append("PUSHOFF -2\n")
            .append("EQUAL\n")
            .append("JUMPC ")
            .append(stopLoopLabel.name)
            .append("\n");

        // append str to memory
        sam
            .append("PUSHIMM 0\n") // will return next address
            .append("PUSHOFF 3\n") // param1: starting memory address
            .append("PUSHOFF -1\n") // param2: string
            .append(appendStringHeap())
            .append("STOREOFF 3\n");

        // increase counter
        sam
            .append("PUSHOFF 2\n")
            .append("PUSHIMM 1\n")
            .append("ADD\n")
            .append("STOREOFF 2\n");

        // Continue loop
        sam.append("JUMP ").append(startLoopLabel.name).append("\n");

        // Stop loop
        sam
            .append(stopLoopLabel.name)
            .append(":\n")
            .append("PUSHOFF 4\n")
            .append("STOREOFF -2\n")
            .append("JUMP ")
            .append(returnLabel.name)
            .append("\n");

        // Invalid param, return empty string
        sam
            .append(invalidParamLabel.name)
            .append(":\n")
            .append("PUSHIMMSTR \"\"")
            .append("STOREOFF -2\n")
            .append("JUMP ")
            .append(returnLabel.name)
            .append("\n");

        // Return func
        sam
            .append(returnLabel.name)
            .append(":\n")
            .append("ADDSP -3\n")
            .append("RST\n");

        // Exit method
        sam.append(exitFuncLabel.name).append(":\n");

        return sam.toString();
    }

    public static String getStringLength() {
        // expects parameters already on the stack
        Label startCountLabel = new Label();
        Label stopCountLabel = new Label();
        StringBuilder sam = new StringBuilder();

        sam.append("DUP\n");

        // START
        sam
            .append(startCountLabel.name)
            .append(":\n")
            .append("DUP\n")
            .append("PUSHIND\n");

        // check end of string
        sam
            .append("ISNIL\n")
            .append("JUMPC ")
            .append(stopCountLabel.name)
            .append("\n");

        // increament count and continue loop
        sam
            .append("PUSHIMM 1\n")
            .append("ADD\n")
            .append("JUMP ")
            .append(startCountLabel.name)
            .append("\n");

        // STOP
        sam
            .append(stopCountLabel.name)
            .append(":\n")
            .append("SWAP\n")
            .append("SUB\n");

        return sam.toString();
    }

    public static String appendStringHeap() {
        // expects parameters already on the stack, String on top, Memory address
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        StringBuilder sam = new StringBuilder();

        // call method
        sam
            .append("LINK\n")
            .append("JSR ")
            .append(enterFuncLabel.name)
            .append("\n")
            .append("UNLINK\n")
            .append("ADDSP -2\n")
            .append("JUMP ")
            .append(exitFuncLabel.name)
            .append("\n");

        sam
            .append(enterFuncLabel.name)
            .append(":\n")
            .append("PUSHOFF -2\n")
            .append("PUSHOFF -1\n");

        sam
            .append(startLoopLabel.name)
            .append(":\n")
            // put char in TOS
            // end loop if nil
            .append("PUSHOFF 3\n")
            .append("PUSHIND\n")
            .append("ISNIL\n")
            .append("JUMPC ")
            .append(stopLoopLabel.name)
            .append("\n");

        // Save to allocated memory
        sam
            .append("PUSHOFF 2\n")
            .append("PUSHOFF 3\n")
            .append("PUSHIND\n")
            .append("STOREIND\n");

        // increase address current string
        sam
            .append("PUSHOFF 3\n")
            .append("PUSHIMM 1\n")
            .append("ADD\n")
            .append("STOREOFF 3\n");

        // increase final address string
        sam
            .append("PUSHOFF 2\n")
            .append("PUSHIMM 1\n")
            .append("ADD\n")
            .append("STOREOFF 2\n");

        sam.append("JUMP ").append(startLoopLabel.name).append("\n");

        sam
            .append(stopLoopLabel.name)
            .append(":\n")
            .append("PUSHOFF 2\n")
            .append("PUSHIMMCH '\\0'")
            .append("\n")
            .append("STOREIND\n")
            .append("PUSHOFF 2\n")
            .append("STOREOFF -3\n")
            .append("ADDSP -2\n")
            .append("RST\n");

        // Exit method
        sam.append(exitFuncLabel.name).append(":\n");

        return sam.toString();
    }

    public static String concatString() {
        // expects parameters (2 strings) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();

        StringBuilder sam = new StringBuilder();

        // call method
        sam
            .append("LINK\n")
            .append("JSR ")
            .append(enterFuncLabel.name)
            .append("\n")
            .append("UNLINK\n")
            .append("ADDSP -1\n") // free second param, only first param remain with new value
            .append("JUMP ")
            .append(exitFuncLabel.name)
            .append("\n");

        // method definition
        sam
            .append(enterFuncLabel.name)
            .append(":\n")
            .append("PUSHIMM 0\n") // local 2: increment address
            .append("PUSHIMM 0\n"); // local 3: return address

        // allocate space for resulting string
        sam
            .append("PUSHOFF -1\n")
            .append(getStringLength())
            .append("PUSHOFF -2\n")
            .append(getStringLength())
            .append("ADD\n")
            .append("PUSHIMM 1\n")
            .append("ADD\n")
            .append("MALLOC\n")
            .append("STOREOFF 2\n");

        // return this address
        sam.append("PUSHOFF 2\n").append("STOREOFF 3\n");

        // append first string to memory
        sam
            .append("PUSHIMM 0\n") // will return next address
            .append("PUSHOFF 2\n") // param1: starting memory address
            .append("PUSHOFF -2\n") // param2: string
            .append(appendStringHeap())
            .append("STOREOFF 2\n");

        // append second string to memory
        sam
            .append("PUSHIMM 0\n")
            .append("PUSHOFF 2\n")
            .append("PUSHOFF -1\n")
            .append(appendStringHeap())
            .append("STOREOFF 2\n");

        // store in the first string pos
        sam.append("PUSHOFF 3\n").append("STOREOFF -2\n");

        // clean local vars
        sam
            .append("ADDSP -2\n")
            // return
            .append("RST\n");

        // Exit method
        sam.append(exitFuncLabel.name).append(":\n");

        return sam.toString();
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

        StringBuilder sam = new StringBuilder();

        // call method
        sam
            .append("LINK\n")
            .append("JSR ")
            .append(enterFuncLabel.name)
            .append("\n")
            .append("UNLINK\n")
            .append("ADDSP -1\n") // free second param, only first param remain with new value
            .append("JUMP ")
            .append(exitFuncLabel.name)
            .append("\n");

        // method definition
        sam
            .append(enterFuncLabel.name)
            .append(":\n")
            .append("PUSHIMM 0\n") // local 2: counter
            .append("PUSHIMM 0\n"); // local 3: result

        // loop...
        sam
            .append(startLoopLabel.name)
            .append(":\n")
            // reach end of string 1?
            .append("PUSHOFF -2\n")
            .append("PUSHOFF 2\n")
            .append("ADD\n")
            .append("PUSHIND\n")
            .append("ISNIL\n");

        // reach end of string 2?
        sam
            .append("PUSHOFF -1\n")
            .append("PUSHOFF 2\n")
            .append("ADD\n")
            .append("PUSHIND\n")
            .append("ISNIL\n");

        // reach end of both string, is equal
        sam
            .append("AND\n")
            .append("JUMPC ")
            .append(stopLoopLabel.name)
            .append("\n");

        // not end, comparing char by char
        // get char of string 1
        sam
            .append("PUSHOFF -2\n")
            .append("PUSHOFF 2\n")
            .append("ADD\n")
            .append("PUSHIND\n");

        // get char of string 2
        sam
            .append("PUSHOFF -1\n")
            .append("PUSHOFF 2\n")
            .append("ADD\n")
            .append("PUSHIND\n");

        // compare and store result
        sam.append("CMP\n").append("STOREOFF 3\n");

        // check if done
        sam
            .append("PUSHOFF 3\n")
            .append("JUMPC ")
            .append(stopLoopLabel.name)
            .append("\n");

        // not done, continue to next char
        sam
            .append("PUSHOFF 2\n")
            .append("PUSHIMM 1\n")
            .append("ADD\n")
            .append("STOREOFF 2\n")
            .append("JUMP ")
            .append(startLoopLabel.name)
            .append("\n");

        // Stop loop
        sam
            .append(stopLoopLabel.name)
            .append(":\n")
            .append("PUSHOFF 3\n")
            .append("STOREOFF -2\n")
            .append("ADDSP -2\n")
            .append("RST\n");

        // Exit method
        sam.append(exitFuncLabel.name).append(":\n");

        if (op == '<') {
            sam.append("PUSHIMM 1\n");
        } else if (op == '>') {
            sam.append("PUSHIMM -1\n");
        } else {
            sam.append("PUSHIMM 0\n");
        }
        sam.append("EQUAL\n");

        return sam.toString();
    }

    public static String reverseString() {
        // expects parameter (1 string) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        Label startLoopLabel = new Label();
        Label stopLoopLabel = new Label();

        StringBuilder sam = new StringBuilder();

        // call method
        sam
            .append("LINK\n")
            .append("JSR ")
            .append(enterFuncLabel.name)
            .append("\n")
            .append("UNLINK\n")
            .append("JUMP ")
            .append(exitFuncLabel.name)
            .append("\n");

        // method definition
        sam
            .append(enterFuncLabel.name)
            .append(":\n")
            .append("PUSHIMM 0\n") // local 2: counter
            .append("PUSHIMM 0\n") // local 3: increment address
            .append("PUSHIMM 0\n"); // local 4: result

        // get string length and store in local 2
        sam
            .append("PUSHOFF -1\n")
            .append(getStringLength())
            .append("STOREOFF 2\n");

        // allocate space for resulting string
        sam
            .append("PUSHOFF 2\n")
            .append("PUSHIMM 1\n")
            .append("ADD\n")
            .append("MALLOC\n")
            .append("STOREOFF 3\n");

        // return this address
        sam.append("PUSHOFF 3\n").append("STOREOFF 4\n");

        // set EOS char first
        sam
            .append("PUSHOFF 3\n")
            .append("PUSHOFF 2\n")
            .append("ADD\n")
            .append("PUSHIMMCH '\\0'")
            .append("\n")
            .append("STOREIND\n");

        // loop (backward)...
        sam.append(startLoopLabel.name).append(":\n");

        // end loop if counter == 0
        sam
            .append("PUSHOFF 2\n")
            .append("ISNIL\n")
            .append("JUMPC ")
            .append(stopLoopLabel.name)
            .append("\n");

        // get current address
        sam.append("PUSHOFF 3\n");

        // get current char
        sam
            .append("PUSHOFF -1\n")
            .append("PUSHOFF 2\n")
            .append("ADD\n")
            .append("PUSHIMM 1\n") // subtract 1 because indexing
            .append("SUB\n")
            .append("PUSHIND\n");

        // store char in address
        sam.append("STOREIND\n");

        // increment address
        sam
            .append("PUSHOFF 3\n")
            .append("PUSHIMM 1\n")
            .append("ADD\n")
            .append("STOREOFF 3\n");

        // decrement counter
        sam
            .append("PUSHOFF 2\n")
            .append("PUSHIMM 1\n")
            .append("SUB\n")
            .append("STOREOFF 2\n");

        // Continue loop
        sam.append("JUMP ").append(startLoopLabel.name).append("\n");

        // Stop loop
        sam
            .append(stopLoopLabel.name)
            .append(":\n")
            .append("PUSHOFF 4\n")
            .append("STOREOFF -1\n")
            .append("ADDSP -3\n")
            .append("RST\n");

        // Exit method
        sam.append(exitFuncLabel.name).append(":\n");

        return sam.toString();
    }
}
