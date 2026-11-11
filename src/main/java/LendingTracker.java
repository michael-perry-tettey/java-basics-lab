/**
 * Entry point kept under its original name, outside any package, so the
 * program still starts with the same commands as the original.
 *
 * The program itself lives in the com.amalitech.training.readingroom package;
 * this class only hands over to it.
 */
public class LendingTracker {

    public static void main(String[] args) {
        com.amalitech.training.readingroom.LendingTracker.main(args);
    }
}
