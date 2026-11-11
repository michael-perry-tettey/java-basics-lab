package com.amalitech.training.readingroom;

/**
 * Something the library lends. Each kind of item is its own subclass and
 * states its own loan rules, so adding a new kind does not mean editing
 * the rules of the others.
 */
abstract class LendingItem {
    public String title;
    public ItemStatus status;
    public String currentBorrower;
    public int id;
    static int counter = 1;

    protected LendingItem(String title) {
        this.id = counter;
        counter++;
        this.title = title;
        this.status = ItemStatus.AVAILABLE;
    }

    /** The label the end-of-day report prints for this kind of item. */
    abstract String typeLabel();

    /** How many days a standard member may keep this kind of item. */
    abstract int baseLoanDays();

    /**
     * How many days the given kind of member may keep this item: the base
     * period times the member kind's multiplier. A kind of item with
     * different rules can replace this calculation.
     */
    int loanDaysFor(MemberKind memberKind) {
        return baseLoanDays() * memberKind.loanMultiplier();
    }
}
