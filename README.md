# The Reading Room: lending tracker

A small program The Reading Room community library uses to track what is
borrowed. This repository holds the rebuilt version of the original
single-file tracker.

- [DIAGNOSIS.md](DIAGNOSIS.md): what was wrong with the original and the
  structure planned before any code changed.
- This file: how to run it, and the design notes for the rebuild.

## Running it

You need JDK 21 and Maven.

```bash
mvn compile
mvn exec:java -Dexec.mainClass=LendingTracker
```

Or directly with the JDK:

```bash
javac -d target/classes -sourcepath src/main/java src/main/java/LendingTracker.java
java -cp target/classes LendingTracker
```

To run the tests:

```bash
mvn test
```

To add the list of due dates after the usual report, optionally as of a
given day:

```bash
mvn exec:java -Dexec.mainClass=LendingTracker -Dexec.args="--due-dates"
mvn exec:java -Dexec.mainClass=LendingTracker -Dexec.args="--due-dates 2026-12-25"
```

## How the code is organised

```text
src/main/java/
├── LendingTracker.java    starts the program, where the original file was
├── domain/                the lending rules and records
│     Library              the items, members and loans; the only place a loan is made or closed
│     Loan, LoanStatus     one loan and whether it is active or returned
│     Member, MemberKind   a member, and standard or student
│     LendingItem          what every lendable item has in common
│     Book, AudioKit       one class per kind of item, each with its own loan rule
│     ItemStatus           available or on loan
├── exception/             the ways the library refuses a request
│     LendingException              the parent of all of them
│     ItemAlreadyOnLoanException    the item is out with someone else
│     ItemNotInLibraryException     the item was never added to the library
│     MemberNotInLibraryException   the person was never added as a member
│     ItemNotOnLoanException        a return for an item that is not out
└── report/                console output
```

## Design notes

### The rule that must never be broken

An item can only be on one loan at a time. Three things enforce it together:

- **One record of who has what.** `Library` keeps the active loans in a map
  with the item as the key, so an item has at most one. An item no longer
  stores its own status or borrower; both are worked out from that map, so
  they cannot disagree with it. The original recorded "on loan" in two places
  and compared neither.
- **A check before anything is recorded.** `Library.borrow` looks the item up
  first. If it is already on loan it throws `ItemAlreadyOnLoanException` and
  records nothing: no loan, no history entry, no loan number used.
- **No way round the check.** Every field is private. Creating a loan,
  marking it returned and adding to a member's history can only be done from
  inside the `domain` folder, and `Library` is the only class there that does
  them. Everything `Library` hands out is read-only.

### Choices, and what I chose them over

| Decision | Chosen | Instead of | Why |
| --- | --- | --- | --- |
| Kinds of item | A class per kind (`Book`, `AudioKit`) extending `LendingItem` | An enum of types with an `if` or `switch` in `Library` | A new kind is one new file. With an enum, every place that branches on the type has to be found and edited, which is how the original broke. |
| `LendingItem` | An abstract class | An interface | Every item needs the same id and title handling. An abstract class holds it once; an interface would make each new kind repeat it. |
| Loan period | Base days on the item times a multiplier on the member kind | The four numbers written out per type | "Students get double" is written once. A kind of item with different rules replaces the calculation in its own class. |
| Whether an item is on loan | Worked out from the active loans | A status field on the item | A stored copy can drift from the loans. This was the root cause of the double loan. |
| Finding an item's loan | A map keyed by item | Scanning a list, or a heap | The rule needs "does this item have a loan?" answered directly. A heap answers "which is smallest?", which is not the question. |
| Refusing a loan | An unchecked exception | A checked exception, or returning `null` or `false` | A return value can be ignored, which is how the original failed silently. A checked exception would force a `try` around every call, including ones that cannot fail. |
| Printing | A separate `ReportPrinter` | Printing inside `Library` | The rules can be tested without reading the console, and changing a message cannot change who has what. |
| Folders | By concern: `domain`, `exception`, `report` | One package, or a folder per kind of class | It is clear where a change belongs. `Library`, `Loan` and `Member` stay together because the protection above depends on it. |
| Today's date | A clock given to `Library` | Reading the system date inside the code | Tests can fix the date, so they do not depend on the day they run. |

Two patterns I considered and left out, because they would add structure
without solving a problem this program has:

- **A use-case layer** (`BorrowItem`, `ReturnItem` classes). `Library.borrow`
  and `Library.returnItem` already are the use cases. I would add the layer
  when there is a second way into the program, such as a menu or a web page.
- **Modules with interfaces between them.** The program has one area, lending.
  The same idea is applied at folder level instead.

### Adding e-loans

E-loans are the change the library says is coming. In this structure it is one
new file in `domain`:

```java
package domain;

public final class ELoan extends LendingItem {

    public ELoan(String title) {
        super(title);
    }

    @Override
    public String typeLabel() {
        return "E_LOAN";
    }

    @Override
    protected int baseLoanDays() {
        return 21; // whatever the library decides
    }
}
```

Nothing else is edited. `Library`, `Book`, `AudioKit`, `Loan` and the report
work with it as they are, because `Library` only asks an item for its loan
period and never checks what kind it is. If e-loans do not follow "students
get double", `ELoan` replaces `loanDaysFor` with its own rule.

What the original would have forced:

- a third branch in the `if / else if` chain in `borrow`, inside the method
  books and audio kits depend on;
- a third copy of the lines that set the status, set the borrower and print;
- the text `"E_LOAN"` typed identically everywhere it is used, with nothing
  to catch a typo;
- and if any part was missed, a 0-day loan recorded with no error.

