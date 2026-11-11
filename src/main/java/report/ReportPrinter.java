package report;

import domain.LendingItem;
import domain.Library;
import domain.Loan;
import domain.Member;
import exception.ItemAlreadyOnLoanException;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Writes the tracker's messages and the end-of-day report. It only reads
 * from the library; it cannot change who has what.
 */
public final class ReportPrinter {

    private final PrintStream out;

    public ReportPrinter(PrintStream out) {
        this.out = Objects.requireNonNull(out, "out");
    }

    public void printLent(Loan loan) {
        out.println("Lent \"" + loan.getItem().getTitle() + "\" to " + loan.getMember().getName()
                + " for " + loan.getLoanDays() + " days");
    }

    public void printRefused(Member member, ItemAlreadyOnLoanException refusal) {
        out.println("Cannot lend \"" + refusal.getItem().getTitle() + "\" to " + member.getName()
                + ": already on loan to " + refusal.getCurrentBorrower().getName());
    }

    public void printReturned(LendingItem item) {
        out.println("Returned \"" + item.getTitle() + "\"");
    }

    public void printEndOfDay(Library library) {
        out.println();
        out.println("=== " + library.getName() + " - end of day ===");
        out.println("Items:");
        for (LendingItem item : library.getItems()) {
            String borrower = library.currentBorrowerOf(item)
                    .map(member -> " - with " + member.getName())
                    .orElse("");
            out.println("  \"" + item.getTitle() + "\" (" + item.typeLabel() + "): "
                    + library.statusOf(item) + borrower);
        }
        out.println("Active loans:");
        for (Loan loan : library.getActiveLoans()) {
            out.println("  Loan #" + loan.getId() + ": \"" + loan.getItem().getTitle() + "\" -> "
                    + loan.getMember().getName());
        }
    }
}
