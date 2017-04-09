package com.example.pocketbabytracker;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Runs the query and gets the data as a list of EventObjects
public class DatabaseQuery extends DatabaseObject{

    private static final String TAG = Database.class.getSimpleName();

    // Table name and all of it's column names
    private final String BABIES_TABLE = "Babies";
    private final String BABIES_COL_BABY_NAME = "baby_name";
    private final String BABIES_COL_BIRTHDAY = "birthday";
    private final String BABIES_COL_GENDER = "gender";

    // Table name and all of it's column names
    private final String FEEDINGS_TABLE = "Feedings";
    private final String FEEDINGS_COL_START_TIME = "start_time";
    private final String FEEDINGS_COL_END_TIME = "end_time";
    private final String FEEDINGS_COL_BABY_NAME = "baby_name";
    private final String FEEDINGS_COL_BOTTLE = "bottle";
    private final String FEEDINGS_COL_BOTTLE_QTY = "bottle_qty";
    private final String FEEDINGS_COL_LEFT = "left";
    private final String FEEDINGS_COL_RIGTH = "right";
    private final String FEEDINGS_COL_SNS = "sns";

    public DatabaseQuery(Context context) {
        super(context);
    }


    // Let's persist a baby
    public boolean setNewBaby(BabyElements newBaby){
        boolean result = false;

        // Query to run, and get the cursor
        String insertStatement = "INSERT INTO " + BABIES_TABLE + " (" + BABIES_COL_BABY_NAME + ", " + BABIES_COL_GENDER + ", " + BABIES_COL_BIRTHDAY + ") " +
                "VALUES ('" + newBaby.getBabyName() + "','" + newBaby.getGender() + "','" + newBaby.getBirthday() + "');";

        // run the query
        try {
            this.getDbConnection().execSQL(insertStatement);
            result = true;

        } catch (SQLException e) {
            result = false;
            Log.d("Andrea", "SQLException inserting newBaby into database" + e.getMessage());
        }

        return result;
    }

    // let's get all of the babies out of the database
    public List<BabyElements> getAllBabies(){

        List<BabyElements> babyElements = new ArrayList<BabyElements>();

        // Query to run, and get the cursor
        String query = "SELECT * FROM " + BABIES_TABLE;
        Cursor cursor = this.getDbConnection().rawQuery(query, null);


        // we have results by way of a cursor. Iterate through them
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(0);

                // get the data
                String babyName = cursor.getString(cursor.getColumnIndexOrThrow(BABIES_COL_BABY_NAME));
                String gender = cursor.getString(cursor.getColumnIndexOrThrow(BABIES_COL_GENDER));
                String birthdayString = cursor.getString(cursor.getColumnIndexOrThrow(BABIES_COL_BIRTHDAY));

                //Date birthday = convertStringToDate(birthdayString);

                // I don't think we need to filter the results here...
                babyElements.add(new BabyElements(babyName, gender, birthdayString));

            }while (cursor.moveToNext());
        }

        // don't forget to close the cursor before returning the data
        cursor.close();

        return babyElements;
    }

    // getting the events out of the database
    public List<EventObjects> getAllFutureEvents(Date mDate){

        Calendar calDate = Calendar.getInstance();
        Calendar dDate = Calendar.getInstance();

        // set calDate with incoming param
        calDate.setTime(mDate);

        // and get the values out of that date
        int calDay = calDate.get(Calendar.DAY_OF_MONTH);
        int calMonth = calDate.get(Calendar.MONTH) + 1;
        int calYear = calDate.get(Calendar.YEAR);

        List<EventObjects> events = new ArrayList<>();
        String query = "SELECT * FROM " + BABIES_TABLE;
        Cursor cursor = this.getDbConnection().rawQuery(query, null);

        // we have results by way of a cursor. Iterate through them
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(0);

                // get the data
                String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("reminder"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end"));

                Date reminderDate = convertStringToDate(startDate);
                Date end = convertStringToDate(endDate);
                dDate.setTime(reminderDate);

                int dDay = dDate.get(Calendar.DAY_OF_MONTH);
                int dMonth = dDate.get(Calendar.MONTH) + 1;
                int dYear = dDate.get(Calendar.YEAR);

                if(calDay == dDay && calMonth == dMonth && calYear == dYear){
                    events.add(new EventObjects(id, message, reminderDate, end));
                }

            }while (cursor.moveToNext());
        }

        // don't forget to close the cursor before returning the data
        cursor.close();
        return events;
    }

    // helper method for date manipulation
    private Date convertStringToDate(String dateInString){
        DateFormat format = new SimpleDateFormat("d-MM-yyyy HH:mm", Locale.ENGLISH);
        Date date = null;
        try {
            date = format.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
}