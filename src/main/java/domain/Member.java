package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A library member. */
public final class Member {

    private static int nextId = 1;

    private final int id;
    private final String name;
    private final MemberKind kind;
    private final List<String> history = new ArrayList<>();

    public Member(String name, MemberKind kind) {
        this.name = Checks.requireText(name, "name");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.id = nextId++;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MemberKind getKind() {
        return kind;
    }

    /** One entry per borrow, oldest first. Read-only. */
    public List<String> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /** Called by the library when it lends this member an item. */
    void recordBorrowed(LendingItem item) {
        history.add("Borrowed " + item.getTitle());
    }
}
