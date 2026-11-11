package exception;

import domain.LendingItem;

/** Thrown when someone tries to return an item that is not on loan. */
public final class ItemNotOnLoanException extends LendingException {

    private static final long serialVersionUID = 1L;

    private final transient LendingItem item;

    public ItemNotOnLoanException(LendingItem item) {
        super("\"" + item.getTitle() + "\" is not on loan");
        this.item = item;
    }

    /** The item that could not be returned. */
    public LendingItem getItem() {
        return item;
    }
}
