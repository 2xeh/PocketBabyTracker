package com.example.pocketbabytracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FeedingActivity extends AppCompatActivity {

    private final String TAG = "Andrea-Feeding";
    private static final int MENU_FIRST = Menu.FIRST;

    // controls
    ListView lvFeedingMenu;
    TextView tvSelectedBabyFeeding;
    Chronometer chMasterTimer, chLeftTimer, chRightTimer;

    // flags
    boolean isMasterTimerRunning = false,
            isLeftTimerRunning = false,
            isRightTimerRunning = false;

    // data
    boolean snsUsed = false;
    Calendar startTime;
    Calendar endTime;
    String bottle = "none";
    int bottleQty = 0; // this doubles for snsQty
    int left = 0;
    int right = 0;
    String babyName;
    String pausedSide = "none";

    // database and prefs
    private DatabaseQuery databaseQuery;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feeding);

        Log.d(TAG, "get the shared preferences");
        sharedPreferences = this.getSharedPreferences("pocketBaby", this.MODE_PRIVATE);

        Log.d(TAG, "get the controls");
        tvSelectedBabyFeeding = (TextView) findViewById(R.id.tvSelectedBabyFeeding);
        lvFeedingMenu = (ListView) findViewById(R.id.lvFeedingMenu);
        chMasterTimer = (Chronometer) findViewById(R.id.chMasterTimer);
        chLeftTimer = (Chronometer) findViewById(R.id.chLeftTimer);
        chRightTimer = (Chronometer) findViewById(R.id.chRightTimer);

        Log.d(TAG, "Getting child from preferences and setting on screen");
        babyName = sharedPreferences.getString("babyName", "");
        tvSelectedBabyFeeding.setText("Selected child: " + babyName);

        // set the databaseQuery
        databaseQuery = new DatabaseQuery(this);

        // Let's fill that list view
        final ArrayList<FeedingMenuOptions> menuItems = new ArrayList<FeedingMenuOptions>();

        menuItems.add(new FeedingMenuOptions("Pause", "pause"));
        menuItems.add(new FeedingMenuOptions("Start Left", "left"));
        menuItems.add(new FeedingMenuOptions("Start Right", "right"));
        menuItems.add(new FeedingMenuOptions("Start Bottle", "bottle"));
        menuItems.add(new FeedingMenuOptions("Set SNS Quantity", "sns"));
        menuItems.add(new FeedingMenuOptions("Finish", "finish"));
        menuItems.add(new FeedingMenuOptions("Save and Go Back", "back"));

        // Initialize the adapter
        FeedingMenuOptionsAdapter adapter = new FeedingMenuOptionsAdapter(this, R.layout.menu_list, menuItems);
        lvFeedingMenu.setAdapter(adapter);


        //have listview respond to selected items
        lvFeedingMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Figure out which control the user pressed
                String identifier = menuItems.get(i).getMenuIdentifier();

                // And handle it accordingly
                switch (identifier) {

                    case "pause":

                        // ok, some logic here.....
                        // only left or right should be running. If one is running, pause it, log which one is paused
                        if(isLeftTimerRunning || isRightTimerRunning) {
                            if(isLeftTimerRunning){
                                chLeftTimer.stop();
                                pausedSide = "left";
                            }
                            else {
                                chRightTimer.stop();
                                pausedSide = "right";
                            }

                            // TODO: should the master timer be handled?
                            // reason would be bottle feeding ... but start time and end time are being recorded...
                        }

                        // If neither is running, then one has been paused (possibly)
                        if(!isLeftTimerRunning && !isRightTimerRunning) {
                            if(pausedSide.equals("left")){
                                pausedSide = "none";
                                chLeftTimer.start();
                                isLeftTimerRunning = true;
                            }
                            else if(pausedSide.equals("right")){
                                pausedSide = "none";
                                chRightTimer.start();
                                isRightTimerRunning = true;
                            }
                            else {
                                pausedSide = "none";
                            }

                        }
                        break;

                    case "left":
                        // start appropriate timers that are running
                        if(startTime != null) {
                            startTime = Calendar.getInstance();
                            // time logged. Need to start chronometer
                            chMasterTimer.start();
                            isMasterTimerRunning = true;
                        }

                        if(!isLeftTimerRunning){
                            chLeftTimer.start();
                            isLeftTimerRunning = true;
                        }

                        // if right timer is running, pause it
                        if(isRightTimerRunning){
                            chRightTimer.stop();
                            isRightTimerRunning = false;
                        }

                        // what happens if the feeding is bottle? (bottle != none && sns == false)
                        break;

                    case "right":
                        // start appropriate timers that are running
                        if(startTime != null) {
                            startTime = Calendar.getInstance();
                            // time logged. Need to start chronometer
                            chMasterTimer.start();
                            isMasterTimerRunning = true;
                        }

                        if(!isRightTimerRunning){
                            chRightTimer.start();
                            isRightTimerRunning = true;
                        }

                        // if right timer is running, pause it
                        if(isLeftTimerRunning){
                            chLeftTimer.stop();
                            isLeftTimerRunning = false;
                        }

                        // what happens if the feeding is bottle? (bottle != none && sns == false)
                        break;

                    case "bottle":
                        // start appropriate timers that are running
                        if(startTime != null) {
                            startTime = Calendar.getInstance();
                            // time logged. Need to start chronometer
                            chMasterTimer.start();
                            isMasterTimerRunning = true;
                        }

                        // If breastfeeding timers are running, pause them.
                        if(isRightTimerRunning){
                            chRightTimer.stop();
                            isRightTimerRunning = false;
                        }

                        if(isLeftTimerRunning){
                            chLeftTimer.stop();
                            isLeftTimerRunning = false;
                        }

                        // TODO: need to start a dialog to log the bottle qty and type

                        break;

                    case "sns":
                        // TODO: add to a text view that this is sns
                        // TODO: if time, improve and make this list view item change color or something
                        snsUsed = true;

                        // TODO: need to start a dialog to log the bottle qty and type
                        break;

                    case "finish":
                        // stop all the timers. log the data
                        finishFeeding();

                        // this should behave as back without navigating back
                        break;

                    case "summary":
                        // take user to summary screen
                        // don't pause the timers
                        break;

                    case "back":
                        // Note: onBackPressed() will save the data, persist it, and navigate to previous screen
                        onBackPressed();
                        break;

                    default:
                        break;
                }

                // TODO: going to have to process what the value is or something to that effect
                // TODO: could write something like menuItems.get(i).getMenuIdentifier();
                // use int i to get the item out of the list?
