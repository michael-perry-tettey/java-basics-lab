package domain;

import java.util.Objects;

/** Checks on values handed to the domain classes. */
final class Checks {

    private Checks() {
    }

    /** Returns the text if it is present and not blank; otherwise rejects it, naming what it was for. */
    static String requireText(String text, String what) {
        Objects.requireNonNull(text, what);
        if (text.isBlank()) {
            throw new IllegalArgumentException(what + " must not be blank");
        }
        return text;
    }
}
