package domain;

import exception.ItemAlreadyOnLoanException;
import exception.ItemNotInLibraryException;
import exception.ItemNotOnLoanException;
import exception.MemberNotInLibraryException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

    private static final Comparator<Loan> SOONEST_DUE_FIRST =
            Comparator.comparing(Loan::getDueDate).thenComparingInt(Loan::getId);

    private final String name;
    private final Clock clock;
    private final Map<Integer, LendingItem> items = new LinkedHashMap<>();
    private final Map<Integer, Member> members = new LinkedHashMap<>();
    private final Map<LendingItem, Loan> activeLoans = new LinkedHashMap<>();
    private final List<Loan> loanHistory = new ArrayList<>();
    private int nextLoanId = 1;

    public Library(String name) {
        this(name, Clock.systemDefaultZone());
    }

    /** Creates a library that reads today's date from the given clock, so tests can fix the date. */
    public Library(String name, Clock clock) {
        this.name = Checks.requireText(name, "name");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public String getName() {
        return name;
    }

    /** Today's date, as this library sees it. */
    public LocalDate today() {
        return LocalDate.now(clock);
    }

    /**
     * Adds an item to the library.
     *
     * @throws IllegalArgumentException if this item has already been added
     */
    public void addItem(LendingItem item) {
        Objects.requireNonNull(item, "item");
        if (items.putIfAbsent(item.getId(), item) != null) {
            throw new IllegalArgumentException(
                    "\"" + item.getTitle() + "\" has already been added to this library");
        }
    }

    /**
     * Adds a member to the library.
     *
     * @throws IllegalArgumentException if this member has already been added
     */
    public void addMember(Member member) {
        Objects.requireNonNull(member, "member");
        if (members.putIfAbsent(member.getId(), member) != null) {
            throw new IllegalArgumentException(member.getName() + " is already a member of this library");
        }
    }

    /**
     * Lends an item to a member, starting today. The item works out the
     * loan period for that kind of member.
     *
     * Every check is made before anything is recorded, so a refused loan
     * leaves the library exactly as it was.
     *
     * @throws MemberNotInLibraryException if the member was never added to this library
     * @throws ItemNotInLibraryException if the item was never added to this library
     * @throws ItemAlreadyOnLoanException if the item is already on loan
     * @throws IllegalStateException if the item's kind gives a loan period of less than one day
     */
    public Loan borrow(Member member, LendingItem item) {
        Objects.requireNonNull(member, "member");
        Objects.requireNonNull(item, "item");

        if (members.get(member.getId()) != member) {
            throw new MemberNotInLibraryException(member);
        }
        if (items.get(item.getId()) != item) {
            throw new ItemNotInLibraryException(item);
        }
        Loan existing = activeLoans.get(item);
        if (existing != null) {
            throw new ItemAlreadyOnLoanException(item, existing.getMember());
        }
        int loanDays = item.loanDaysFor(member.getKind());
        if (loanDays < 1) {
            throw new IllegalStateException(item.typeLabel() + " items must be lent for at least 1 day, but \""
                    + item.getTitle() + "\" gives " + loanDays + " for a " + member.getKind() + " member");
        }

        Loan loan = new Loan(nextLoanId++, member, item, loanDays, today());
        activeLoans.put(item, loan);
        loanHistory.add(loan);
        member.recordBorrowed(item);
        return loan;
    }

    /**
     * Takes an item back, closing its active loan.
     *
     * @throws ItemNotOnLoanException if the item is not on loan
     */
    public void returnItem(LendingItem item) {
        Objects.requireNonNull(item, "item");
        Loan loan = activeLoans.remove(item);
        if (loan == null) {
            throw new ItemNotOnLoanException(item);
        }
        loan.markReturned();
    }

    /** Whether the item is out, worked out from the active loans and stored nowhere else. */
    public ItemStatus statusOf(LendingItem item) {
        return activeLoans.containsKey(item) ? ItemStatus.ON_LOAN : ItemStatus.AVAILABLE;
    }

    /** The member who has the item now, if it is on loan. */
    public Optional<Member> currentBorrowerOf(LendingItem item) {
        return Optional.ofNullable(activeLoans.get(item)).map(Loan::getMember);
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

    /** The loans that have not been returned, with the earliest due date first. */
    public List<Loan> getActiveLoansByDueDate() {
        return activeLoans.values().stream().sorted(SOONEST_DUE_FIRST).toList();
    }
}
