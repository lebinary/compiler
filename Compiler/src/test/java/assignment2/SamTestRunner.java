package assignment2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.utexas.cs.sam.core.*;
import edu.utexas.cs.sam.ui.SamText;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for testing SaM assembly code in Junit tests.
 *
 * Usage example for Junit5:
 * <pre>{@code
 *     @Test
 *     @DisplayName("should obey short circuit evaluation rules")
 *     void testShortCircuitEvaluation() throws IOException, SystemException, AssemblerException {
 *         String fileName = Path.of(exampleDir, "short-circuit.lo").toString();
 *         SamTokenizer tokenizer = new SamTokenizer(fileName);
 *         Parser parser = new Parser(tokenizer);
 *         String program = parser.parse();
 *
 *         main.SamTestRunner str = new main.SamTestRunner(program);
 *         Memory.Data returnValue = str.run();
 *         assertEquals(
 *                 0,
 *                 returnValue.getValue()
 *         );
 *     }
 * }</pre>
 *
 * @author Tyler Collins &lt;tjscollins@gmail.com&gt;
 */
public class SamTestRunner {

    private final String programCode;
    private final Processor cpu;
    private final Memory mem;
    private final SamText txt;

    /**
     * Instantiates a new SaM test runner and sets up the virtual machine to run
     * the given assembly program.
     *
     * @param assembly SaM assembly code that you wish to run
     */
    public SamTestRunner(String assembly) {
        txt = new SamText();
        Sys sys = new Sys();
        cpu = sys.cpu();
        mem = sys.mem();
        cpu.init();
        mem.init();
        sys.setVideo(txt);
        programCode = assembly;
    }

    /**
     * Checks that the exit status of the supplied program equals the
     * expected value.  Meant to be used in JUnit tests.
     *
     * @param program       the SaM assembly code
     * @param expectedValue the expected exit status of the program
     * @throws IOException
     * @throws AssemblerException
     * @throws SystemException
     */
    public static void checkReturnValue(String program, int expectedValue)
        throws Throwable {
        var samMachine = new SamTestRunner(program);
        var returnValue = executeProgramWithTimeout(samMachine);

        assertEquals(expectedValue, returnValue.getValue());
    }

    /**
     * Checks the value of the string returned by main if main
     * returns a string value.  Useful for debugging string
     * operations.  Meant to be used in JUnit tests.
     *
     * @param program       the SaM assembly program
     * @param expectedValue the expected string value
     * @throws IOException
     * @throws AssemblerException
     * @throws SystemException
     */
    public static void checkReturnedStringValue(
        String program,
        String expectedValue
    ) throws Throwable {
        SamTestRunner str = new SamTestRunner(program);
        var returnValue = executeProgramWithTimeout(str);
        var heapValue = str.getHeapContents(
            returnValue.getValue(),
            expectedValue.length()
        );
        assertEquals(expectedValue, heapValueToString(heapValue));
    }

    private static Memory.Data executeProgramWithTimeout(SamTestRunner str)
        throws Throwable {
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Memory.Data> processResult = executor.submit(
            (Callable<Memory.Data>) str::run
        );
        Memory.Data returnValue = null;
        try {
            returnValue = processResult.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
        return returnValue;
    }

    /**
     * Runs the SaM assembly program that was passed to the constructor.
     *
     * @return Memory data from stack address 0 when the SaM assembly program exits.
     * @throws IOException        java.io.IOException
     * @throws AssemblerException Thrown if supplied assembly program is invalid
     * @throws SystemException    Thrown if supplied assembly program has a runtime error
     *                            (e.g. divide by zero)
     */
    public Memory.Data run()
        throws IOException, AssemblerException, SystemException {
        Program prg = SamAssembler.assemble(new StringReader(programCode));
        cpu.load(prg);
        cpu.run();
        return mem.getMem(0);
    }

    /**
     * Get contents of the SaM heap at the given memory address when the program terminated.
     *
     * @param addr Memory address on the heap you wish to access. Can be obtained from
     *             the program's return value if that address was the value at stack
     *             address 0 on program exit.
     * @param size The size of the object you wish to fetch from the heap.  You'll have
     *             to just know what you're looking for and supply the size of that.
     * @return A linked list of Memory.Data values from the heap.  You'll have to convert
     *         this into something useful if you want to compare against expected values.
     *         For example
     *
     * <pre>{@code
     *         var heapValue = str.getHeapContents(returnValue.getValue(), 10);
     *         String heapContents = heapValue.stream()
     *                                    .map(Memory.Data::getValue)
     *                                    .map((v) -> String.valueOf((char)(int)v))
     *                                    .collect(Collectors.joining());
     * }</pre>
     *
     *         will convert the contents of the heap to a regular Java string.
     */
    public List<Memory.Data> getHeapContents(int addr, int size) {
        return mem.getAllocation(new HeapAllocator.Allocation(addr, size));
    }

    /**
     * Converts a value on the SaM heap to a Java string.
     *
     * @param heapValue the heap value
     * @return the string
     */
    public static String heapValueToString(List<Memory.Data> heapValue) {
        return heapValue
            .stream()
            .map(Memory.Data::getValue)
            .map(v -> String.valueOf((char) (int) v))
            .collect(Collectors.joining());
    }

    /**
     * Gets final register value after program execution.  This is useful
     * for testing SP and FBR values at the end of program fragments.
     *
     * @param register The desired CPU register
     * @return the register value
     */
    public int getRegisterValue(Registers register) {
        return cpu.getRegisters()[register.ordinal()];
    }

    /**
     * Runs the SaM assembly program that was passed to the constructor.  Returns the
     * contents of multiple stack addresses.  This is primarily useful for testing program
     * fragments
     *
     * @param stackAddrs List of stack addresses to retrieve
     * @return An array of Memory.Data objects containing the contents of the specified
     *          SaM stack addresses.
     * @throws IOException
     * @throws AssemblerException
     * @throws SystemException
     */
    public List<Memory.Data> run(Integer... stackAddrs)
        throws IOException, AssemblerException, SystemException {
        Program prg = SamAssembler.assemble(new StringReader(programCode));
        cpu.load(prg);
        cpu.run();
        return Stream.of(stackAddrs)
            .map(v -> {
                try {
                    return mem.getMem(v);
                } catch (SystemException e) {
                    throw new RuntimeException(e.getMessage());
                }
            })
            .collect(Collectors.toList());
    }

    public enum Registers {
        PC,
        SP,
        FBR,
    }
}
