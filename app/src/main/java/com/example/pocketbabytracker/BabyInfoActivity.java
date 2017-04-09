package com.example.pocketbabytracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.List;

public class BabyInfoActivity extends AppCompatActivity {

    private DatabaseQuery databaseQuery;

    EditText etBabyName, etGender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_info);


        // set the databaseQuery
        databaseQuery = new DatabaseQuery(this);

        displayAllBabies();



    }

    public void addBaby(View view) {
        // get all of the data for the new baby



        //BabyElements newBaby = new BabyElements();



    }

    private void displayAllBabies(){
        List<BabyElements> babies = databaseQuery.getAllBabies();

        for(BabyElements baby : babies){

            // let's do something to display each baby
            // TODO: add toString() method to BabyElements
            Log.d("Andrea", baby.toString());

        }
    }
}
