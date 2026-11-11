package com.amalitech.training.readingroom;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Pins the loan periods to the numbers in the original program:
 * books 14 and 28 days, audio kits 7 and 14 days.
 */
class LoanPeriodTest {

    @Test
    void standardMemberBorrowsABookFor14Days() {
        assertEquals(14, new Book("Any book").loanDaysFor(MemberKind.STANDARD));
    }

    @Test
    void studentBorrowsABookFor28Days() {
        assertEquals(28, new Book("Any book").loanDaysFor(MemberKind.STUDENT));
    }

    @Test
    void standardMemberBorrowsAnAudioKitFor7Days() {
        assertEquals(7, new AudioKit("Any kit").loanDaysFor(MemberKind.STANDARD));
    }

    @Test
    void studentBorrowsAnAudioKitFor14Days() {
        assertEquals(14, new AudioKit("Any kit").loanDaysFor(MemberKind.STUDENT));
    }
}
