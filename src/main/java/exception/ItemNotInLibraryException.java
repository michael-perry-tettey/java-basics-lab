package exception;

import domain.LendingItem;

/** Thrown when someone tries to borrow an item that was never added to the library. */
public final class ItemNotInLibraryException extends LendingException {

    private static final long serialVersionUID = 1L;

    private final transient LendingItem item;

    public ItemNotInLibraryException(LendingItem item) {
        super("\"" + item.getTitle() + "\" has not been added to this library");
        this.item = item;
    }

    /** The item that could not be lent. */
    public LendingItem getItem() {
        return item;
    }
}
