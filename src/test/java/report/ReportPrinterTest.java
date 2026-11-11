package report;

import static org.junit.jupiter.api.Assertions.assertEquals;

import domain.AudioKit;
import domain.Book;
import domain.LendingItem;
import domain.Library;
import domain.Loan;
import domain.Member;
import domain.MemberKind;
import exception.ItemAlreadyOnLoanException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** Checks the wording of each message, one message per test, in the original's format. */
class ReportPrinterTest {

    private static final LocalDate BORROW_DATE = LocalDate.of(2026, 1, 10);

    private final Member kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
    private final Member ama = new Member("Ama Owusu", MemberKind.STUDENT);
    private final LendingItem novel = new Book("Things Fall Apart");
    private final LendingItem audioCourse = new AudioKit("French Audio Course");
    private final Library library = withMembersAndItems(new Library("The Reading Room",
            Clock.fixed(BORROW_DATE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC)));
    private final ByteArrayOutputStream printed = new ByteArrayOutputStream();
    private final ReportPrinter printer = new ReportPrinter(new PrintStream(printed, true, StandardCharsets.UTF_8));

    // JUnit creates a new instance for every test, so each test starts from this state.
    private Library withMembersAndItems(Library newLibrary) {
        newLibrary.addMember(kofi);
        newLibrary.addMember(ama);
        newLibrary.addItem(novel);
        newLibrary.addItem(audioCourse);
        return newLibrary;
    }

    @Test
    void lentMessage() {
        printer.printLent(library.borrow(ama, novel));

        assertEquals(lines("Lent \"Things Fall Apart\" to Ama Owusu for 28 days"), printedText());
    }

    @Test
    void returnedMessage() {
        printer.printReturned(audioCourse);

        assertEquals(lines("Returned \"French Audio Course\""), printedText());
    }

    @Test
    void refusedMessage() {
        library.borrow(kofi, novel);
        ItemAlreadyOnLoanException refusal = new ItemAlreadyOnLoanException(novel, kofi);

        printer.printRefused(ama, refusal);

        assertEquals(lines("Cannot lend \"Things Fall Apart\" to Ama Owusu: already on loan to Kofi Mensah"),
                printedText());
    }

    @Test
    void endOfDayReportListsEveryItemAndOnlyTheActiveLoans() {
        Loan kofisLoan = library.borrow(kofi, novel);
        library.borrow(ama, audioCourse);
        library.returnItem(audioCourse);

        printer.printEndOfDay(library);

        assertEquals(lines(
                "",
                "=== The Reading Room - end of day ===",
                "Items:",
                "  \"Things Fall Apart\" (BOOK): ON_LOAN - with Kofi Mensah",
                "  \"French Audio Course\" (AUDIO_KIT): AVAILABLE",
                "Active loans:",
                "  Loan #" + kofisLoan.getId() + ": \"Things Fall Apart\" -> Kofi Mensah"), printedText());
    }

    @Test
    void endOfDayReportWithNothingOnLoan() {
        printer.printEndOfDay(library);

        assertEquals(lines(
                "",
                "=== The Reading Room - end of day ===",
                "Items:",
                "  \"Things Fall Apart\" (BOOK): AVAILABLE",
                "  \"French Audio Course\" (AUDIO_KIT): AVAILABLE",
                "Active loans:"), printedText());
    }

    @Test
    void dueDatesViewListsTheEarliestDueFirstAndMarksOverdueLoans() {
        library.borrow(ama, novel);
        library.borrow(kofi, audioCourse);

        printer.printDueDates(library, LocalDate.of(2026, 1, 20));

        assertEquals(lines(
                "",
                "Due dates (as of 2026-01-20):",
                "  Loan #2: \"French Audio Course\" -> Kofi Mensah, due 2026-01-17 (OVERDUE by 3 days)",
                "  Loan #1: \"Things Fall Apart\" -> Ama Owusu, due 2026-02-07"), printedText());
    }

    @Test
    void dueDatesViewSaysOneDayInTheSingular() {
        library.borrow(kofi, audioCourse);

        printer.printDueDates(library, LocalDate.of(2026, 1, 18));

        assertEquals(lines(
                "",
                "Due dates (as of 2026-01-18):",
                "  Loan #1: \"French Audio Course\" -> Kofi Mensah, due 2026-01-17 (OVERDUE by 1 day)"),
                printedText());
    }

    @Test
    void dueDatesViewSaysWhenNothingIsOnLoan() {
        printer.printDueDates(library, BORROW_DATE);

        assertEquals(lines("", "Due dates (as of 2026-01-10):", "  No active loans"), printedText());
    }

    private static String lines(String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }

    private String printedText() {
        return printed.toString(StandardCharsets.UTF_8);
    }
}
