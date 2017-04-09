package com.example.pocketbabytracker;

import java.util.Date;

// A java bean wrapper. Represents the data we are getting out of the query
public class EventObjects {
    private int id;
    private String message;
    private Date date;
    private Date end;

    // construct without int id
    public EventObjects(String message, Date date, Date end) {
        this.message = message;
        this.date = date;
        this.end = end;
    }

    // fully qualified constructor
    public EventObjects(int id, String message, Date date, Date end) {
        this.date = date;
        this.message = message;
        this.id = id;
        this.end = end;
    }

    public Date getEnd(){ return end; }
    public int getId() { return id; }
    public String getMessage() { return message; }
    public Date getDate() { return date; }

}