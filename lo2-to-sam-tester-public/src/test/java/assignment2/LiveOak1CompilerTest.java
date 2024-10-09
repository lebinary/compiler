package assignment2;

import static org.junit.jupiter.api.Assertions.*;
import assignment2.errors.TypeErrorException;
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
 * Example test cases for main.LiveOak2Compiler, on LiveOak-1
 *
 *
 * *******************/
class LiveOak1CompilerTest {

    private static final String lo1ValidProgramDir = Path.of(
        "src",
        "test",
        "resources",
        "LO-0",
        "ValidPrograms"
    ).toString();
    private static final String lo1InvalidProgramDir = Path.of(
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
    void testL01_0() throws Throwable {
        String fileName = Path.of(lo1ValidProgramDir, "test_0.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0);

        assertEquals(20, stackValues.get(0).getValue());
    }

    @Test
    @DisplayName("Test 1")
    void testLO1_1() throws Throwable {
        String fileName = Path.of(lo1ValidProgramDir, "test_1.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);

        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0, 1);

        assertEquals(10, stackValues.get(0).getValue());
        assertEquals(0, stackValues.get(1).getValue());
    }

    @Test
    @DisplayName("Test 2")
    void testLO1_2() throws Throwable {
        String fileName = Path.of(lo1ValidProgramDir, "test_2.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);

        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0, 1);

        assertEquals(30, stackValues.get(0).getValue());
        assertEquals(20, stackValues.get(1).getValue());
    }

    @Test
    @DisplayName("Test 3")
    void testLO1_3() throws Throwable {
        String fileName = Path.of(lo1ValidProgramDir, "test_3.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);

        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0, 1, 2);

        assertEquals(90, stackValues.get(0).getValue());
        assertEquals(20, stackValues.get(1).getValue());
        assertEquals(15, stackValues.get(2).getValue());
    }

    @Test
    @DisplayName("Test 4")
    void testLO1_4() throws Throwable {
        String fileName = Path.of(lo1ValidProgramDir, "test_4.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);

        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0, 1, 2);

        assertEquals(90, stackValues.get(0).getValue());
        assertEquals(5, stackValues.get(1).getValue());
        assertEquals(15, stackValues.get(2).getValue());
    }

    @Test
    @DisplayName("Test 5")
    void testLO1_5() throws Throwable {
        String fileName = Path.of(lo1ValidProgramDir, "test_5.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);

        var samMachine = new SamTestRunner(program);
        var stackValues = samMachine.run(0, 1, 2);

        assertEquals(100, stackValues.get(0).getValue());
        assertEquals(10, stackValues.get(1).getValue());
        assertEquals(10, stackValues.get(2).getValue());
    }

    /** INVALID PROGRAMS
     **/
    @Test
    @DisplayName("Test_6")
    void testLO1_6() throws Throwable {
        String fileName = Path.of(lo1InvalidProgramDir, "test_6.lo").toString();
        assertThrows(
            TypeErrorException.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_6.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-0/InvalidPrograms/test_6.lo"
                )
        );
    }
}
