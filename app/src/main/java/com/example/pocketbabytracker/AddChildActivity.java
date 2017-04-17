package com.example.pocketbabytracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddChildActivity extends AppCompatActivity {

    private DatabaseQuery databaseQuery;

    EditText etBabyName;
    Spinner spBiologicalSex;
    DatePicker dpBirthDate;
    TimePicker tpBirthTime;
    private SharedPreferences sharedPreferences;
    ListView lvAddChildControls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        // Let's get all of the control references
        etBabyName = (EditText) findViewById(R.id.etBabyName);
        spBiologicalSex = (Spinner) findViewById(R.id.spBioSex);
        dpBirthDate = (DatePicker) findViewById(R.id.dpBirthDate);
        tpBirthTime = (TimePicker) findViewById(R.id.tpBirthTime);

        // set the databaseQuery
        databaseQuery = new DatabaseQuery(this);

        // get the shared preferences
        sharedPreferences = this.getSharedPreferences("pocketBaby", this.MODE_PRIVATE);

        // get the list view to populate
        lvAddChildControls = (ListView) findViewById(R.id.lvAddChildControls);


        // CONTROLS LIST
        // Let's fill that list view
        final ArrayList<ControlsMenuOptions> menuItems = new ArrayList<ControlsMenuOptions>();
        menuItems.add(new ControlsMenuOptions("Add Child", 1));
        menuItems.add(new ControlsMenuOptions("Back", 2));


        // Initialize the adapter
        ControlsMenuOptionsAdapter adapter = new ControlsMenuOptionsAdapter(this, R.layout.menu_list, menuItems);
        lvAddChildControls.setAdapter(adapter);

        //have listview respond to selected items
        lvAddChildControls.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Because there are only two items on the list, let's use an if statement
                // If adding more list items in the future, beware to change this over to a switch statement
                if (menuItems.get(i).getMenuIndex() == 1){
                    addBaby();
                }
                else {
                    onBackPressed();
                }
            }
        });

        // and set the Dynamic Height of the two lists
        ListUtils.setDynamicHeight(lvAddChildControls);

    }

    // ListUtils thanks to StackOverflow user Hiren Patel
    // http://stackoverflow.com/questions/17693578/android-how-to-display-2-listviews-in-one-activity-one-after-the-other
    public static class ListUtils {
        public static void setDynamicHeight(ListView mListView) {
            ListAdapter mListAdapter = mListView.getAdapter();
            if (mListAdapter == null) {
                // when adapter is null
                return;
            }
            int height = 0;
            int desiredWidth = MeasureSpec.makeMeasureSpec(mListView.getWidth(), MeasureSpec.UNSPECIFIED);
            for (int i = 0; i < mListAdapter.getCount(); i++) {
                View listItem = mListAdapter.getView(i, null, mListView);
                listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
                height += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = mListView.getLayoutParams();
            params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
            mListView.setLayoutParams(params);
            mListView.requestLayout();
        }
    }

    // class to define properties of menu items
    class ControlsMenuOptions {
        private String menuDescription;
        private int menuIndex;

        public ControlsMenuOptions(String menuDescription, int menuIndex) {
            this.menuDescription = menuDescription;
            this.menuIndex = menuIndex;
        }

        public String getMenuDescription() {return this.menuDescription; }
        public int getMenuIndex() { return this.menuIndex; }
    }

    // adapter for controls list
    private class ControlsMenuOptionsAdapter extends ArrayAdapter<ControlsMenuOptions> {
        private ArrayList<ControlsMenuOptions> items;

        public ControlsMenuOptionsAdapter(Context context, int textViewResourceId, ArrayList<ControlsMenuOptions> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }

        // get the views for each item in the array list of MainActivityMenuOptions
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.menu_list, null);
            }

            ControlsMenuOptions menuItem = items.get(position);

            if (menuItem != null) {
                TextView header = (TextView) view.findViewById(R.id.tvMenuItem);
                header.setText(menuItem.getMenuDescription());
            }
            return view;
        }
    }

    // Method to add baby to the Babies table
    public void addBaby() {
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
        boolean result = databaseQuery.setNewBaby(newBaby);
        if(result){
            // Set this baby to selected
            saveToSharedPreferences(babyName);

            makeToast("Successfully added baby " + babyName );
        }
        else {
            makeToast("Unable to add baby " + babyName);
        }

    }

    private void saveToSharedPreferences(String babyName) {

        // instantiate the SharedPreferences Editor and put in the new values
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("babyName", babyName);

        // commit changes made by the editor
        if (editor.commit()) {
            // update a TextView?
        } else {
            makeToast("Unable to set baby.");
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, BabyInfoActivity.class));
    }

    // A helper method for easy long toasts
    private void makeToast(String message){
        Toast.makeText(AddChildActivity.this, message, Toast.LENGTH_LONG).show();
    }
}

