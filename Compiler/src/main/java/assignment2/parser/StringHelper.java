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
            sam.append(Backend.swap());
        }

        // call method
        sam
            .append(Backend.link())
            .append(Backend.jumpSubroutine(enterFuncLabel.name))
            .append(Backend.unlink())
            .append(Backend.addStackPointer(-1)) // free second param, only first param remain with new value
            .append(Backend.jump(exitFuncLabel.name));

        // method definition
        sam
            .append(Backend.label(enterFuncLabel.name))
            .append(Backend.pushImmediate(0)) // local 1: loop counter
            .append(Backend.pushImmediate(0)) // local 2: increment address
            .append(Backend.pushImmediate(0)); // local 3: return address

        // validate param, if n < 0 -> return
        sam
            .append(Backend.pushOffset(-2))
            .append(Backend.isNegative())
            .append(Backend.jumpConditional(invalidParamLabel.name));

        // allocate memory for new string -> Address
        sam
            .append(Backend.pushOffset(-1))
            .append(getStringLength())
            .append(Backend.pushOffset(-2))
            .append(Backend.multiply())
            .append(Backend.pushImmediate(1))
            .append(Backend.add())
            .append(Backend.malloc())
            .append(Backend.storeOffset(3));

        // return this address
        sam.append(Backend.pushOffset(3)).append(Backend.storeOffset(4));

        // loop...
        sam
            .append(Backend.label(startLoopLabel.name))
            // check if done
            .append(Backend.pushOffset(2))
            .append(Backend.pushOffset(-2))
            .append(Backend.equal())
            .append(Backend.jumpConditional(stopLoopLabel.name));

        // append str to memory
        sam
            .append(Backend.pushImmediate(0)) // will return next address
            .append(Backend.pushOffset(3)) // param1: starting memory address
            .append(Backend.pushOffset(-1)) // param2: string
            .append(appendStringHeap())
            .append(Backend.storeOffset(3));

        // increase counter
        sam
            .append(Backend.pushOffset(2))
            .append(Backend.pushImmediate(1))
            .append(Backend.add())
            .append(Backend.storeOffset(2));

        // Continue loop
        sam.append(Backend.jump(startLoopLabel.name));

        // Stop loop
        sam
            .append(Backend.label(stopLoopLabel.name))
            .append(Backend.pushOffset(4))
            .append(Backend.storeOffset(-2))
            .append(Backend.jump(returnLabel.name));

        // Invalid param, return empty string
        sam
            .append(Backend.label(invalidParamLabel.name))
            .append(Backend.pushImmediateString(""))
            .append(Backend.storeOffset(-2))
            .append(Backend.jump(returnLabel.name));

        // Return func
        sam
            .append(Backend.label(returnLabel.name))
            .append(Backend.addStackPointer(-3))
            .append(Backend.returnSubroutine());

        // Exit method
        sam.append(Backend.label(exitFuncLabel.name));

        return sam.toString();
    }

    public static String getStringLength() {
        // expects parameters already on the stack
        Label startCountLabel = new Label();
        Label stopCountLabel = new Label();
        StringBuilder sam = new StringBuilder();

        sam
            .append(Backend.dup())
            // START
            .append(Backend.label(startCountLabel.name))
            .append(Backend.dup())
            .append(Backend.pushIndirect())
            // check end of string
            .append(Backend.isNil())
            .append(Backend.jumpConditional(stopCountLabel.name))
            // increment count and continue loop
            .append(Backend.pushImmediate(1))
            .append(Backend.add())
            .append(Backend.jump(startCountLabel.name))
            // STOP
            .append(Backend.label(stopCountLabel.name))
            .append(Backend.swap())
            .append(Backend.subtract());

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
            .append(Backend.link())
            .append(Backend.jumpSubroutine(enterFuncLabel.name))
            .append(Backend.unlink())
            .append(Backend.addStackPointer(-2))
            .append(Backend.jump(exitFuncLabel.name));

        // Function entry
        sam
            .append(Backend.label(enterFuncLabel.name))
            .append(Backend.pushOffset(-2))
            .append(Backend.pushOffset(-1));

        // Main loop
        sam
            .append(Backend.label(startLoopLabel.name))
            // put char in TOS and end loop if nil
            .append(Backend.pushOffset(3))
            .append(Backend.pushIndirect())
            .append(Backend.isNil())
            .append(Backend.jumpConditional(stopLoopLabel.name));

        // Save to allocated memory
        sam
            .append(Backend.pushOffset(2))
            .append(Backend.pushOffset(3))
            .append(Backend.pushIndirect())
            .append(Backend.storeIndirect());

        // increase address current string
        sam
            .append(Backend.pushOffset(3))
            .append(Backend.pushImmediate(1))
            .append(Backend.add())
            .append(Backend.storeOffset(3));

        // increase final address string
        sam
            .append(Backend.pushOffset(2))
            .append(Backend.pushImmediate(1))
            .append(Backend.add())
            .append(Backend.storeOffset(2));

        sam.append(Backend.jump(startLoopLabel.name));

        // End of loop
        sam
            .append(Backend.label(stopLoopLabel.name))
            .append(Backend.pushOffset(2))
            .append(Backend.pushImmediateChar('\0'))
            .append(Backend.storeIndirect())
            .append(Backend.pushOffset(2))
            .append(Backend.storeOffset(-3))
            .append(Backend.addStackPointer(-2))
            .append(Backend.returnSubroutine());

        // Exit method
        sam.append(Backend.label(exitFuncLabel.name));

        return sam.toString();
    }

    public static String concatString() {
        // expects parameters (2 strings) already on the stack
        Label enterFuncLabel = new Label();
        Label exitFuncLabel = new Label();
        StringBuilder sam = new StringBuilder();

        // call method
        sam
            .append(Backend.link())
            .append(Backend.jumpSubroutine(enterFuncLabel.name))
            .append(Backend.unlink())
            .append(Backend.addStackPointer(-1)) // free second param, only first param remain with new value
            .append(Backend.jump(exitFuncLabel.name));

        // method definition
        sam
            .append(Backend.label(enterFuncLabel.name))
            .append(Backend.pushImmediate(0)) // local 2: increment address
            .append(Backend.pushImmediate(0)); // local 3: return address

        // allocate space for resulting string
        sam
            .append(Backend.pushOffset(-1))
            .append(getStringLength())
            .append(Backend.pushOffset(-2))
            .append(getStringLength())
            .append(Backend.add())
            .append(Backend.pushImmediate(1))
            .append(Backend.add())
            .append(Backend.malloc())
            .append(Backend.storeOffset(2));

        // return this address
        sam.append(Backend.pushOffset(2)).append(Backend.storeOffset(3));

        // append first string to memory
        sam
            .append(Backend.pushImmediate(0)) // will return next address
            .append(Backend.pushOffset(2)) // param1: starting memory address
            .append(Backend.pushOffset(-2)) // param2: string
            .append(appendStringHeap())
            .append(Backend.storeOffset(2));

        // append second string to memory
        sam
            .append(Backend.pushImmediate(0))
            .append(Backend.pushOffset(2))
            .append(Backend.pushOffset(-1))
            .append(appendStringHeap())
            .append(Backend.storeOffset(2));

        // store in the first string pos
        sam.append(Backend.pushOffset(3)).append(Backend.storeOffset(-2));

        // clean local vars and return
        sam
            .append(Backend.addStackPointer(-2))
            .append(Backend.returnSubroutine());

        // Exit method
        sam.append(Backend.label(exitFuncLabel.name));

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
            .append(Backend.link())
            .append(Backend.jumpSubroutine(enterFuncLabel.name))
            .append(Backend.unlink())
            .append(Backend.addStackPointer(-1)) // free second param, only first param remain with new value
            .append(Backend.jump(exitFuncLabel.name));

        // method definition
        sam
            .append(Backend.label(enterFuncLabel.name))
            .append(Backend.pushImmediate(0)) // local 2: counter
            .append(Backend.pushImmediate(0)); // local 3: result

        // loop...
        sam
            .append(Backend.label(startLoopLabel.name))
            // reach end of string 1?
            .append(Backend.pushOffset(-2))
            .append(Backend.pushOffset(2))
            .append(Backend.add())
            .append(Backend.pushIndirect())
            .append(Backend.isNil());

        // reach end of string 2?
        sam
            .append(Backend.pushOffset(-1))
            .append(Backend.pushOffset(2))
            .append(Backend.add())
            .append(Backend.pushIndirect())
            .append(Backend.isNil());

        // reach end of both string, is equal
        sam
            .append(Backend.and())
            .append(Backend.jumpConditional(stopLoopLabel.name));

        // not end, comparing char by char
        // get char of string 1
        sam
            .append(Backend.pushOffset(-2))
            .append(Backend.pushOffset(2))
            .append(Backend.add())
            .append(Backend.pushIndirect());

        // get char of string 2
        sam
            .append(Backend.pushOffset(-1))
            .append(Backend.pushOffset(2))
            .append(Backend.add())
            .append(Backend.pushIndirect());

        // compare and store result
        sam.append(Backend.compare()).append(Backend.storeOffset(3));

        // check if done
        sam
            .append(Backend.pushOffset(3))
            .append(Backend.jumpConditional(stopLoopLabel.name));

        // not done, continue to next char
        sam
            .append(Backend.pushOffset(2))
            .append(Backend.pushImmediate(1))
            .append(Backend.add())
            .append(Backend.storeOffset(2))
            .append(Backend.jump(startLoopLabel.name));

        // Stop loop
        sam
            .append(Backend.label(stopLoopLabel.name))
            .append(Backend.pushOffset(3))
            .append(Backend.storeOffset(-2))
            .append(Backend.addStackPointer(-2))
            .append(Backend.returnSubroutine());

        // Exit method
        sam.append(Backend.label(exitFuncLabel.name));

        // Compare operation
        if (op == '<') {
            sam.append(Backend.pushImmediate(1));
        } else if (op == '>') {
            sam.append(Backend.pushImmediate(-1));
        } else {
            sam.append(Backend.pushImmediate(0));
        }
        sam.append(Backend.equal());

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
            .append(Backend.link())
            .append(Backend.jumpSubroutine(enterFuncLabel.name))
            .append(Backend.unlink())
            .append(Backend.jump(exitFuncLabel.name));

        // method definition
        sam
            .append(Backend.label(enterFuncLabel.name))
            .append(Backend.pushImmediate(0)) // local 2: counter
            .append(Backend.pushImmediate(0)) // local 3: increment address
            .append(Backend.pushImmediate(0)); // local 4: result

        // get string length and store in local 2
        sam
            .append(Backend.pushOffset(-1))
            .append(getStringLength())
            .append(Backend.storeOffset(2));

        // allocate space for resulting string
        sam
            .append(Backend.pushOffset(2))
            .append(Backend.pushImmediate(1))
            .append(Backend.add())
            .append(Backend.malloc())
            .append(Backend.storeOffset(3));

        // return this address
        sam.append(Backend.pushOffset(3)).append(Backend.storeOffset(4));

        // set EOS char first
        sam
            .append(Backend.pushOffset(3))
            .append(Backend.pushOffset(2))
            .append(Backend.add())
            .append(Backend.pushImmediateChar('\0'))
            .append(Backend.storeIndirect());

        // loop (backward)...
        sam.append(Backend.label(startLoopLabel.name));

        // end loop if counter == 0
        sam
            .append(Backend.pushOffset(2))
            .append(Backend.isNil())
            .append(Backend.jumpConditional(stopLoopLabel.name));

        // get current address
        sam.append(Backend.pushOffset(3));

        // get current char
        sam
            .append(Backend.pushOffset(-1))
            .append(Backend.pushOffset(2))
            .append(Backend.add())
            .append(Backend.pushImmediate(1)) // subtract 1 because indexing
            .append(Backend.subtract())
            .append(Backend.pushIndirect());

        // store char in address
        sam.append(Backend.storeIndirect());

        // increment address
        sam
            .append(Backend.pushOffset(3))
            .append(Backend.pushImmediate(1))
            .append(Backend.add())
            .append(Backend.storeOffset(3));

        // decrement counter
        sam
            .append(Backend.pushOffset(2))
            .append(Backend.pushImmediate(1))
            .append(Backend.subtract())
            .append(Backend.storeOffset(2));

        // Continue loop
        sam.append(Backend.jump(startLoopLabel.name));

        // Stop loop
        sam
            .append(Backend.label(stopLoopLabel.name))
            .append(Backend.pushOffset(4))
            .append(Backend.storeOffset(-1))
            .append(Backend.addStackPointer(-3))
            .append(Backend.returnSubroutine());

        // Exit method
        sam.append(Backend.label(exitFuncLabel.name));

        return sam.toString();
    }
}
