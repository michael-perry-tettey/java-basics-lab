package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DueDateTest {

    private static final LocalDate BORROW_DATE = LocalDate.of(2026, 1, 10);

    private Library library;
    private Member kofi;
    private Member ama;
    private LendingItem novel;
    private LendingItem audioCourse;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(BORROW_DATE.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        library = new Library("Test library", fixedClock);
        kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
        ama = new Member("Ama Owusu", MemberKind.STUDENT);
        novel = new Book("Things Fall Apart");
        audioCourse = new AudioKit("French Audio Course");
        library.addMember(kofi);
        library.addMember(ama);
        library.addItem(novel);
        library.addItem(audioCourse);
    }

    @Test
    void aLoanRecordsTheDateItWasMade() {
        assertEquals(BORROW_DATE, library.borrow(kofi, novel).getBorrowedOn());
    }

    @Test
    void theDueDateIsTheBorrowDatePlusTheLoanPeriod() {
        assertEquals(LocalDate.of(2026, 1, 24), library.borrow(kofi, novel).getDueDate());
        assertEquals(LocalDate.of(2026, 1, 24), library.borrow(ama, audioCourse).getDueDate());
    }

    @Test
    void aStudentsBookIsDue28DaysLater() {
        assertEquals(LocalDate.of(2026, 2, 7), library.borrow(ama, novel).getDueDate());
    }

    @Test
    void aLoanIsNotOverdueOnItsDueDate() {
        Loan loan = library.borrow(kofi, novel);

        assertFalse(loan.isOverdueOn(LocalDate.of(2026, 1, 24)));
    }

    @Test
    void aLoanIsOverdueTheDayAfterItsDueDate() {
        Loan loan = library.borrow(kofi, novel);

        assertTrue(loan.isOverdueOn(LocalDate.of(2026, 1, 25)));
    }

    @Test
    void aReturnedLoanIsNeverOverdue() {
        Loan loan = library.borrow(kofi, novel);
        library.returnItem(novel);

        assertFalse(loan.isOverdueOn(LocalDate.of(2026, 12, 31)));
    }

    @Test
    void activeLoansAreListedWithTheEarliestDueDateFirst() {
        Loan studentBookLoan = library.borrow(ama, novel);
        Loan standardKitLoan = library.borrow(kofi, audioCourse);

        // The kit was borrowed second but is due first: 7 days against 28.
        assertEquals(List.of(standardKitLoan, studentBookLoan), library.getActiveLoansByDueDate());
        assertEquals(List.of(studentBookLoan, standardKitLoan), library.getActiveLoans());
    }

    @Test
    void returnedLoansAreLeftOutOfTheDueDateList() {
        library.borrow(ama, novel);
        Loan kitLoan = library.borrow(kofi, audioCourse);
        library.returnItem(novel);

        assertEquals(List.of(kitLoan), library.getActiveLoansByDueDate());
    }
}
