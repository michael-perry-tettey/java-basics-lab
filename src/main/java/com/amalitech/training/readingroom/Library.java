package com.amalitech.training.readingroom;

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
 */
public final class Library {

    private final String name;
    private final Map<Integer, LendingItem> items = new LinkedHashMap<>();
    private final Map<Integer, Member> members = new LinkedHashMap<>();
    private final List<Loan> loans = new ArrayList<>();
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

    /** Lends an item to a member. The item works out the loan period for that kind of member. */
    public Loan borrow(Member member, LendingItem item) {
        Objects.requireNonNull(member, "member");
        Objects.requireNonNull(item, "item");
        Loan loan = new Loan(nextLoanId++, member, item, item.loanDaysFor(member.getKind()));
        loans.add(loan);
        member.recordBorrowed(item);
        return loan;
    }

    /** Takes an item back, closing the active loans for that exact item. */
    public void returnItem(LendingItem item) {
        Objects.requireNonNull(item, "item");
        for (Loan loan : loans) {
            if (loan.isActive() && loan.getItem() == item) {
                loan.markReturned();
            }
        }
    }

    /** Whether the item is out, worked out from the loans and stored nowhere else. */
    public ItemStatus statusOf(LendingItem item) {
        return currentBorrowerOf(item).isPresent() ? ItemStatus.ON_LOAN : ItemStatus.AVAILABLE;
    }

    /** The member who most recently borrowed the item and has not returned it, if any. */
    public Optional<Member> currentBorrowerOf(LendingItem item) {
        Member borrower = null;
        for (Loan loan : loans) {
            if (loan.isActive() && loan.getItem() == item) {
                borrower = loan.getMember();
            }
        }
        return Optional.ofNullable(borrower);
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
        return Collections.unmodifiableList(loans);
    }

    /** The loans that have not been returned, in the order they were made. */
    public List<Loan> getActiveLoans() {
        return loans.stream().filter(Loan::isActive).toList();
    }
}
