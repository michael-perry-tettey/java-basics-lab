package domain;

import exception.ItemAlreadyOnLoanException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The library's items, members and loans. This is the only place a loan
 * can be created or closed; everything it hands out is read-only.
 *
 * An item can only ever be on one loan at a time. The active loans are
 * kept by item, one per item, and {@link #borrow} refuses to lend an item
 * that already has one.
 */
public final class Library {

    private final String name;
    private final Map<Integer, LendingItem> items = new LinkedHashMap<>();
    private final Map<Integer, Member> members = new LinkedHashMap<>();
    private final Map<LendingItem, Loan> activeLoans = new LinkedHashMap<>();
    private final List<Loan> loanHistory = new ArrayList<>();
    private int nextLoanId = 1;

    public Library(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getName() {
        return name;
    }

    public void addItem(LendingItem item) {
        items.putIfAbsent(item.getId(), item);
    }

    public void addMember(Member member) {
        members.putIfAbsent(member.getId(), member);
    }

    /**
     * Lends an item to a member. The item works out the loan period for
     * that kind of member.
     *
     * @throws ItemAlreadyOnLoanException if the item is already on loan;
     *         in that case nothing is recorded
     */
    public Loan borrow(Member member, LendingItem item) {
        Objects.requireNonNull(member, "member");
        Objects.requireNonNull(item, "item");

        Loan existing = activeLoans.get(item);
        if (existing != null) {
            throw new ItemAlreadyOnLoanException(item, existing.getMember());
        }

        Loan loan = new Loan(nextLoanId++, member, item, item.loanDaysFor(member.getKind()));
        activeLoans.put(item, loan);
        loanHistory.add(loan);
        member.recordBorrowed(item);
        return loan;
    }

    /** Takes an item back, closing its active loan if it has one. */
    public void returnItem(LendingItem item) {
        Objects.requireNonNull(item, "item");
        Loan loan = activeLoans.remove(item);
        if (loan != null) {
            loan.markReturned();
        }
    }

    /** Whether the item is out, worked out from the active loans and stored nowhere else. */
    public ItemStatus statusOf(LendingItem item) {
        return activeLoans.containsKey(item) ? ItemStatus.ON_LOAN : ItemStatus.AVAILABLE;
    }

    /** The member who has the item now, if it is on loan. */
    public Optional<Member> currentBorrowerOf(LendingItem item) {
        return Optional.ofNullable(activeLoans.get(item)).map(Loan::getMember);
    }

    public Optional<LendingItem> findItem(int id) {
        return Optional.ofNullable(items.get(id));
    }

    public Optional<Member> findMember(int id) {
        return Optional.ofNullable(members.get(id));
    }

    /** The items in the order they were added. Read-only. */
    public Collection<LendingItem> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    /** The members in the order they were added. Read-only. */
    public Collection<Member> getMembers() {
        return Collections.unmodifiableCollection(members.values());
    }

    /** Every loan, active or returned, in the order it was made. Read-only. */
    public List<Loan> getLoans() {
        return Collections.unmodifiableList(loanHistory);
    }

    /** The loans that have not been returned, in the order they were made. */
    public List<Loan> getActiveLoans() {
        return loanHistory.stream().filter(Loan::isActive).toList();
    }
}
