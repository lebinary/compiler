package assignment2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/*******************
 * Example test cases for main.LiveOak3Compiler
 *
 * You will want to add more test cases
 * *******************/
class LiveOak3CompilerTest {
    private static final String lo3GoodExampleDir = Path.of("src", "test", "resources", "LO-3", "ValidPrograms").toString();
    private static final String lo3BadExampleDir = Path.of("src", "test", "resources", "LO-3", "InvalidPrograms").toString();
    private static ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
        errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
    }

    void resetStdErr() {
        errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent));
    }

    private static String getStdErr() {
        return errContent.toString().replaceAll("\r", "");
    }

    @Test
    @DisplayName("should fail to compile test_0.lo")
    void testLO2_0() {
        String fileName = Path.of(lo3BadExampleDir, "test_0.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_0.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_1.lo")
    void testLO2_1() {
        String fileName = Path.of(lo3BadExampleDir, "test_1.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_1.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_2.lo")
    void testLO2_2() {
        String fileName = Path.of(lo3BadExampleDir, "test_2.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_2.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_3.lo")
    void testLO2_3() {
        String fileName = Path.of(lo3BadExampleDir, "test_3.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_3.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_4.lo")
    void testLO2_4() {
        String fileName = Path.of(lo3BadExampleDir, "test_4.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_4.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_5.lo")
    void testLO2_5() {
        String fileName = Path.of(lo3BadExampleDir, "test_5.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_5.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_6.lo")
    void testLO2_6() {
        String fileName = Path.of(lo3BadExampleDir, "test_6.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_6.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_7.lo")
    void testLO2_7() {
        String fileName = Path.of(lo3BadExampleDir, "test_7.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_7.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_8.lo")
    void testLO2_8() {
        String fileName = Path.of(lo3BadExampleDir, "test_8.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_8.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_9.lo")
    void testLO2_9() {
        String fileName = Path.of(lo3BadExampleDir, "test_9.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_9.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_10.lo")
    void testLO2_10() {
        String fileName = Path.of(lo3BadExampleDir, "test_10.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_10.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_11.lo")
    void testLO2_11() {
        String fileName = Path.of(lo3BadExampleDir, "test_11.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_11.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_12.lo")
    void testLO2_12() {
        String fileName = Path.of(lo3BadExampleDir, "test_12.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_12.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_13.lo")
    void testLO2_13() {
        String fileName = Path.of(lo3BadExampleDir, "test_13.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_13.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should fail to compile test_14.lo")
    void testLO2_14() {
        String fileName = Path.of(lo3BadExampleDir, "test_14.lo").toString();
        assertThrows(
                Error.class,
                () -> LiveOak3Compiler.compiler(fileName),
                "Expected parse error to be thrown for file test_14.lo"
        );
        assertEquals("Failed to compile " + fileName + "\n", getStdErr());
        resetStdErr();
    }

    @Test
    @DisplayName("should return 10")
    void testLO2_15() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_15.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 10);
    }

    @Test
    @DisplayName("should return 6")
    void testLO2_16() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_16.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 6);
    }

    @Test
    @DisplayName("should return 2")
    void testLO2_17() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_17.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 2);
    }

    @Test
    @DisplayName("should return 48")
    void testLO2_18() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_18.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 48);
    }

    @Test
    @DisplayName("should return 10")
    void testLO2_19() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_19.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 10);
    }

    @Test
    @DisplayName("should return 1")
    void testLO2_20() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_20.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1);
    }

    @Test
    @DisplayName("should return 1")
    void testLO2_21() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_21.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1);
    }

    @Test
    @DisplayName("should return 7")
    void testLO2_22() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_22.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 7);
    }

    @Test
    @DisplayName("should return 4")
    void testLO2_23() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_23.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 4);
    }

    @Test
    @DisplayName("should return 7")
    void testLO2_24() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_24.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 7);
    }

    @Test
    @DisplayName("should return 16")
    void testLO2_25() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_25.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 16);
    }

    @Test
    @DisplayName("should return 4")
    void testLO2_26() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_26.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 4);
    }

    @Test
    @DisplayName("should return 0")
    void testLO2_27() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_27.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 3);
    }

    @Test
    @DisplayName("should return 70")
    void testLO2_28() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_28.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 70);
    }

    @Test
    @DisplayName("should return 20")
    void testLO2_29() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_29.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 20);
    }

    @Test
    @DisplayName("should return 14")
    void testLO2_30() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_30.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 14);
    }

    @Test
    @DisplayName("should return 4")
    void testLO2_31() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_31.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 4);
    }

    @Test
    @DisplayName("should return 92")
    void testLO2_32() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_32.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 92);
    }

    @Test
    @DisplayName("should return 956")
    void testLO2_33() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_33.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 956);
    }

    @Test
    @DisplayName("should return 56")
    void testLO2_34() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_34.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 56);
    }

    @Test
    @DisplayName("should return 99")
    void testLO2_35() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_35.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 99);
    }

    @Test
    @DisplayName("should return 10")
    void testLO2_36() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_36.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 10);
    }

    @Test
    @DisplayName("should return 9")
    void testLO2_37() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_37.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 9);
    }

    @Test
    @DisplayName("should return 36")
    void testLO2_38() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_38.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 36);
    }

    @Test
    @DisplayName("should return 12")
    void testLO2_39() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_39.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 12);
    }

    @Test
    @DisplayName("should return 17")
    void testLO2_40() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_40.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 17);
    }

    @Test
    @DisplayName("should return 71")
    void testLO2_41() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_41.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 71);
    }

    @Test
    @DisplayName("should return 38")
    void testLO2_42() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_42.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 38);
    }

    @Test
    @DisplayName("should return 'zYxzYx'")
    void testLO2_43() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_43.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnedStringValue(program, "zYxzYx");
    }

    @Test
    @DisplayName("should return 53441")
    void testLO2_44() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_44.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 53441);
    }

    @Test
    @DisplayName("should return 296")
    void testLO2_45() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_45.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 296);
    }

    @Test
    @DisplayName("should return 55")
    void testLO2_46() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_46.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 55);
    }

    @Test
    @DisplayName("should return 1")
    void testLO2_47() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_47.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 1);
    }

    @Test
    @DisplayName("should return -1")
    void testLO2_48() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_48.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, -1);
    }

    @Test
    @DisplayName("should return 36")
    void testLO2_49() throws Throwable {
        String fileName = Path.of(lo3GoodExampleDir, "test_49.lo").toString();
        String program = LiveOak3Compiler.compiler(fileName);
        SamTestRunner.checkReturnValue(program, 36);
    }

}
