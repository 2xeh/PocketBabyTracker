package com.example.pocketbabytracker;

import java.util.Date;

public class BabyElements {

    private String babyName;
    private String gender;
    private Date birthday;

    // construct without int id
    public BabyElements(String babyName, String gender, Date birthday) {
        this.babyName = babyName;
        this.gender = gender;
        this.birthday = birthday;
    }

    public String toString(){
        return "Name: " + babyName + ", Gender: " + gender + ", Birthday: " + birthday;
    }

    public String getBabyName() { return babyName; }
    public void setBabyName(String babyName) { this.babyName = babyName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Date getBirthday () { return birthday; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
}
