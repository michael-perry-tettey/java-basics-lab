import domain.AudioKit;
import domain.Book;
import domain.LendingItem;
import domain.Library;
import domain.Member;
import domain.MemberKind;
import exception.ItemAlreadyOnLoanException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import report.ReportPrinter;

// The Reading Room - community library lending tracker
public class LendingTracker {

    private static final String DUE_DATES_OPTION = "--due-dates";

    public static void main(String[] args) {
        Library library = new Library("The Reading Room");
        ReportPrinter printer = new ReportPrinter(System.out);

        LendingItem novel = new Book("Things Fall Apart");
        LendingItem audioCourse = new AudioKit("French Audio Course");
        library.addItem(novel);
        library.addItem(audioCourse);

        Member kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
        Member ama = new Member("Ama Owusu", MemberKind.STUDENT);
        library.addMember(kofi);
        library.addMember(ama);

        // a normal afternoon
        printer.printLent(library.borrow(kofi, novel));
        printer.printLent(library.borrow(ama, audioCourse));
        library.returnItem(audioCourse);
        printer.printReturned(audioCourse);

        // ... and the sort of thing that used to worry them:
        // the novel is already out with Kofi, so the library refuses to lend it again
        try {
            printer.printLent(library.borrow(ama, novel));
        } catch (ItemAlreadyOnLoanException refusal) {
            printer.printRefused(ama, refusal);
        }

        printer.printEndOfDay(library);

        printDueDatesIfAsked(args, library, printer);
    }

    /**
     * Prints the due-dates view only when the program is started with
     * --due-dates, optionally followed by a date such as 2026-12-25 to see
     * what would be overdue on that day. Without the option the program
     * prints exactly what it always has.
     */
    private static void printDueDatesIfAsked(String[] args, Library library, ReportPrinter printer) {
        if (args.length == 0 || !DUE_DATES_OPTION.equals(args[0])) {
            return;
        }
        try {
            LocalDate asOf = args.length > 1 ? LocalDate.parse(args[1]) : library.today();
            printer.printDueDates(library, asOf);
        } catch (DateTimeParseException badDate) {
            System.err.println("Cannot read the date \"" + args[1] + "\". Use the form 2026-12-25.");
        }
    }
}
