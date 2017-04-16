package com.example.pocketbabytracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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

    // Attributes for master timer (chronometer) -- we can only run one
    boolean isMasterTimerRunning = false;
    Chronometer chMasterTimer;

    // Attributes for Left Timer
    boolean isLeftTimerRunning = false;
    TextView tvLeftTimer;
    Handler leftHandler;
    long leftMillisTime, leftStartTime, leftTimeBuff, leftUpdateTime = 0L ;
    int leftSeconds, leftMinutes, leftMillis;

    // Attributes for Right Timer
    boolean isRightTimerRunning = false;
    TextView tvRightTimer;
    Handler rightHandler;
    long rightMillisTime, rightStartTime, rightTimeBuff, rightUpdateTime = 0L ;
    int rightSeconds, rightMinutes, rightMillis;

    // Variables to hold data captured
    boolean snsUsed = false;
    Calendar startTime;
    Calendar endTime;
    String bottle = "none";
    int bottleQty = 0; // if sns was used, this attribute captures the qty associated with that
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
        tvLeftTimer = (TextView) findViewById(R.id.tvLeftTimer);
        tvRightTimer = (TextView) findViewById(R.id.tvRightTimer);

        Log.d(TAG, "create handlers for left and right timers");
        leftHandler = new Handler();
        rightHandler = new Handler();


        // AA learning lesson: if you try to run multiple Chronometers on one screen, they
        // interfere with one another. So instead of multiple Chronometers, I created some custom timers.

        Log.d(TAG, "Getting child from preferences and setting on screen");
        babyName = sharedPreferences.getString("babyName", "");
        tvSelectedBabyFeeding.setText("Selected child: " + babyName);

        Log.d(TAG, "set the databaseQuery");
        databaseQuery = new DatabaseQuery(this);

        Log.d(TAG, "fill the list view for controls");
        final ArrayList<FeedingMenuOptions> menuItems = new ArrayList<FeedingMenuOptions>();
        menuItems.add(new FeedingMenuOptions("Pause", "btPauseLeft"));
        menuItems.add(new FeedingMenuOptions("Start Left", "left"));
        menuItems.add(new FeedingMenuOptions("Start Right", "right"));
        menuItems.add(new FeedingMenuOptions("Start Bottle", "bottle"));
        menuItems.add(new FeedingMenuOptions("Set SNS Quantity", "sns"));
        menuItems.add(new FeedingMenuOptions("Finish", "finish"));
        menuItems.add(new FeedingMenuOptions("Save and Go Back", "back"));

        Log.d(TAG, "Initialize the list view adapter - for controls");
        FeedingMenuOptionsAdapter adapter = new FeedingMenuOptionsAdapter(this, R.layout.menu_list, menuItems);
        lvFeedingMenu.setAdapter(adapter);


        // have listview respond to selected items
        lvFeedingMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                // Figure out which control the user pressed
                String identifier = menuItems.get(i).getMenuIdentifier();

                // And handle it accordingly
                switch (identifier) {

                    case "btPauseLeft":

                        if(startTime != null) {
                            // If neither is running, then one has been paused (possibly)
                            if (!pausedSide.equals("none")) {
                                if (pausedSide.equals("left")) {
                                    // start the left timer again
                                    startLeft();

                                    // set the flags
                                    pausedSide = "none";
                                    isLeftTimerRunning = true;
                                } else if (pausedSide.equals("right")) {
                                    // start the right timer again
                                    startRight();

                                    // set the flags
                                    pausedSide = "none";
                                    isRightTimerRunning = true;
                                }
                            }

                            // If left or right timer is running, pause it (assumed only one running)
                            else if (isLeftTimerRunning) {
                                // Pause the left timer
                                pauseLeft();

                                // set the flags
                                pausedSide = "left";
                                isLeftTimerRunning = false;
                            }

                            else if (isRightTimerRunning) {
                                // Pause the right timer
                                pauseRight();

                                // set the flags
                                pausedSide = "right";
                                isRightTimerRunning = false;
                            }

                            // TODO: should the master timer be handled?
                            // reason would be bottle feeding ... but btStartLeft time and end time are being recorded...


                        }

                        // if the master timer has not been started, do nothing
                        break;

                    case "left":

                        // if left is not running, start it
                        if(!isLeftTimerRunning){
                            // start the left timer
                            startLeft();

                            // set the flag
                            isLeftTimerRunning = true;
                        }

                        // if the right is running, pause it
                        if(isRightTimerRunning){
                            // pause the right timer
                            pauseRight();

                            // set the flag
                            isRightTimerRunning = false;
                        }

                        // if the master timer has not been started, start it
                        if(startTime == null) {
                            // get instance of current time, start chronometer
                            startTime = Calendar.getInstance();
                            chMasterTimer.start();

                            // set the flag
                            isMasterTimerRunning = true;
                        }

                        // AA NOTE: if bottle feeding, we aren't really worried about timing it
                        // what happens if the feeding is bottle? (bottle != none && sns == false)
                        break;

                    case "right":
                        // if right is not running, start it
                        if(!isRightTimerRunning){
                            // start the right timer
                            startRight();

                            // set the flag
                            isRightTimerRunning = true;
                        }

                        // if the left is running, pause it
                        if(isLeftTimerRunning){
                            // pause the left timer
                            pauseLeft();

                            // set the flag
                            isLeftTimerRunning = false;
                        }

                        // if the master timer has not been started, start it
                        if(startTime == null) {
                            // get instance of current time, start chronometer
                            startTime = Calendar.getInstance();
                            chMasterTimer.start();

                            // set the flag
                            isMasterTimerRunning = true;
                        }

                        // AA NOTE: if bottle feeding, we aren't really worried about timing it
                        // what happens if the feeding is bottle? (bottle != none && sns == false)

                        break;

                    case "bottle":
                        // if the master timer has not been started, start it
                        if(startTime == null) {
                            // get instance of current time, start chronometer
                            startTime = Calendar.getInstance();
                            chMasterTimer.start();

                            // set the flag
                            isMasterTimerRunning = true;
                        }

                        // If breastfeeding timers are running, btPauseLeft them.
                        if(isRightTimerRunning){
                            // pause the right timer
                            pauseRight();

                            // set the flag
                            isRightTimerRunning = false;
                        }

                        if(isLeftTimerRunning){
                            // pause the left timer
                            pauseLeft();

                            // set the flag
                            isLeftTimerRunning = false;
                        }

                        // TODO: need to create a dialog to log the bottle qty and type

                        break;

                    case "sns":
                        // TODO: add to a text view that this is sns
                        // TODO: if time, improve and make this list view item change color or something
                        snsUsed = true;

                        // TODO: need to create a dialog to log the bottle qty and type
                        break;

                    case "finish":
                        // stop all the timers. log the data
                        finishFeeding();

                        // this should behave as back without navigating back
                        break;

                    case "summary":
                        // take user to summary screen
                        // don't btPauseLeft the timers
                        break;

                    case "back":
                        // Note: onBackPressed() will save the data, persist it, and navigate to previous screen
                        onBackPressed();
                        break;

                    default:
                        break;
                }

            }
        });
    }

    // HERE ARE THE START/STOP/PAUSE METHODS FOR THE LEFT AND RIGHT TIMERS
    private void startLeft() {
        leftStartTime = SystemClock.uptimeMillis();
        leftHandler.postDelayed(leftRunnable, 0);
        //btResetLeft.setEnabled(false);
    }

    private void pauseLeft() {
        leftTimeBuff += leftMillisTime;
        leftHandler.removeCallbacks(leftRunnable);
        //btResetLeft.setEnabled(true);
    }

    private void resetLeft() {
        leftMillisTime = 0L ;
        leftStartTime = 0L ;
        leftTimeBuff = 0L ;
        leftUpdateTime = 0L ;
        leftSeconds = 0 ;
        leftMinutes = 0 ;
        leftMillis = 0 ;
        tvLeftTimer.setText("00:00:00");
    }

    private void startRight() {
        rightStartTime = SystemClock.uptimeMillis();
        rightHandler.postDelayed(rightRunnable, 0);
        //btResetRight.setEnabled(false);
    }

    private void pauseRight() {
        rightTimeBuff += rightMillisTime;
        rightHandler.removeCallbacks(rightRunnable);
        //btResetRight.setEnabled(true);
    }

    private void resetRight() {
        rightMillisTime = 0L ;
        rightStartTime = 0L ;
        rightTimeBuff = 0L ;
        rightUpdateTime = 0L ;
        rightSeconds = 0 ;
        rightMinutes = 0 ;
        rightMillis = 0 ;
        tvRightTimer.setText("00:00:00");
    }

    public Runnable leftRunnable = new Runnable() {

        public void run() {

            leftMillisTime = SystemClock.uptimeMillis() - leftStartTime;
            leftUpdateTime = leftTimeBuff + leftMillisTime;
            leftSeconds = (int) (leftUpdateTime / 1000);
            leftMinutes = leftSeconds / 60;
            leftSeconds = leftSeconds % 60;
            leftMillis = (int) (leftUpdateTime % 1000);

            tvLeftTimer.setText("" + leftMinutes + ":"
                    + String.format("%02d", leftSeconds) + ":"
                    + String.format("%03d", leftMillis));

            leftHandler.postDelayed(this, 0);
        }
    };

    public Runnable rightRunnable = new Runnable() {

        public void run() {

            rightMillisTime = SystemClock.uptimeMillis() - rightStartTime;
            rightUpdateTime = rightTimeBuff + rightMillisTime;
            rightSeconds = (int) (rightUpdateTime / 1000);
            rightMinutes = rightSeconds / 60;
            rightSeconds = rightSeconds % 60;
            rightMillis = (int) (rightUpdateTime % 1000);

            tvRightTimer.setText("" + rightMinutes + ":"
                    + String.format("%02d", rightSeconds) + ":"
                    + String.format("%03d", rightMillis));

            rightHandler.postDelayed(this, 0);
        }
    };



    // WRAP UP THE FEEDING AND GET THE DATA
    // TODO: persist to the database
    @Override
    public void onBackPressed() {

        // save the data and finish the feeding
        finishFeeding();

        super.onBackPressed();
    }

    private void finishFeeding() {
        // stop appropriate timers that are running
        if(endTime == null && isMasterTimerRunning) {
            // log end time
            endTime = Calendar.getInstance();
            // stop the chronometer
            chMasterTimer.stop();
            isMasterTimerRunning = false;
        }

        // If breastfeeding timers are running, btPauseLeft them.
        if(isRightTimerRunning){
            // todo: stop the right side timer
            isRightTimerRunning = false;
        }

        if(isLeftTimerRunning){
            // todo: stop the right side timer
            isLeftTimerRunning = false;
        }

        // TODO: format time in threads and here. We can come back to this at the end
        // now we need to wrap up the data.
        String leftString = tvLeftTimer.getText().toString();
        String rightString = tvRightTimer.getText().toString();




        // ok, we should have all of our data now. Let's set it up for persisting to database.
        Log.d(TAG, "Baby Name: " + babyName);                       // String
        Log.d(TAG, "Start Time: " + startTime.getTimeInMillis());   // save millis as string
        Log.d(TAG, "End Time: " + endTime.getTimeInMillis());       // save millis as string
        Log.d(TAG, "Elapsed Time (Left): " + leftString);                 // int
        Log.d(TAG, "Elapsed Time (Right): " + rightString);               // int
        Log.d(TAG, "SNS? : " + snsUsed);                            // boolean
        Log.d(TAG, "Bottle Type: " + bottle);                       // string
        Log.d(TAG, "Bottle Qty (mL): " + bottleQty);                // int


        // TODO: persist the data
        // TODO: call form btResetLeft method (need to code)

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

    // Toasting helper method
    private void makeToast(String message){
        Toast.makeText(FeedingActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
