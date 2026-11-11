package com.amalitech.training.readingroom;

/** An audio kit, lent for a shorter period than a book. */
public final class AudioKit extends LendingItem {

    public AudioKit(String title) {
        super(title);
    }

    @Override
    public String typeLabel() {
        return "AUDIO_KIT";
    }

    @Override
    protected int baseLoanDays() {
        return 7;
    }
}
