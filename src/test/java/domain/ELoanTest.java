package domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import exception.ItemAlreadyOnLoanException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Shows that an e-loan works with the existing library without any other change. */
class ELoanTest {

    private final Member kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
    private final Member ama = new Member("Ama Owusu", MemberKind.STUDENT);
    private final LendingItem eBook = new ELoan("Half of a Yellow Sun");
    private final Library library = withMembersAndItems(new Library("The Reading Room"));

    // JUnit creates a new instance for every test, so each test starts from this state.
    private Library withMembersAndItems(Library newLibrary) {
        newLibrary.addMember(kofi);
        newLibrary.addMember(ama);
        newLibrary.addItem(eBook);
        return newLibrary;
    }

    @Test
    void anELoanIsLentForItsOwnLoanPeriod() {
        assertEquals(21, library.borrow(kofi, eBook).getLoanDays());
    }

    @Test
    void studentsGetDoubleAsForOtherItems() {
        assertEquals(42, library.borrow(ama, eBook).getLoanDays());
    }

    @Test
    void theOneLoanPerItemRuleAppliesToELoansToo() {
        library.borrow(kofi, eBook);

        assertEquals(Optional.of(kofi), library.currentBorrowerOf(eBook));
        assertEquals(ItemAlreadyOnLoanException.class,
                assertThrows(ItemAlreadyOnLoanException.class, () -> library.borrow(ama, eBook)).getClass());
    }

    @Test
    void theReportLabelsItAsAnELoan() {
        assertEquals("E_LOAN", eBook.typeLabel());
    }
}
