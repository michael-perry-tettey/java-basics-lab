import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Checks how the program responds to the options it is started with. */
class LendingTrackerTest {

    @Test
    void withoutOptionsThereIsNoDueDatesView() {
        String printed = runAndCaptureOutput();

        assertTrue(printed.contains("end of day"), printed);
        assertFalse(printed.contains("Due dates"), printed);
    }

    @Test
    void dueDatesOptionAddsTheViewAfterTheReport() {
        String printed = runAndCaptureOutput("--due-dates", "2999-01-01");

        assertTrue(printed.indexOf("Due dates (as of 2999-01-01):") > printed.indexOf("end of day"), printed);
        assertTrue(printed.contains("(OVERDUE by "), printed);
    }

    @Test
    void anUnreadableDateIsReportedInsteadOfCrashing() {
        PrintStream realErr = System.err;
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            System.setErr(capture);
            runAndCaptureOutput("--due-dates", "tomorrow");
        } finally {
            System.setErr(realErr);
        }

        String message = errors.toString(StandardCharsets.UTF_8);
        assertTrue(message.contains("Cannot read the date \"tomorrow\""), message);
    }

    private static String runAndCaptureOutput(String... args) {
        PrintStream realOut = System.out;
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(printed, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            LendingTracker.main(args);
        } finally {
            System.setOut(realOut);
        }
        return printed.toString(StandardCharsets.UTF_8);
    }
}
