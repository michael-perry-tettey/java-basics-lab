package com.amalitech.training.readingroom;

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
    private LoanStatus status = LoanStatus.ACTIVE;

    Loan(int id, Member member, LendingItem item, int loanDays) {
        this.id = id;
        this.member = member;
        this.item = item;
        this.loanDays = loanDays;
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
