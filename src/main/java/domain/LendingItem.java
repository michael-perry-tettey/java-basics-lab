package domain;

/**
 * Something the library lends. Each kind of item is its own subclass and
 * states its own loan rules, so adding a new kind does not mean editing
 * the rules of the others.
 *
 * An item never changes after it is created. Whether it is on loan is
 * known only to the {@link Library}.
 */
public abstract class LendingItem {

    private static int nextId = 1;

    private final int id;
    private final String title;

    protected LendingItem(String title) {
        this.title = Checks.requireText(title, "title");
        this.id = nextId++;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    /** The label the end-of-day report prints for this kind of item. */
    public abstract String typeLabel();

    /** How many days a standard member may keep this kind of item. */
    protected abstract int baseLoanDays();

    /**
     * How many days the given kind of member may keep this item: the base
     * period times the member kind's multiplier. A kind of item with
     * different rules can replace this calculation.
     */
    public int loanDaysFor(MemberKind memberKind) {
        return baseLoanDays() * memberKind.loanMultiplier();
    }
}
