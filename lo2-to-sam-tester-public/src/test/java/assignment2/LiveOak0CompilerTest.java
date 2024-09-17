package assignment2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/*******************
 * Example test cases for main.LiveOak0Compiler, on LiveOak-0
 *
 *
 * *******************/
class LiveOak0CompilerTest {
    private static final String lo0ValidProgramDir = Path.of("src", "test", "resources", "LO-0", "ValidPrograms").toString();
    private static final String lo0InvalidProgramDir = Path.of("src", "test", "resources", "LO-0", "InvalidPrograms").toString();
    private static ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
        errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    }

    private static String getStdErr() {
        return errContent.toString().replaceAll("\r", "");
    }

    @Test
    @DisplayName("should just run")
    void testLO0_1() throws Throwable {
        String fileName = Path.of(lo0ValidProgramDir, "test_0.lo").toString();
        String program = LiveOak0Compiler.compiler(fileName);
    }
}
