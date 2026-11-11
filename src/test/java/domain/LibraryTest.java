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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LibraryTest {

    private Library library;
    private Member kofi;
    private Member ama;
    private LendingItem novel;
    private LendingItem audioCourse;

    @BeforeEach
    void setUp() {
        library = new Library("Test library");
        kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
        ama = new Member("Ama Owusu", MemberKind.STUDENT);
        novel = new Book("Things Fall Apart");
        audioCourse = new AudioKit("French Audio Course");
        library.addMember(kofi);
        library.addMember(ama);
        library.addItem(novel);
        library.addItem(audioCourse);
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

        assertThrows(ItemAlreadyOnLoanException.class, () -> library.borrow(kofi, novel));
    }

    @Test
    void aRefusedLoanChangesNothing() {
        Loan kofisLoan = library.borrow(kofi, novel);

        assertThrows(ItemAlreadyOnLoanException.class, () -> library.borrow(ama, novel));

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

        assertThrows(ItemNotOnLoanException.class, () -> library.returnItem(novel));
    }

    @Test
    void everyRefusalCanBeCaughtAsALendingException() {
        library.borrow(kofi, novel);

        assertThrows(LendingException.class, () -> library.borrow(ama, novel));
        assertThrows(LendingException.class, () -> library.borrow(kofi, new Book("Not ours")));
        assertThrows(LendingException.class, () -> library.returnItem(audioCourse));
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
        assertThrows(IllegalArgumentException.class, () -> library.addItem(novel));

        assertEquals(2, library.getItems().size());
    }

    @Test
    void theSameMemberCannotBeAddedTwice() {
        assertThrows(IllegalArgumentException.class, () -> library.addMember(kofi));

        assertEquals(2, library.getMembers().size());
    }

    @Test
    void aBlankTitleOrNameIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Book("   "));
        assertThrows(IllegalArgumentException.class, () -> new AudioKit(""));
        assertThrows(IllegalArgumentException.class, () -> new Member(" ", MemberKind.STANDARD));
        assertThrows(IllegalArgumentException.class, () -> new Library(""));
    }

    // Nothing can be changed from outside

    @Test
    void aReturnedLoanCannotBeMarkedReturnedAgain() {
        Loan loan = library.borrow(kofi, novel);
        library.returnItem(novel);

        assertThrows(IllegalStateException.class, loan::markReturned);
    }

    @Test
    void theLibrarysListsCannotBeChangedFromOutside() {
        library.borrow(kofi, novel);

        assertThrows(UnsupportedOperationException.class, () -> library.getLoans().clear());
        assertThrows(UnsupportedOperationException.class, () -> library.getActiveLoans().clear());
        assertThrows(UnsupportedOperationException.class, () -> library.getItems().clear());
        assertThrows(UnsupportedOperationException.class, () -> library.getMembers().clear());
        assertEquals(1, library.getLoans().size());
    }

    @Test
    void aMembersHistoryCannotBeChangedFromOutside() {
        library.borrow(kofi, novel);

        assertThrows(UnsupportedOperationException.class, () -> kofi.getHistory().add("Borrowed nothing"));
    }
}
