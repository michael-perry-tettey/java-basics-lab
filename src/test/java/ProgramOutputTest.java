import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pins what the program prints.
 *
 * docs/original-output.txt is the output of the original starter code and
 * is never edited. docs/expected-output.txt is what the program prints now.
 * The two differ only where the one-loan-per-item rule is enforced, and the
 * second test checks exactly that.
 */
class ProgramOutputTest {

    private static final Path ORIGINAL = Path.of("docs", "original-output.txt");
    private static final Path EXPECTED = Path.of("docs", "expected-output.txt");

    @Test
    void programOutputMatchesTheExpectedOutput() throws IOException {
        String expected = normaliseLineEndings(Files.readString(EXPECTED, StandardCharsets.UTF_8));

        String actual = normaliseLineEndings(runProgramAndCaptureOutput());

        assertEquals(expected, actual);
    }

    @Test
    void expectedOutputDiffersFromTheOriginalOnlyWhereTheSecondLoanIsRefused() throws IOException {
        List<String> original = Files.readAllLines(ORIGINAL, StandardCharsets.UTF_8);
        List<String> expected = Files.readAllLines(EXPECTED, StandardCharsets.UTF_8);

        // Everything before the second attempt to borrow the novel is identical.
        assertEquals(original.subList(0, 3), expected.subList(0, 3));

        // The second loan was made in the original and is refused now.
        assertEquals("Lent \"Things Fall Apart\" to Ama Owusu for 28 days", original.get(3));
        assertEquals("Cannot lend \"Things Fall Apart\" to Ama Owusu: already on loan to Kofi Mensah",
                expected.get(3));

        // The report header is identical.
        assertEquals(original.subList(4, 7), expected.subList(4, 7));

        // The novel is shown with Kofi, who actually has it, instead of Ama.
        assertEquals("  \"Things Fall Apart\" (BOOK): ON_LOAN - with Ama Owusu", original.get(7));
        assertEquals("  \"Things Fall Apart\" (BOOK): ON_LOAN - with Kofi Mensah", expected.get(7));

        // The audio course line, the heading and Kofi's loan are identical.
        assertEquals(original.subList(8, 11), expected.subList(8, 11));

        // The loan that should never have existed is gone, and nothing else follows.
        assertEquals("  Loan #3: \"Things Fall Apart\" -> Ama Owusu", original.get(11));
        assertEquals(12, original.size());
        assertEquals(11, expected.size());
    }

    @Test
    void dueDatesOptionAddsAViewAfterTheUnchangedReport() throws IOException {
        String expected = normaliseLineEndings(Files.readString(EXPECTED, StandardCharsets.UTF_8));

        String actual = normaliseLineEndings(runProgramAndCaptureOutput("--due-dates", "2999-01-01"));

        assertTrue(actual.startsWith(expected), "the normal output must come first, unchanged");
        String addedView = actual.substring(expected.length());
        assertTrue(addedView.startsWith("\nDue dates (as of 2999-01-01):\n"), addedView);
        assertTrue(addedView.contains("Loan #1: \"Things Fall Apart\" -> Kofi Mensah, due "), addedView);
        assertTrue(addedView.contains("(OVERDUE by "), addedView);
    }

    private static String runProgramAndCaptureOutput(String... args) {
        PrintStream realOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            LendingTracker.main(args);
        } finally {
            System.setOut(realOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private static String normaliseLineEndings(String text) {
        return text.replace("\r\n", "\n");
    }
}
