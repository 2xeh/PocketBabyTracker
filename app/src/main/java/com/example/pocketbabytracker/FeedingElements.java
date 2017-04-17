package com.example.pocketbabytracker;

public class FeedingElements {
    //Strings start_time, end_time, baby_name, bottle,
    // int bottle_qty, left, right, sns;
    private long startTime, endTime;
    private String babyName, bottle;
    private int bottleQty, left, right;
    private boolean sns;

    // construct without all properties
    public FeedingElements(long startTime, String babyName) {
        this.startTime = startTime;
        this.babyName = babyName;
    }

    // fully qualified constructor
    public FeedingElements(long startTime, long endTime, String babyName, String bottle, int bottleQty, int left, int right, boolean sns) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.babyName = babyName;
        this.bottle = bottle;
        this.bottleQty = bottleQty;
        this.left = left;
        this.right = right;
        this.sns = sns;
    }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime;}
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime;}
    public String getBabyName() { return babyName; }
    public void setBabyName(String babyName) { this.babyName = babyName;}
    public String getBottle() { return bottle; }
    public void setBottle(String bottle) { this.bottle = bottle;}
    public int getBottleQty() { return bottleQty; }
    public void setBottleQty(int bottleQty) { this.bottleQty = bottleQty;}
    public int getLeft() { return left; }
    public void setLeft(int left) { this.left = left;}
    public int getRight() { return right; }
    public void setRight(int right) { this.right = right;}
    public boolean getSns() { return sns; }
    public void setSns(boolean sns) { this.sns = sns;}
}