One limit to be honest about: the brief only says e-loans have "their own
borrowing rules". If that means a digital item can be lent to several members
at once, `Library`'s one-loan check would need to ask the item whether it
allows that. That is one added question in `Library`, not a rewrite.

### Behaviour: what is the same and what changed

Every feature of the original still exists:

| Original | Now |
| --- | --- |
| `lib.addItem`, `lib.addMember` | Same calls on `Library`; adding the same one twice is rejected |
| `lib.borrow(member, item)` | Same call; returns the loan, and refuses an invalid one |
| `lib.returnItem(item)` | Same call; refuses an item that is not on loan |
| `lib.getLoans()` | Same call; the list is now read-only |
| `lib.printReport()` | `ReportPrinter.printEndOfDay(library)` |
| `member.history` | `member.getHistory()`, read-only |
| Item `status` and `currentBorrower` | `library.statusOf(item)` and `library.currentBorrowerOf(item)` |
| Started as `LendingTracker` | Still started as `LendingTracker` |

The evidence:

- `OriginalAfternoonTest` runs the same afternoon as the original program,
  step by step, and checks the result of each step: the same loan numbers and
  loan periods, the same return, and the same end-of-day state, except that
  the second loan of the novel is refused.
- `ReportPrinterTest` checks each message and the end-of-day report against
  the original's wording and layout, one message per test, so a change to one
  message fails only that test.
- The original program's output was saved in commit `c6fbd5f`, before any
  code changed. Through pull requests 1 to 4 a test compared the program with
  it and passed every time; the one intended change arrived on its own in
  pull request 5. That whole-output comparison was later replaced by the
  targeted tests above, which say which rule broke instead of only that the
  text differs.

What the program prints now differs from the original in three lines:

```diff
-Lent "Things Fall Apart" to Ama Owusu for 28 days
+Cannot lend "Things Fall Apart" to Ama Owusu: already on loan to Kofi Mensah

-  "Things Fall Apart" (BOOK): ON_LOAN - with Ama Owusu
+  "Things Fall Apart" (BOOK): ON_LOAN - with Kofi Mensah

-  Loan #3: "Things Fall Apart" -> Ama Owusu
```

### Other requests the library now refuses

The brief asks for one change in behaviour, and what the program prints
changes in that one way only. The diagnosis found other requests the original
accepted that leave the records wrong (sections 2.1 and 2.6). The program's
own run never makes any of them, so the output is unaffected, but leaving
them open would have left other routes to bad records. Each is now refused
before anything is recorded, and each has a test:

| Request | Original | Now |
| --- | --- | --- |
| Lend an item that was never added to the library | Loan recorded for an item the library does not list | `ItemNotInLibraryException` |
| Lend to someone who was never added as a member | Loan recorded | `MemberNotInLibraryException` |
| Return an item that is not on loan | "Returned" printed, nothing checked | `ItemNotOnLoanException` |
| Add the same item or member twice | Listed twice | `IllegalArgumentException` |
| A missing or blank title or name | Accepted and printed as "null" or blank | Rejected when the item, member or library is created |
| An item kind whose loan period comes to less than one day | Could not be written; an unknown type got a silent 0-day loan | `IllegalStateException` naming the item kind |

The four lending refusals share a parent, `LendingException`, so a caller can
handle any refusal in one place. The last three rows are mistakes in the
calling code, not requests from a member, so they use Java's standard
exceptions.

The last row protects the change that is coming: a volunteer writing the
e-loan class who mistypes the loan period finds out the first time that item
is lent, with a message that names the item kind.

Two more differences follow from the structure itself:

- Returning one copy of a title no longer closes the loan for another copy
  with the same title, because a loan points at the actual item.
- Loan numbers start at 1 in each library, not once for the whole program.

### The improvement beyond the brief: due dates

The original stored how many days a loan was for but not when it started, so
the loan periods could not be used: the tracker could not say when anything
was due or what was late. Now:

- each loan records the date it was made, and its due date is worked out from
  that date and the loan period;
- `--due-dates` adds a list of the active loans, earliest due first, with
  overdue ones marked;
- without the option the program prints exactly what it did before.

For the library this turns the tracker from a record of who has what into
something that tells the volunteers which items to chase. The list is sorted
when it is printed. A priority queue was considered and not used: at this
library's size sorting is simpler and just as fast.

The other thing left for the next person is the test suite: 46 tests that run
in a few seconds with `mvn test` and on every pull request. The original had
none, which is why volunteers were told not to touch the code.

### What changed from the plan in DIAGNOSIS.md

- **Folders by concern.** The plan did not say how the classes would be
  grouped. They started in one package and were regrouped into `domain`,
  `exception` and `report` once the classes had settled.
- **The active-loans map arrived with the fix, not before it.** The earlier
  steps had to keep reproducing the double loan to match the original output,
  and a map that holds one loan per item cannot do that.
- **`ReportPrinter` prints the "Lent" and "Returned" lines too,** not only the
  end-of-day report, so no printing is left in `Library`.
- **The due-dates list shows every active loan, not only overdue ones,** so
  the volunteers can also see what is coming due.
- **More is refused than planned.** The plan refused only the double loan and
  left the other invalid requests as they were. They are now refused too, as
  listed above, because each one was another way for the records to go wrong.
- **The item's status became a calculated value.** `ItemStatus` still exists,
  but as the answer `Library` gives, not as a field on the item.

### Known limits

- Item and member ids come from a private counter in each class. Nothing
  outside can change it, but ids are unique across the whole program, not per
  library.
- Nothing is saved between runs, as in the original.
- `Library.findItem` and `Library.findMember` are not used by the scripted run
  in `main`. They are there, with tests, because looking things up by id is
  the first thing a menu or a screen would need.
