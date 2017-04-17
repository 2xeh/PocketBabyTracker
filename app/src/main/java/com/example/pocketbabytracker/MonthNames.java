package com.example.pocketbabytracker;

// An enum to handle names of Months
public enum MonthNames {
    JANUARY("January", 0),
    FEBRUARY("February", 1),
    MARCH("March", 2),
    APRIL("April", 3),
    MAY("May", 4),
    JUNE("June", 5),
    JULY("July", 6),
    AUGUST("August", 7),
    SEPTEMBER("September", 8),
    OCTOBER("October", 9),
    NOVEMBER("November", 10),
    DECEMBER("December", 11)
    ;

    private String desc;
    private int id;

    private MonthNames(String desc, int id) {
        this.desc = desc;
        this.id = id;
    }

    public int getId() { return id; }
    public String getDesc() { return desc; }

    public static String getName(int id){
        String selectedMonth = JANUARY.getDesc();

        for (MonthNames month : MonthNames.values()) {
            if (month.getId() == id) {
                selectedMonth = month.getDesc();
            }
        }
        return selectedMonth;
    }

    @Override
    public String toString() {
        return desc;
    }
}