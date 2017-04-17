package com.example.pocketbabytracker;

// An enum to handle names of days of the week
public enum DaysOfTheWeek {
    SUNDAY("Sunday", 1),
    MONDAY("Monday", 2),
    TUESDAY("Tuesday", 3),
    WEDNESDAY("Wednesday", 4),
    THURSDAY("Thursday", 5),
    FRIDAY("Friday", 6),
    SATURDAY("Saturday", 7)
    ;

    private String desc;
    private int id;

    private DaysOfTheWeek(String desc, int id) {
        this.desc = desc;
        this.id = id;
    }

    public int getId() { return id; }
    public String getDesc() { return desc; }

    public static String getName(int id){
        String selectedDay = SUNDAY.getDesc();

        for (DaysOfTheWeek day : DaysOfTheWeek.values()) {
            if (day.getId() == id) {
                selectedDay = day.getDesc();
            }
        }
        return selectedDay;
    }

    @Override
    public String toString() {
        return desc;
    }
}