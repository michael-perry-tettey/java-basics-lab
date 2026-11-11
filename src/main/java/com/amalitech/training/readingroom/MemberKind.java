package com.amalitech.training.readingroom;

/** The kinds of library member. Students are allowed longer loans. */
public enum MemberKind {
    STANDARD(1),
    STUDENT(2);

    private final int loanMultiplier;

    MemberKind(int loanMultiplier) {
        this.loanMultiplier = loanMultiplier;
    }

    /** What an item's base loan period is multiplied by for this kind of member. */
    public int loanMultiplier() {
        return loanMultiplier;
    }
}
