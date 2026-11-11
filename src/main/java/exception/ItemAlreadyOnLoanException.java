package exception;

import domain.LendingItem;
import domain.Member;

/**
 * Thrown when someone tries to borrow an item that is already on loan.
 * An item can only ever be on one loan at a time.
 */
public final class ItemAlreadyOnLoanException extends RuntimeException {

    private final transient LendingItem item;
    private final transient Member currentBorrower;

    public ItemAlreadyOnLoanException(LendingItem item, Member currentBorrower) {
        super("\"" + item.getTitle() + "\" is already on loan to " + currentBorrower.getName());
        this.item = item;
        this.currentBorrower = currentBorrower;
    }

    /** The item that could not be lent. */
    public LendingItem getItem() {
        return item;
    }

    /** The member who has the item now. */
    public Member getCurrentBorrower() {
        return currentBorrower;
    }
}