//                Intent intent = menuItems.get(i).getMenuIntent();
//                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {

        // save the data and finish the feeding
        finishFeeding();

        super.onBackPressed();
    }

    private void finishFeeding() {
        // stop appropriate timers that are running
        if(endTime != null && isMasterTimerRunning) {
            // log end time
            endTime = Calendar.getInstance();
            // stop the chronometers
            chMasterTimer.stop();
            isMasterTimerRunning = false;
        }

        // If breastfeeding timers are running, pause them.
        if(isRightTimerRunning){
            chRightTimer.stop();
            isRightTimerRunning = false;
        }

        if(isLeftTimerRunning){
            chLeftTimer.stop();
            isLeftTimerRunning = false;
        }

        // now we need to wrap up the data.
        left = (int)(SystemClock.elapsedRealtime() - chLeftTimer.getBase());
        right = (int)(SystemClock.elapsedRealtime() - chRightTimer.getBase());

        // ok, we should have all of our data now. Let's set it up for persisting to database.

        // TODO: persist the data
        // TODO: call form reset method (need to code)

    }

    // An ArrayAdapter Blogs to use in the ListView
    private class FeedingMenuOptionsAdapter extends ArrayAdapter<FeedingMenuOptions> {
        private ArrayList<FeedingMenuOptions> items;

        public FeedingMenuOptionsAdapter(Context context, int textViewResourceId, ArrayList<FeedingMenuOptions> items) {
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

            FeedingMenuOptions menuItem = items.get(position);

            if (menuItem != null) {
                TextView header = (TextView) view.findViewById(R.id.tvMenuItem);
                header.setText(menuItem.getMenuDescription());

                // I feel like I should be doing something with intent here???
            }
            return view;
        }
    } // end of MainActivityMenuOptionsAdapter nested class


    // class to define properties of menu items
    class FeedingMenuOptions {
        private String menuDescription;
        private String menuIdentifier;

        public FeedingMenuOptions(String menuDescription, String menuIdentifier) {
            this.menuDescription = menuDescription;
            this.menuIdentifier = menuIdentifier;
        }

        public String getMenuDescription() {return this.menuDescription; }
        public void setMenuDescription(String menuDescription) { this.menuDescription = menuDescription; }
        public String getMenuIdentifier() { return this.menuIdentifier; }
        public void setMenuIdentifier(String menuIdentifier) { this.menuIdentifier = menuIdentifier; }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // let's try to get access to the menu
        int menuCounter = MENU_FIRST;

        List<BabyElements> babies = databaseQuery.getAllBabies();

        // add each baby to the menu
        for(BabyElements baby : babies){
            menu.add(0, menuCounter++, Menu.NONE, baby.getBabyName());
        }

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        babyName = item.getTitle().toString();
        tvSelectedBabyFeeding.setText("Selected child: " + babyName);


        // instantiate the SharedPreferences Editor and put in the new values
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("babyName", babyName);

        // commit changes made by the editor
        if (editor.commit()) {
            // update a TextView?
        } else {
            makeToast("Unable to set baby.");
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeToast(String message){
        Toast.makeText(FeedingActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
