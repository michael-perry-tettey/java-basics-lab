package domain;

/**
 * A digital item, such as an e-book. Added to show that a new kind of item
 * needs only this one file: nothing else in the program is edited.
 *
 * The library has not yet given the e-loan rules, so the loan period here
 * is a placeholder, and students get double as they do for other items.
 */
public final class ELoan extends LendingItem {

    public ELoan(String title) {
        super(title);
    }

    @Override
    public String typeLabel() {
        return "E_LOAN";
    }

    @Override
    protected int baseLoanDays() {
        return 21;
    }
}
