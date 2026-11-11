package com.amalitech.training.readingroom;

// The Reading Room - community library lending tracker
public class LendingTracker {

    public static void main(String[] args) {
        Library library = new Library("The Reading Room");
        ReportPrinter printer = new ReportPrinter(System.out);

        LendingItem novel = new Book("Things Fall Apart");
        LendingItem audioCourse = new AudioKit("French Audio Course");
        library.addItem(novel);
        library.addItem(audioCourse);

        Member kofi = new Member("Kofi Mensah", MemberKind.STANDARD);
        Member ama = new Member("Ama Owusu", MemberKind.STUDENT);
        library.addMember(kofi);
        library.addMember(ama);

        // a normal afternoon
        printer.printLent(library.borrow(kofi, novel));
        printer.printLent(library.borrow(ama, audioCourse));
        library.returnItem(audioCourse);
        printer.printReturned(audioCourse);

        // ... and the sort of thing that used to worry them:
        // the novel is already out with Kofi, so the library refuses to lend it again
        try {
            printer.printLent(library.borrow(ama, novel));
        } catch (ItemAlreadyOnLoanException refusal) {
            printer.printRefused(ama, refusal);
        }

        printer.printEndOfDay(library);
    }
}
