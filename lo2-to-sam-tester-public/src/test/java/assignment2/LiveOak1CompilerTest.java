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
        "LO-1",
        "ValidPrograms"
    ).toString();
    private static final String lo1InvalidProgramDir = Path.of(
        "src",
        "test",
        "resources",
        "LO-1",
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

        System.out.println(program);
        // SamTestRunner.checkReturnValue(program, 20);
    }
}
