package com.amalitech.training.readingroom;

/** An audio kit, lent for a shorter period than a book. */
class AudioKit extends LendingItem {

    AudioKit(String title) {
        super(title);
    }

    @Override
    String typeLabel() {
        return "AUDIO_KIT";
    }

    @Override
    int baseLoanDays() {
        return 7;
    }
}
