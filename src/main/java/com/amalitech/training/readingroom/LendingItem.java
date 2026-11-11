package com.amalitech.training.readingroom;

class LendingItem {
    public String title;
    public ItemType type;
    public ItemStatus status;
    public String currentBorrower;
    public int id;
    static int counter = 1;

    public LendingItem(String title, ItemType type) {
        this.id = counter;
        counter++;
        this.title = title;
        this.type = type;
        this.status = ItemStatus.AVAILABLE;
    }
}
