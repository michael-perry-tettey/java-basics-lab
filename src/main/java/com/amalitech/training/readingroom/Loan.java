package com.amalitech.training.readingroom;

class Loan {
    public int id;
    public String memberName;
    public String itemTitle;
    public int loanDays;
    public LoanStatus status;
    static int counter = 1;

    public Loan(String memberName, String itemTitle, int loanDays) {
        this.id = counter;
        counter++;
        this.memberName = memberName;
        this.itemTitle = itemTitle;
        this.loanDays = loanDays;
        this.status = LoanStatus.ACTIVE;
    }
}
