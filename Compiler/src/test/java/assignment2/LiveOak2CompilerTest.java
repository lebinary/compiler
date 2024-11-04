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
 * Example test cases for main.LiveOak2Compiler
 *
 * You will want to add more test cases
 * *******************/
class LiveOak2CompilerTest {

    private static final String lo2ValidProgramDir = Path.of(
        "src",
        "test",
        "resources",
        "LO-2",
        "ValidPrograms"
    ).toString();
    private static final String lo2InvalidProgramDir = Path.of(
        "src",
        "test",
        "resources",
        "LO-2",
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

    @Test
    @DisplayName("should fail to compile test_0.lo")
    void testLO2_0() {
        String fileName = Path.of(lo2InvalidProgramDir, "test_0.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_0.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_0.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_1.lo")
    void testLO2_1() {
        String fileName = Path.of(lo2InvalidProgramDir, "test_1.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_1.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_1.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_10.lo")
    void testLO2_2() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_10.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_10.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_10.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_11.lo")
    void testLO2_3() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_11.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_11.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_11.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_12.lo")
    void testLO2_4() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_12.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_12.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_12.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_13.lo")
    void testLO2_5() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_13.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_13.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_13.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_14.lo")
    void testLO2_6() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_14.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_14.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_14.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_15.lo")
    void testLO2_7() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_15.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_15.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_15.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_16.lo")
    void testLO2_8() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_16.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_16.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_16.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_17.lo")
    void testLO2_9() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_17.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_17.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_17.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_18.lo")
    void testLO2_10() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_18.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_18.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_18.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_19.lo")
    void testLO2_11() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_19.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_19.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_19.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_2.lo")
    void testLO2_12() {
        String fileName = Path.of(lo2InvalidProgramDir, "test_2.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_2.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_2.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_20.lo")
    void testLO2_13() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_20.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_20.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_20.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_21.lo")
    void testLO2_14() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_21.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_21.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_21.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_22.lo")
    void testLO2_15() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_22.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_22.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_22.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_23.lo")
    void testLO2_16() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_23.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_23.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_23.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_24.lo")
    void testLO2_17() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_24.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_24.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_24.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_25.lo")
    void testLO2_18() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_25.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_25.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_25.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_26.lo")
    void testLO2_19() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_26.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_26.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_26.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_27.lo")
    void testLO2_20() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_27.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_27.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_27.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_28.lo")
    void testLO2_21() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_28.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_28.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_28.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_29.lo")
    void testLO2_22() {
        String fileName = Path.of(
            lo2InvalidProgramDir,
            "test_29.lo"
        ).toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_29.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_29.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_3.lo")
    void testLO2_23() {
        String fileName = Path.of(lo2InvalidProgramDir, "test_3.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_3.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_3.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_4.lo")
    void testLO2_24() {
        String fileName = Path.of(lo2InvalidProgramDir, "test_4.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_4.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_4.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_5.lo")
    void testLO2_25() {
        String fileName = Path.of(lo2InvalidProgramDir, "test_5.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_5.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_5.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_6.lo")
    void testLO2_26() {
        String fileName = Path.of(lo2InvalidProgramDir, "test_6.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_6.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_6.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_7.lo")
    void testLO2_27() {
        String fileName = Path.of(lo2InvalidProgramDir, "test_7.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_7.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_7.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_8.lo")
    void testLO2_28() {
        String fileName = Path.of(lo2InvalidProgramDir, "test_8.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_8.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_8.lo"
                )
        );
    }

    @Test
    @DisplayName("should fail to compile test_9.lo")
    void testLO2_29() {
        String fileName = Path.of(lo2InvalidProgramDir, "test_9.lo").toString();
        assertThrows(
            Error.class,
            () -> LiveOak2Compiler.compiler(fileName),
            "Expected parse error to be thrown for file test_9.lo"
        );
        assertTrue(
            getStdErr()
                .contains(
                    "Failed to compile src/test/resources/LO-2/InvalidPrograms/test_9.lo"
                )
        );
    }

    @Test
    @DisplayName("should return 120")
    void testLO2_30() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_30.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 120);
    }

    @Test
    @DisplayName("should return 'The rain in Spain falls'")
    void testLO2_31() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_31.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(
            program,
            "The rain in Spain falls"
        );
    }

    @Test
    @DisplayName("should return 1100")
    void testLO2_32() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_32.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1100);
    }

    @Test
    @DisplayName("should return 'false'")
    void testLO2_33() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_33.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(program, "false");
    }

    @Test
    @DisplayName("should return 'abcabcabcabcabcabcabcabcabc'")
    void testLO2_34() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_34.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(
            program,
            "abcabcabcabcabcabcabcabcabc"
        );
    }

    @Test
    @DisplayName("should return 30")
    void testLO2_35() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_35.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 30);
    }

