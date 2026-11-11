package report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import domain.AudioKit;
import domain.Book;
import domain.Library;
import domain.Member;
import domain.MemberKind;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReportPrinterTest {

    private static final LocalDate BORROW_DATE = LocalDate.of(2026, 1, 10);

    private Library library;
    private ByteArrayOutputStream printed;
    private ReportPrinter printer;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(BORROW_DATE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        library = new Library("Test library", fixedClock);
        printed = new ByteArrayOutputStream();
        printer = new ReportPrinter(new PrintStream(printed, true, StandardCharsets.UTF_8));
    }

    @Test
    void dueDatesViewListsTheEarliestDueFirstAndMarksOverdueLoans() {
        library.borrow(new Member("Ama Owusu", MemberKind.STUDENT), new Book("Things Fall Apart"));
        library.borrow(new Member("Kofi Mensah", MemberKind.STANDARD), new AudioKit("French Audio Course"));

        printer.printDueDates(library, LocalDate.of(2026, 1, 20));

        assertEquals(String.join(System.lineSeparator(),
                "",
                "Due dates (as of 2026-01-20):",
                "  Loan #2: \"French Audio Course\" -> Kofi Mensah, due 2026-01-17 (OVERDUE by 3 days)",
                "  Loan #1: \"Things Fall Apart\" -> Ama Owusu, due 2026-02-07",
                ""), printedText());
    }

    @Test
    void dueDatesViewSaysOneDayInTheSingular() {
        library.borrow(new Member("Kofi Mensah", MemberKind.STANDARD), new AudioKit("French Audio Course"));

        printer.printDueDates(library, LocalDate.of(2026, 1, 18));

        assertEquals(String.join(System.lineSeparator(),
                "",
                "Due dates (as of 2026-01-18):",
                "  Loan #1: \"French Audio Course\" -> Kofi Mensah, due 2026-01-17 (OVERDUE by 1 day)",
                ""), printedText());
    }

    @Test
    void dueDatesViewSaysWhenNothingIsOnLoan() {
        printer.printDueDates(library, BORROW_DATE);

        assertEquals(String.join(System.lineSeparator(),
                "",
                "Due dates (as of 2026-01-10):",
                "  No active loans",
                ""), printedText());
    }

    private String printedText() {
        return printed.toString(StandardCharsets.UTF_8);
    }
}
