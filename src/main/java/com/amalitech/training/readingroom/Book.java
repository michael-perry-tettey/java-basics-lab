package com.amalitech.training.readingroom;

/** A physical book. */
class Book extends LendingItem {

    Book(String title) {
        super(title);
    }

    @Override
    String typeLabel() {
        return "BOOK";
    }

    @Override
    int baseLoanDays() {
        return 14;
    }
}
