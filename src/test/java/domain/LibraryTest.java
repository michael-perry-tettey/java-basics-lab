package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.ItemAlreadyOnLoanException;
import exception.ItemNotInLibraryException;
import exception.ItemNotOnLoanException;
import exception.LendingException;
import exception.MemberNotInLibraryException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class LibraryTest {

    private final Member kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
    private final Member ama = new Member("Ama Owusu", MemberKind.STUDENT);
    private final LendingItem novel = new Book("Things Fall Apart");
    private final LendingItem audioCourse = new AudioKit("French Audio Course");
    private final Library library = withMembersAndItems(new Library("Test library"));

    // JUnit creates a new instance for every test, so each test starts from this state.
    private Library withMembersAndItems(Library newLibrary) {
        newLibrary.addMember(kofi);
        newLibrary.addMember(ama);
        newLibrary.addItem(novel);
        newLibrary.addItem(audioCourse);
        return newLibrary;
    }

    // Borrowing and returning

    @Test
    void borrowingCreatesAnActiveLoanForThatMemberAndItem() {
        Loan loan = library.borrow(kofi, novel);

        assertSame(kofi, loan.getMember());
        assertSame(novel, loan.getItem());
        assertEquals(14, loan.getLoanDays());
        assertTrue(loan.isActive());
        assertEquals(ItemStatus.ON_LOAN, library.statusOf(novel));
        assertEquals(Optional.of(kofi), library.currentBorrowerOf(novel));
    }

    @Test
    void borrowingIsRecordedInTheMembersHistory() {
        library.borrow(kofi, novel);

        assertEquals(List.of("Borrowed Things Fall Apart"), kofi.getHistory());
    }

    @Test
    void loanNumbersStartAtOneInEachLibrary() {
        Library anotherLibrary = new Library("Another library");
        LendingItem anotherBook = new Book("Another book");
        anotherLibrary.addMember(kofi);
        anotherLibrary.addItem(anotherBook);

        assertEquals(1, library.borrow(kofi, novel).getId());
        assertEquals(2, library.borrow(ama, audioCourse).getId());
        assertEquals(1, anotherLibrary.borrow(kofi, anotherBook).getId());
    }

    @Test
    void returningAnItemMakesItAvailableAgain() {
        Loan loan = library.borrow(kofi, novel);

        library.returnItem(novel);

        assertEquals(LoanStatus.RETURNED, loan.getStatus());
        assertEquals(ItemStatus.AVAILABLE, library.statusOf(novel));
        assertEquals(Optional.empty(), library.currentBorrowerOf(novel));
        assertTrue(library.getActiveLoans().isEmpty());
    }

    @Test
    void anItemCanBeBorrowedAgainOnceItIsReturned() {
        library.borrow(kofi, novel);
        library.returnItem(novel);

        Loan secondLoan = library.borrow(ama, novel);

        assertTrue(secondLoan.isActive());
        assertEquals(28, secondLoan.getLoanDays());
        assertEquals(Optional.of(ama), library.currentBorrowerOf(novel));
        assertEquals(2, library.getLoans().size());
    }

    @Test
    void returningOneCopyLeavesTheOtherCopyOfTheSameTitleOnLoan() {
        LendingItem secondCopy = new Book("Things Fall Apart");
        library.addItem(secondCopy);
        library.borrow(kofi, novel);
        Loan secondLoan = library.borrow(ama, secondCopy);

        library.returnItem(novel);

        assertTrue(secondLoan.isActive());
        assertEquals(ItemStatus.ON_LOAN, library.statusOf(secondCopy));
        assertEquals(Optional.of(ama), library.currentBorrowerOf(secondCopy));
    }

    // One loan per item

    @Test
    void anItemAlreadyOnLoanCannotBeBorrowedAgain() {
        library.borrow(kofi, novel);

        ItemAlreadyOnLoanException refusal =
                assertThrows(ItemAlreadyOnLoanException.class, () -> library.borrow(ama, novel));

        assertSame(novel, refusal.getItem());
        assertSame(kofi, refusal.getCurrentBorrower());
    }

    @Test
    void theSameMemberCannotBorrowAnItemTheyAlreadyHave() {
        library.borrow(kofi, novel);

        assertFails(ItemAlreadyOnLoanException.class, () -> library.borrow(kofi, novel));
    }

    @Test
    void aRefusedLoanChangesNothing() {
        Loan kofisLoan = library.borrow(kofi, novel);

        assertFails(ItemAlreadyOnLoanException.class, () -> library.borrow(ama, novel));

        assertEquals(List.of(kofisLoan), library.getLoans());
        assertEquals(List.of(kofisLoan), library.getActiveLoans());
        assertEquals(Optional.of(kofi), library.currentBorrowerOf(novel));
        assertTrue(ama.getHistory().isEmpty());
        assertEquals(2, library.borrow(ama, audioCourse).getId());
    }

    // Other refusals

    @Test
    void anItemThatWasNeverAddedCannotBeBorrowed() {
        LendingItem stranger = new Book("Not ours");

        ItemNotInLibraryException refusal =
                assertThrows(ItemNotInLibraryException.class, () -> library.borrow(kofi, stranger));

        assertSame(stranger, refusal.getItem());
        assertTrue(library.getLoans().isEmpty());
        assertTrue(kofi.getHistory().isEmpty());
    }

    @Test
    void someoneWhoIsNotAMemberCannotBorrow() {
        Member visitor = new Member("Yaw Boateng", MemberKind.STANDARD);

        MemberNotInLibraryException refusal =
                assertThrows(MemberNotInLibraryException.class, () -> library.borrow(visitor, novel));

        assertSame(visitor, refusal.getMember());
        assertEquals(ItemStatus.AVAILABLE, library.statusOf(novel));
        assertTrue(visitor.getHistory().isEmpty());
    }

    @Test
    void anItemThatIsNotOnLoanCannotBeReturned() {
        ItemNotOnLoanException refusal =
                assertThrows(ItemNotOnLoanException.class, () -> library.returnItem(novel));

        assertSame(novel, refusal.getItem());
        assertEquals(ItemStatus.AVAILABLE, library.statusOf(novel));
    }

    @Test
    void anItemCannotBeReturnedTwice() {
        library.borrow(kofi, novel);
        library.returnItem(novel);

        assertFails(ItemNotOnLoanException.class, () -> library.returnItem(novel));
    }

    @Test
    void everyRefusalCanBeCaughtAsALendingException() {
        library.borrow(kofi, novel);

        List<Class<?>> refusals = List.of(
                assertThrows(LendingException.class, () -> library.borrow(ama, novel)).getClass(),
                assertThrows(LendingException.class, () -> library.borrow(kofi, new Book("Not ours"))).getClass(),
                assertThrows(LendingException.class, () -> library.returnItem(audioCourse)).getClass());

        assertEquals(List.of(ItemAlreadyOnLoanException.class, ItemNotInLibraryException.class,
                ItemNotOnLoanException.class), refusals);
    }

    @Test
    void anItemKindWithNoLoanPeriodCannotBeLent() {
        LendingItem broken = new LendingItem("Mistyped e-loan") {
            @Override
            public String typeLabel() {
                return "E_LOAN";
            }

            @Override
            protected int baseLoanDays() {
                return 0;
            }
        };
        library.addItem(broken);

        IllegalStateException error =
                assertThrows(IllegalStateException.class, () -> library.borrow(kofi, broken));

        assertTrue(error.getMessage().contains("E_LOAN items must be lent for at least 1 day"));
        assertEquals(ItemStatus.AVAILABLE, library.statusOf(broken));
        assertTrue(library.getLoans().isEmpty());
    }

    // Adding items and members

    @Test
    void theSameItemCannotBeAddedTwice() {
        assertFails(IllegalArgumentException.class, () -> library.addItem(novel));

        assertEquals(2, library.getItems().size());
    }

    @Test
    void theSameMemberCannotBeAddedTwice() {
        assertFails(IllegalArgumentException.class, () -> library.addMember(kofi));

        assertEquals(2, library.getMembers().size());
    }

    @Test
    void aBlankTitleOrNameIsRejected() {
        assertFails(IllegalArgumentException.class, () -> new Book("   "));
        assertFails(IllegalArgumentException.class, () -> new AudioKit(""));
        assertFails(IllegalArgumentException.class, () -> new Member(" ", MemberKind.STANDARD));
        assertFails(IllegalArgumentException.class, () -> new Library(""));
    }

    // Nothing can be changed from outside

    @Test
    void aReturnedLoanCannotBeMarkedReturnedAgain() {
        Loan loan = library.borrow(kofi, novel);
        library.returnItem(novel);

        assertFails(IllegalStateException.class, loan::markReturned);
    }

    @Test
    void theLibrarysListsCannotBeChangedFromOutside() {
        library.borrow(kofi, novel);

        assertFails(UnsupportedOperationException.class, () -> library.getLoans().clear());
        assertFails(UnsupportedOperationException.class, () -> library.getActiveLoans().clear());
        assertFails(UnsupportedOperationException.class, () -> library.getItems().clear());
        assertFails(UnsupportedOperationException.class, () -> library.getMembers().clear());
        assertEquals(1, library.getLoans().size());
    }

    @Test
    void aMembersHistoryCannotBeChangedFromOutside() {
        library.borrow(kofi, novel);

        assertFails(UnsupportedOperationException.class, () -> kofi.getHistory().add("Borrowed nothing"));
    }

    /** Checks that the action throws exactly the given kind of exception. */
    private static void assertFails(Class<? extends Throwable> expected, Executable action) {
        assertEquals(expected, assertThrows(Throwable.class, action).getClass());
    }
}
