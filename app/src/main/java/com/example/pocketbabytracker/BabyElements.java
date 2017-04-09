package com.example.pocketbabytracker;

public class BabyElements {

    private String babyName;
    private String gender;
    private String birthday;

    // construct without int id
    public BabyElements(String babyName, String gender, String birthday) {
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
    public String getBirthday () { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }


}
