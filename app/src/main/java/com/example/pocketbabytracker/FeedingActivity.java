package com.example.pocketbabytracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
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
    TextView tvSelectedBabyFeeding, tvLastFeeding, tvLastSide, tvSnsUsed;
    String lastSide = "none";

    // Attributes for Master Timer
    boolean isMasterTimerRunning = false;
    TextView tvMasterTimer;
    Handler masterHandler;
    long masterMillisTime, masterStartTime, masterTimeBuff, masterUpdateTime = 0L ;
    int masterSeconds, masterMinutes, masterMillis;


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
        tvLastFeeding = (TextView) findViewById(R.id.tvLastFeeding);
        tvLastSide = (TextView) findViewById(R.id.tvLastSide);
        tvSnsUsed = (TextView) findViewById(R.id.tvSnsUsed);
        lvFeedingMenu = (ListView) findViewById(R.id.lvFeedingMenu);
        tvMasterTimer = (TextView) findViewById(R.id.tvMasterTimer);
        tvLeftTimer = (TextView) findViewById(R.id.tvLeftTimer);
        tvRightTimer = (TextView) findViewById(R.id.tvRightTimer);

        Log.d(TAG, "create handlers for master, left, and right timers");
        masterHandler = new Handler();
        leftHandler = new Handler();
        rightHandler = new Handler();

        // AA learning lesson: if you try to run multiple Chronometers on one screen, they
        // interfere with one another. So instead of multiple Chronometers, I created some custom timers.

        Log.d(TAG, "Getting child from preferences and setting on screen");
        babyName = sharedPreferences.getString("babyName", "");
        tvSelectedBabyFeeding.setText("Feeding baby " + babyName);

        // capture last side parent fed on
        lastSide = sharedPreferences.getString("lastSide", "none");
        tvLastSide.setText("Last Side: " + lastSide);

        Log.d(TAG, "set the databaseQuery");
        databaseQuery = new DatabaseQuery(this);

        // Because last feeding depends on which child was fed, need to get last feeding from database
        FeedingElements lastFeeding = databaseQuery.getLastFeeding(babyName);
        if(lastFeeding != null) {
            tvLastFeeding.setText("Last Feeding ended: " + lastFeeding.lastFeedingString());
        } else {
            tvLastFeeding.setText("This is the first feeding to record for baby " + babyName);
        }

        Log.d(TAG, "fill the list view for controls");
        final ArrayList<FeedingMenuOptions> menuItems = new ArrayList<FeedingMenuOptions>();
        menuItems.add(new FeedingMenuOptions("Pause", "btPauseLeft"));
        menuItems.add(new FeedingMenuOptions("Start Left", "left"));
        menuItems.add(new FeedingMenuOptions("Start Right", "right"));
        menuItems.add(new FeedingMenuOptions("Start Bottle", "bottle"));
        menuItems.add(new FeedingMenuOptions("Set SNS Quantity", "sns"));
        menuItems.add(new FeedingMenuOptions("Finish", "finish"));
        menuItems.add(new FeedingMenuOptions("Save and Go Back", "back"));
        menuItems.add(new FeedingMenuOptions("Reset", "cancel"));

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

                            // AA NOTE: choosing not to pause master timer, because it represents start time of session
                            // actual time spent on the breast is recorded separately
                            // where the master timer is capturing the actual amount of time spent on the activity
                            // reason would be bottle feeding ... but btStartLeft time and end time are being recorded...
                        }

                        // if the master timer has not been started, do nothing
                        break;

                    case "left":
                        pausedSide = "none";

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

                            // start the master timer from 0
                            resetMaster();
                            startMaster();

                            // set the flag
                            isMasterTimerRunning = true;
                        }

                        // AA NOTE: if bottle feeding, we aren't really worried about timing it
                        // what happens if the feeding is bottle? (bottle != none && sns == false)
                        break;

                    case "right":
                        pausedSide = "none";

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

                            // start the master timer from 0
                            resetMaster();
                            startMaster();

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

                            // start the mastertimer from 0
                            resetMaster();
                            startMaster();

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

                        // Open dialog for bottle/supplement. Pass in false to indicate SNS was not used
                        openBottleDialog(false);

                        break;

                    case "sns":
                        // Open dialog for bottle/supplement. Pass in true to indicate SNS used
                        openBottleDialog(true);
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

                    case "cancel":
                        resetForm();
                        break;

                    default:
                        break;
                }

            }
        });
    }

    // A dialog to record qty of supplement and type
    private void openBottleDialog(final boolean isSnsFeeding){
        final AlertDialog.Builder bottleDialog = new AlertDialog.Builder(FeedingActivity.this);

        if(isSnsFeeding) {
            bottleDialog.setTitle("Supplement Details");
        } else {
            bottleDialog.setTitle("Bottle Details");
        }

        View bottleView = getLayoutInflater().inflate(R.layout.dialog_bottle, null);

        // define the views inside that layout
        final RadioButton rbBreast= (RadioButton) bottleView.findViewById(R.id.rbBreast);
        final EditText etBottleQty = (EditText) bottleView.findViewById(R.id.etBottleQty);

        bottleDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // get the data out
                snsUsed = isSnsFeeding;
                boolean isBreastmilk = rbBreast.isChecked();
                if(isBreastmilk){
                    bottle = "breast";
                } else {
                    bottle = "formula";
                }

                bottleQty = Integer.parseInt(etBottleQty.getText().toString());
                if(isSnsFeeding){
                    tvSnsUsed.setText("SNS: yes");
                } else {
                    tvSnsUsed.setText("SNS: not used");
                }

                dialog.cancel();
            }
        });
        bottleDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        bottleDialog.setView(bottleView);
        AlertDialog dialog = bottleDialog.create();
        dialog.show();
    }

    // WRAP UP THE FEEDING AND GET THE DATA
    @Override
    public void onBackPressed() {

        // If there is data to save, then save it
        if(startTime != null){
            finishFeeding();
        }

        super.onBackPressed();
    }

    private void finishFeeding() {
        pausedSide = "none";

        // stop appropriate timers that are running
        if(endTime == null && isMasterTimerRunning) {
            // stop the master timer
            endTime = Calendar.getInstance();
            pauseMaster();
            isMasterTimerRunning = false;
        }

        // If breastfeeding timers are running, btPauseLeft them.
        if(isRightTimerRunning){
            // stop the right side timer
            pauseRight();
            isRightTimerRunning = false;
        }

        if(isLeftTimerRunning){
            // stop the right side timer
            pauseLeft();
            isLeftTimerRunning = false;
        }


        // ok, we should have all of our data now. Let's set it up for persisting to database.
        Log.d(TAG, "Baby Name: " + babyName);                       // String
        Log.d(TAG, "Start Time: " + startTime.getTimeInMillis());   // save millis as string
        Log.d(TAG, "End Time: " + endTime.getTimeInMillis());       // save millis as string
        Log.d(TAG, "Elapsed Time (Left): " + leftMillisTime);       // long
        Log.d(TAG, "Elapsed Time (Right): " + rightMillisTime);     // long
        Log.d(TAG, "SNS? : " + snsUsed);                            // boolean
        Log.d(TAG, "Bottle Type: " + bottle);                       // string
        Log.d(TAG, "Bottle Qty (mL): " + bottleQty);                // int


        // Let's put our data into a FeedingElement so we can persist the data
        FeedingElements feedingToSave = new FeedingElements(
                startTime.getTimeInMillis(),
                endTime.getTimeInMillis(),
                babyName,
                bottle,
                bottleQty,
                (int) leftMillisTime,
                (int) rightMillisTime,
                snsUsed
        );
        Log.d(TAG, "created feedingToSave");

        // set this data as the last feeding
        tvLastSide.setText(lastSide);
        tvLastFeeding.setText(feedingToSave.lastFeedingString());

        // save last side fed on to shared preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastSide", lastSide);
        if (!editor.commit()) {
            makeToast("Unable to set last fed side.");
        }

        // persist the data
        boolean feedingSaveResult = databaseQuery.setNewFeeding(feedingToSave);
        Log.d(TAG, "finished trying to persist data");

        if(!feedingSaveResult){
            makeToast("Unable to save Feeding Data to database.");
        }

        resetForm();
    }

    private void resetForm(){
        resetMaster();
        resetLeft();
        resetRight();

        // reset the attributes that hold significant data
        startTime = null;
        endTime = null;
        snsUsed = false;
        bottle = "none";
        bottleQty = 0;
        left = 0;
        right = 0;
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
        public String getMenuIdentifier() { return this.menuIdentifier; }
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

    // METHODS TO CONTROL START/PAUSE/RESET FOR THE LEFT AND RIGHT TIMERS

    private void startMaster() {
        masterStartTime = SystemClock.uptimeMillis();
        masterHandler.postDelayed(masterRunnable, 0);
        //btResetMaster.setEnabled(false);
    }

    private void pauseMaster() {
        masterTimeBuff += masterMillisTime;
        masterHandler.removeCallbacks(masterRunnable);
        //btResetMaster.setEnabled(true);
    }

    private void resetMaster() {
        masterHandler.removeCallbacks(masterRunnable);
        masterMillisTime = 0L ;
        masterStartTime = 0L ;
        masterTimeBuff = 0L ;
        masterUpdateTime = 0L ;
        masterSeconds = 0 ;
        masterMinutes = 0 ;
        masterMillis = 0 ;
        tvMasterTimer.setText("00:00");
    }

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
        leftHandler.removeCallbacks(leftRunnable);
        leftMillisTime = 0L ;
        leftStartTime = 0L ;
        leftTimeBuff = 0L ;
        leftUpdateTime = 0L ;
        leftSeconds = 0 ;
        leftMinutes = 0 ;
        leftMillis = 0 ;
        tvLeftTimer.setText("00:00");
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
        rightHandler.removeCallbacks(rightRunnable);
        rightMillisTime = 0L ;
        rightStartTime = 0L ;
        rightTimeBuff = 0L ;
        rightUpdateTime = 0L ;
        rightSeconds = 0 ;
        rightMinutes = 0 ;
        rightMillis = 0 ;
        tvRightTimer.setText("00:00");
    }


    // RUNNABLES FOR ALL TIMERS (master, left, right)
    public Runnable masterRunnable = new Runnable() {

        public void run() {

            masterMillisTime = SystemClock.uptimeMillis() - masterStartTime;
            masterUpdateTime = masterTimeBuff + masterMillisTime;
            masterSeconds = (int) (masterUpdateTime / 1000);
            masterMinutes = masterSeconds / 60;
            masterSeconds = masterSeconds % 60;
            masterMillis = (int) (masterUpdateTime % 1000);

            tvMasterTimer.setText("" + String.format("%02d", masterMinutes) + ":"
                    + String.format("%02d", masterSeconds));
            // If milliseconds desired, cat this: + ":" + String.format("%03d", masterMillis)
            masterHandler.postDelayed(this, 0);
        }
    };

    public Runnable leftRunnable = new Runnable() {

        public void run() {

            leftMillisTime = SystemClock.uptimeMillis() - leftStartTime;
            leftUpdateTime = leftTimeBuff + leftMillisTime;
            leftSeconds = (int) (leftUpdateTime / 1000);
            leftMinutes = leftSeconds / 60;
            leftSeconds = leftSeconds % 60;
            leftMillis = (int) (leftUpdateTime % 1000);

            tvLeftTimer.setText("" + String.format("%02d", leftMinutes) + ":"
                    + String.format("%02d", leftSeconds));

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

            tvRightTimer.setText("" + String.format("%02d", rightMinutes) + ":"
                    + String.format("%02d", rightSeconds));

            rightHandler.postDelayed(this, 0);
        }
    };

    // Toasting helper method
    private void makeToast(String message){
        Toast.makeText(FeedingActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
