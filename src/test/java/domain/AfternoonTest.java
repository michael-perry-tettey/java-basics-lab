package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import exception.ItemAlreadyOnLoanException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Checks that the application keeps the original program's behaviour. It
 * runs the same afternoon as the original, step by step, and checks the
 * outcome of each step.
 *
 * Every step gives the same result as the original except the last borrow,
 * which the original allowed and which is now refused.
 */
class AfternoonTest {

    @Test
    void theOriginalAfternoonGivesTheSameResultsExceptThatTheDoubleLoanIsRefused() {
        Library library = new Library("The Reading Room");
        LendingItem novel = new Book("Things Fall Apart");
        LendingItem audioCourse = new AudioKit("French Audio Course");
        Member kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
        Member ama = new Member("Ama Owusu", MemberKind.STUDENT);
        library.addItem(novel);
        library.addItem(audioCourse);
        library.addMember(kofi);
        library.addMember(ama);

        // As in the original: Kofi borrows the novel for 14 days.
        Loan kofisLoan = library.borrow(kofi, novel);
        assertEquals(1, kofisLoan.getId());
        assertEquals(14, kofisLoan.getLoanDays());

        // As in the original: Ama, a student, borrows the audio course for 14 days.
        Loan amasLoan = library.borrow(ama, audioCourse);
        assertEquals(2, amasLoan.getId());
        assertEquals(14, amasLoan.getLoanDays());

        // As in the original: the audio course comes back and is available again.
        library.returnItem(audioCourse);
        assertEquals(LoanStatus.RETURNED, amasLoan.getStatus());
        assertEquals(ItemStatus.AVAILABLE, library.statusOf(audioCourse));

        // The one change: the original lent the novel to Ama while Kofi had it.
        ItemAlreadyOnLoanException refusal =
                assertThrows(ItemAlreadyOnLoanException.class, () -> library.borrow(ama, novel));
        assertSame(kofi, refusal.getCurrentBorrower());

        // End of day: the novel is with Kofi, who actually has it, and his loan
        // is the only active one. The original showed Ama and a second loan.
        assertEquals(ItemStatus.ON_LOAN, library.statusOf(novel));
        assertEquals(Optional.of(kofi), library.currentBorrowerOf(novel));
        assertEquals(List.of(kofisLoan), library.getActiveLoans());
        assertEquals(List.of("Borrowed Things Fall Apart"), kofi.getHistory());
        assertEquals(List.of("Borrowed French Audio Course"), ama.getHistory());
    }
}
