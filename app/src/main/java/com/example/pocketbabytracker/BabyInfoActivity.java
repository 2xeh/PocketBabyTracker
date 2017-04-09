package com.example.pocketbabytracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BabyInfoActivity extends AppCompatActivity {

    private DatabaseQuery databaseQuery;

    EditText etBabyName;
    Spinner spBiologicalSex;
    DatePicker dpBirthDate;
    TimePicker tpBirthTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_info);

        // Let's get all of the control references
        etBabyName = (EditText) findViewById(R.id.etBabyName);
        spBiologicalSex = (Spinner) findViewById(R.id.spBioSex);
        dpBirthDate = (DatePicker) findViewById(R.id.dpBirthDate);
        tpBirthTime = (TimePicker) findViewById(R.id.tpBirthTime);

        // set the databaseQuery
        databaseQuery = new DatabaseQuery(this);

        displayAllBabies();



    }

    public void addBaby(View view) {
        // get all of the data for the new baby

        String babyName = etBabyName.getText().toString();
        String gender = spBiologicalSex.getSelectedItem().toString();
        int birthYear = dpBirthDate.getYear();
        int birthMonth = dpBirthDate.getMonth();
        int birthDay = dpBirthDate.getDayOfMonth();
        int birthHour = tpBirthTime.getCurrentHour();
        int birthMinute = tpBirthTime.getCurrentMinute();

        String birthdayString = birthDay + "-" + (birthMonth + 1) + "-" + birthYear + " " + birthHour + ":" + birthMinute;

        Log.d("Andrea", "babyName: " + babyName);
        Log.d("Andrea", "gender: " + gender);
        Log.d("Andrea", "birthday: " + birthdayString);

        // AA NOTE: originally BabyElements took birthday as type Date
        // Date birthDate = convertStringToDate(birthdayString);

        BabyElements newBaby = new BabyElements(babyName, gender, birthdayString);

        // Ok, we have a BabyElement. Let's persist it
        databaseQuery.setNewBaby(newBaby);

    }

    private void displayAllBabies(){
        List<BabyElements> babies = databaseQuery.getAllBabies();

        for(BabyElements baby : babies){

            // let's do something to display each baby
            // TODO: add toString() method to BabyElements
            Log.d("Andrea", baby.toString());

        }
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
