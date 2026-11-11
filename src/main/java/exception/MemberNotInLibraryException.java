package exception;

import domain.Member;

/** Thrown when someone who was never added to the library as a member tries to borrow. */
public final class MemberNotInLibraryException extends LendingException {

    private static final long serialVersionUID = 1L;

    private final transient Member member;

    public MemberNotInLibraryException(Member member) {
        super(member.getName() + " is not a member of this library");
        this.member = member;
    }

    /** The person who could not borrow. */
    public Member getMember() {
        return member;
    }
}