    @Test
    @DisplayName("should return 266")
    void testLO2_36() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_36.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 266);
    }

    @Test
    @DisplayName("should return 4")
    void testLO2_37() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_37.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 4);
    }

    @Test
    @DisplayName("should return 'tset a tsuj si sihTtset a tsuj si sihTright?'")
    void testLO2_38() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_38.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(
            program,
            "tset a tsuj si sihTtset a tsuj si sihTright?"
        );
    }

    @Test
    @DisplayName("should return 238")
    void testLO2_39() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_39.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 238);
    }

    @Test
    @DisplayName("should return 1")
    void testLO2_40() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_40.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1);
    }

    @Test
    @DisplayName("should return 1")
    void testLO2_41() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_41.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1);
    }

    @Test
    @DisplayName("should return 26141")
    void testLO2_42() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_42.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 26141);
    }

    @Test
    @DisplayName(
        "should return 'The rain in Spain fallsThe rain in Spain falls'"
    )
    void testLO2_43() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_43.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(
            program,
            "The rain in Spain fallsThe rain in Spain falls"
        );
    }

    @Test
    @DisplayName("should return 14")
    void testLO2_44() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_44.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 14);
    }

    @Test
    @DisplayName("should return 199704")
    void testLO2_45() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_45.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 199704);
    }

    @Test
    @DisplayName("should return 40")
    void testLO2_46() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_46.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 40);
    }

    @Test
    @DisplayName("should return 15")
    void testLO2_47() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_47.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 15);
    }

    @Test
    @DisplayName("should return 1")
    void testLO2_48() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_48.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1);
    }

    @Test
    @DisplayName("should return 475")
    void testLO2_49() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_49.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 475);
    }

    @Test
    @DisplayName("should return 'do Do Do do Do'")
    void testLO2_50() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_50.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(program, "do Do Do do Do");
    }

    @Test
    @DisplayName("should return 20")
    void testLO2_51() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_51.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 20);
    }

    @Test
    @DisplayName("should return 25")
    void testLO2_52() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_52.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 25);
    }

    @Test
    @DisplayName("should return 1")
    void testLO2_53() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_53.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1);
    }

    @Test
    @DisplayName("should return 11110")
    void testLO2_54() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_54.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 11110);
    }

    @Test
    @DisplayName("should return 'eip sa ysae'")
    void testLO2_55() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_55.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(program, "eip sa ysae");
    }

    @Test
    @DisplayName("should return 'HelloHello'")
    void testLO2_56() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_56.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(program, "HelloHello");
    }

    @Test
    @DisplayName("should return 'halb!emosewa era sgnirts'")
    void testLO2_57() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_57.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(
            program,
            "halb!emosewa era sgnirts"
        );
    }

    @Test
    @DisplayName("should return 17")
    void testLO2_58() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_58.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 17);
    }

    @Test
    @DisplayName("should return 'zZZZsllaf niapS ni niar ehTZZZz'")
    void testLO2_59() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_59.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(
            program,
            "zZZZsllaf niapS ni niar ehTZZZz"
        );
    }

    @Test
    @DisplayName("should return 345")
    void testLO2_60() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_60.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 345);
    }

    @Test
    @DisplayName("should return 5493")
    void testLO2_61() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_61.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 5493);
    }

    @Test
    @DisplayName("should return 30")
    void testLO2_62() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_62.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 30);
    }

    @Test
    @DisplayName("should return 34")
    void testLO2_63() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_63.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 34);
    }

    @Test
    @DisplayName("should return 'alternate result'")
    void testLO2_64() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_64.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(program, "alternate result");
    }

    @Test
    @DisplayName("should return 123")
    void testLO2_65() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_65.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 123);
    }

    @Test
    @DisplayName("should return 1311")
    void testLO2_66() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_66.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1311);
    }

    @Test
    @DisplayName("should return 24")
    void testLO2_67() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_67.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 24);
    }

    @Test
    @DisplayName("should return 5")
    void testLO2_68() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_68.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 5);
    }

    @Test
    @DisplayName("should return 100098")
    void testLO2_69() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_69.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 100098);
    }

    @Test
    @DisplayName("should return 12069")
    void testLO2_70() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_70.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 12069);
    }

    @Test
    @DisplayName("should return 'azzzzzc'")
    void testLO2_71() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_71.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(program, "azzzzzc");
    }

    @Test
    @DisplayName("should return 32")
    void testLO2_72() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_72.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 32);
    }

    @Test
    @DisplayName("should return 20")
    void testLO2_73() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_73.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 20);
    }

    @Test
    @DisplayName("should return 147")
    void testLO2_74() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_74.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 147);
    }

    @Test
    @DisplayName("should return 431")
    void testLO2_75() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_75.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 431);
    }

    @Test
    @DisplayName("should return 3533")
    void testLO2_76() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_76.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 3533);
    }

    @Test
    @DisplayName("should return 0")
    void testLO2_77() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_77.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 0);
    }

    @Test
    @DisplayName("should return 1")
    void testLO2_78() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_78.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1);
    }

    @Test
    @DisplayName("should return 0")
    void testLO2_79() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_79.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 0);
    }

    @Test
    @DisplayName("should return 388")
    void testLO2_80() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_80.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 388);
    }

    @Test
    @DisplayName("should return 456")
    void testLO2_81() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_81.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 456);
    }

    @Test
    @DisplayName("should return 0")
    void testLO2_82() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_82.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 0);
    }

    @Test
    @DisplayName("should return -10")
    void testLO2_83() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_83.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, -10);
    }

    @Test
    @DisplayName("should return 'black cat'")
    void testLO2_84() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_84.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(program, "black cat");
    }

    @Test
    @DisplayName("should return 10111")
    void testLO2_85() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_85.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 10111);
    }

    @Test
    @DisplayName("should return 14")
    void testLO2_86() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_86.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 14);
    }

    @Test
    @DisplayName("should return 72")
    void testLO2_87() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_87.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 72);
    }

    @Test
    @DisplayName("should return 2")
    void testLO2_88() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_88.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 2);
    }

    @Test
    @DisplayName("should return 0")
    void testLO2_89() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_89.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 0);
    }

    @Test
    @DisplayName("should return 168")
    void testLO2_90() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_90.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 168);
    }

    @Test
    @DisplayName("should return 12")
    void testLO2_91() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_91.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 12);
    }

    @Test
    @DisplayName("should return 90")
    void testLO2_92() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_92.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 90);
    }

    @Test
    @DisplayName("should return 111")
    void testLO2_93() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_93.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 111);
    }

    @Test
    @DisplayName("should return 481")
    void testLO2_94() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_94.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 481);
    }

    @Test
    @DisplayName("should return 1597")
    void testLO2_95() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_95.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1597);
    }

    @Test
    @DisplayName("should return 163")
    void testLO2_96() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_96.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 163);
    }

    @Test
    @DisplayName("should return 'abcabcabcabcabcabcabcabcabcabcabc'")
    void testLO2_97() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_97.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(
            program,
            "abcabcabcabcabcabcabcabcabcabcabc"
        );
    }

    @Test
    @DisplayName("should return -1")
    void testLO2_98() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_98.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, -1);
    }

    @Test
    @DisplayName("should return 905")
    void testLO2_99() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_99.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 905);
    }

    @Test
    @DisplayName("should return \n    *\n   ***\n  *****\n *******\n********* ")
    void testLO2_100() throws Throwable {
        String fileName = Path.of(lo2ValidProgramDir, "test_100.lo").toString();
        String program = LiveOak2Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(
            program,
            "    *\n   ***\n  *****\n *******\n*********"
        );
    }
}
