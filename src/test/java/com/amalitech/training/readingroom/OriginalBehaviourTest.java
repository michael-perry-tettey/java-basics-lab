package com.amalitech.training.readingroom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Pins the program's console output to the baseline captured from the
 * original, unchanged starter code (docs/original-output.txt).
 *
 * While the code is being reorganised this test must keep passing without
 * the baseline file being edited. The baseline may only change in a commit
 * that deliberately changes behaviour, and that commit must say why.
 */
class OriginalBehaviourTest {

    private static final Path BASELINE = Path.of("docs", "original-output.txt");

    @Test
    void programOutputMatchesTheSavedBaseline() throws IOException {
        String expected = normaliseLineEndings(Files.readString(BASELINE, StandardCharsets.UTF_8));

        String actual = normaliseLineEndings(runProgramAndCaptureOutput());

        assertEquals(expected, actual);
    }

    private static String runProgramAndCaptureOutput() {
        PrintStream realOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            System.setOut(capture);
            LendingTracker.main(new String[0]);
        } finally {
            System.setOut(realOut);
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private static String normaliseLineEndings(String text) {
        return text.replace("\r\n", "\n");
    }
}
