package exception;

/**
 * The parent of every way the library refuses a lending request. Catch this
 * to handle any refusal; catch a subclass to handle one in particular.
 */
public abstract class LendingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected LendingException(String message) {
        super(message);
    }
}
