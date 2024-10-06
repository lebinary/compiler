package assignment2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*******************
 * Example test cases for main.LiveOak0Compiler, on LiveOak-0
 *
 *
 * *******************/
class LiveOak0CompilerTest {
    private static final String lo0ValidProgramDir = Path.of(
        "src",
        "test",
        "resources",
        "LO-0",
        "ValidPrograms"
    ).toString();
    private static final String lo0InvalidProgramDir = Path.of(
        "src",
        "test",
        "resources",
        "LO-0",
        "InvalidPrograms"
    ).toString();
    private static ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
        errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(
            new PrintStream(new FileOutputStream(FileDescriptor.out))
        );
    }

    private static String getStdErr() {
        return errContent.toString().replaceAll("\r", "");
    }

    /** VALID PROGRAMS
     **/
    @Test
    @DisplayName("Test_0")
    void testLO0_0() throws Throwable {
        String fileName = Path.of(lo0ValidProgramDir, "test_0.lo").toString();
        String program = LiveOak0Compiler.compiler(fileName);

        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0);

        assertEquals(20, stackValues.get(0).getValue());
    }

    @Test
    @DisplayName("Test 1")
    void testLO0_1() throws Throwable {
        String fileName = Path.of(lo0ValidProgramDir, "test_1.lo").toString();
        String program = LiveOak0Compiler.compiler(fileName);

        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0, 1);

        assertEquals(10, stackValues.get(0).getValue());
        assertEquals(0, stackValues.get(1).getValue());
    }

    @Test
    @DisplayName("Test 2")
    void testLO0_2() throws Throwable {
        String fileName = Path.of(lo0ValidProgramDir, "test_2.lo").toString();
        String program = LiveOak0Compiler.compiler(fileName);

        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0, 1);

        assertEquals(30, stackValues.get(0).getValue());
        assertEquals(20, stackValues.get(1).getValue());
    }

    @Test
    @DisplayName("Test 3")
    void testLO0_3() throws Throwable {
        String fileName = Path.of(lo0ValidProgramDir, "test_3.lo").toString();
        String program = LiveOak0Compiler.compiler(fileName);

        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0, 1, 2);

        assertEquals(90, stackValues.get(0).getValue());
        assertEquals(20, stackValues.get(1).getValue());
        assertEquals(15, stackValues.get(2).getValue());
    }

    @Test
    @DisplayName("Test 4")
    void testLO0_4() throws Throwable {
        String fileName = Path.of(lo0ValidProgramDir, "test_4.lo").toString();
        String program = LiveOak0Compiler.compiler(fileName);

        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0, 1, 2);

        assertEquals(90, stackValues.get(0).getValue());
        assertEquals(5, stackValues.get(1).getValue());
        assertEquals(15, stackValues.get(2).getValue());
    }

    /** INVALID PROGRAMS
     **/
    @Test
    @DisplayName("Test_5")
    void testLO0_5() throws Throwable {
        String fileName = Path.of(lo0InvalidProgramDir, "test_5.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak0Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_5.lo"
        );
        // assertTrue(
        //     getStdErr()
        //         .contains(
        //             "Failed to compile src/test/resources/LO-0/InvalidPrograms/test_5.lo"
        //         )
        // );
    }
}
