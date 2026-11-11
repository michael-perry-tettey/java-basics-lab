package domain;

import java.time.LocalDate;

/**
 * One loan of one item to one member. It points at the actual item and
 * member, not at copies of their names.
 *
 * A loan starts active and can only ever be marked as returned. Only the
 * {@link Library} can create a loan or mark it returned.
 */
public final class Loan {

    private final int id;
    private final Member member;
    private final LendingItem item;
    private final int loanDays;
    private final LocalDate borrowedOn;
    private LoanStatus status = LoanStatus.ACTIVE;

    Loan(int id, Member member, LendingItem item, int loanDays, LocalDate borrowedOn) {
        this.id = id;
        this.member = member;
        this.item = item;
        this.loanDays = loanDays;
        this.borrowedOn = borrowedOn;
    }

    public int getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LendingItem getItem() {
        return item;
    }

    public int getLoanDays() {
        return loanDays;
    }

    public LocalDate getBorrowedOn() {
        return borrowedOn;
    }

    /** The last day the member may keep the item: the borrow date plus the loan period. */
    public LocalDate getDueDate() {
        return borrowedOn.plusDays(loanDays);
    }

    /** Whether the item is still out after its due date, as of the given date. */
    public boolean isOverdueOn(LocalDate date) {
        return isActive() && date.isAfter(getDueDate());
    }

    public LoanStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == LoanStatus.ACTIVE;
    }

    void markReturned() {
        if (!isActive()) {
            throw new IllegalStateException("Loan #" + id + " has already been returned");
        }
        status = LoanStatus.RETURNED;
    }
}
