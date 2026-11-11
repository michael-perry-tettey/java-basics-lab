package com.amalitech.training.readingroom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LibraryTest {

    private Library library;
    private Member kofi;
    private Member ama;
    private LendingItem novel;

    @BeforeEach
    void setUp() {
        library = new Library("Test library");
        kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
        ama = new Member("Ama Owusu", MemberKind.STUDENT);
        novel = new Book("Things Fall Apart");
        library.addMember(kofi);
        library.addMember(ama);
        library.addItem(novel);
    }

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
        assertEquals(1, library.borrow(kofi, novel).getId());
        assertEquals(2, library.borrow(ama, new AudioKit("French Audio Course")).getId());
        assertEquals(1, new Library("Another library").borrow(kofi, new Book("Another book")).getId());
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

    @Test
    void aReturnedLoanCannotBeReturnedAgain() {
        Loan loan = library.borrow(kofi, novel);
        loan.markReturned();

        assertThrows(IllegalStateException.class, loan::markReturned);
    }

    @Test
    void theLoanListCannotBeChangedFromOutside() {
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

    @Test
    void itemsAndMembersCanBeFoundByTheirId() {
        assertEquals(Optional.of(novel), library.findItem(novel.getId()));
        assertEquals(Optional.of(kofi), library.findMember(kofi.getId()));
        assertFalse(library.findItem(-1).isPresent());
    }

    @Test
    void addingTheSameItemTwiceKeepsOneEntry() {
        library.addItem(novel);

        assertEquals(1, library.getItems().size());
    }
}
