package domain;

/** A physical book. */
public final class Book extends LendingItem {

    public Book(String title) {
        super(title);
    }

    @Override
    public String typeLabel() {
        return "BOOK";
    }

    @Override
    protected int baseLoanDays() {
        return 14;
    }
}
