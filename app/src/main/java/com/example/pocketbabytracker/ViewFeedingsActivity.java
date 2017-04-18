package com.example.pocketbabytracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

public class ViewFeedingsActivity extends AppCompatActivity {

    private final String TAG = "Andrea-ViewFeeding";
    TextView tvSelectedBaby;
    private String babyName;
    private String babyReport = "";
    private static final int MENU_FIRST = Menu.FIRST;

    WebView wvViewFeedings;
    private SharedPreferences sharedPreferences;
    private DatabaseQuery databaseQuery;



    String startHtmlString = "<!DOCTYPE html> <html> <head> <meta charset='utf-8'> <meta name='viewport' content='width=device-width, initial-scale=1'> <title>PocketBaby Tracker - Report</title> <script type='text/javascript' src='http://cdnjs.cloudflare.com/ajax/libs/jquery/2.0.3/jquery.min.js'> </script> <script type='text/javascript' src='http://netdna.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js'> </script> <link href='http://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.3.0/css/font-awesome.min.css' rel='stylesheet' type='text/css'> <link href='http://pingendo.github.io/pingendo-bootstrap/themes/default/bootstrap.css' rel='stylesheet' type='text/css'> </head> <body> <div class='section' > <div class='container'>";
    String endHtmlString = "</div> </div> </body> </html>";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feedings);


        // Set the databaseQuery
        databaseQuery = new DatabaseQuery(this);

        // Get the baby name out of shared preferences
        Log.d(TAG, "get the shared preferences");
        sharedPreferences = this.getSharedPreferences("pocketBaby", this.MODE_PRIVATE);
        babyName = sharedPreferences.getString("babyName", "");
        Log.d(TAG, "selected baby: " + babyName);

        // Get the view
        wvViewFeedings = (WebView) findViewById(R.id.wvViewFeedings);

        Log.d(TAG, "enabling javascript");
        // Enable JavaScript
        WebSettings webSettings = wvViewFeedings.getSettings();
        webSettings.setJavaScriptEnabled(true);

        loadReport();

    }

    private void loadReport(){
        Log.d(TAG, "generating report html");
        // Generate the report html
        babyReport = generateHtmlString();
        Log.d(TAG, babyReport);

        Log.d(TAG, "Load the generated html into the webview");
        // Using a referenced file
        wvViewFeedings.loadData(babyReport, "text/html", null);
    }

    private String generateHtmlString() {

        // Initialize the body with baby name
        String bodyHtmlString = " <h2> Feedings for baby: " + babyName + "</h2> ";

        // get all of the feedings
        List<FeedingElements> feedingElements = databaseQuery.getFeedingsForBaby(babyName);

        // So long as there are feedings to report on, add a div for each feeding
        if (!feedingElements.isEmpty()) {
            for (FeedingElements feeding : feedingElements) {
                // try to get all of the data, format it and add it to the body of the HTML string
                try {
                    // Let's process the elements
                    Calendar startTimeDate = Calendar.getInstance();
                    startTimeDate.setTimeInMillis(feeding.getStartTime());

                    int startYear = startTimeDate.get(Calendar.YEAR);
                    int startMonth = startTimeDate.get(Calendar.MONTH);
                    int startDayOfWeek = startTimeDate.get(Calendar.DAY_OF_WEEK);
                    int startDay = startTimeDate.get(Calendar.DAY_OF_MONTH);
                    int startHour = startTimeDate.get(Calendar.HOUR_OF_DAY);
                    int startMinute = startTimeDate.get(Calendar.MINUTE);

                    String startDOWString = DaysOfTheWeek.getName(startDayOfWeek);
                    String startMonthString = MonthNames.getName(startMonth);

                    Calendar endTimeDate = Calendar.getInstance();
                    endTimeDate.setTimeInMillis(feeding.getEndTime());

                    int endYear = endTimeDate.get(Calendar.YEAR);
                    int endMonth = endTimeDate.get(Calendar.MONTH);
                    int endDayOfWeek = endTimeDate.get(Calendar.DAY_OF_WEEK);
                    int endDay = endTimeDate.get(Calendar.DAY_OF_MONTH);
                    int endHour = endTimeDate.get(Calendar.HOUR_OF_DAY);
                    int endMinute = endTimeDate.get(Calendar.MINUTE);

                    String endDOWString = DaysOfTheWeek.getName(endDayOfWeek);
                    String endMonthString = MonthNames.getName(endMonth);

                    // todo: time on breast should be in minutes, handle when time is under 1 minute
                    bodyHtmlString += "<div class='row'><div class='col-md-8'> <h4> Start Time: "
                            + startDOWString + ", "
                            + startMonthString + " "
                            + startDay + ", "
                            + startYear + " - "
                            + startHour + ":" + String.format("%02d", startMinute) + "hrs"
                            + "</h4> <p>"
                            + "<strong>Left Breast: </strong>" + Math.round(feeding.getLeft()/1000) + " seconds <br>"
                            + "<strong>Right Breast: </strong>" + Math.round(feeding.getRight()/1000) + " seconds <br>"
                            + "<strong>Supplemental Nursing Used: </strong>" + feeding.getSns() + " <br>"
                            + "<strong>Bottle or SNS Type: </strong>" + feeding.getBottle() + "<br>"
                            + "<strong>Qty: </strong>" + feeding.getBottleQty()  + "mL <br>"
                            + "<strong>End Time: </strong>"
                            + endDOWString + ", "
                            + endMonthString + " "
                            + endDay + ", "
                            + endYear + " - "
                            + endHour + ":" + String.format("%02d", endMinute) + "hrs"
                            + "</p> </div> </div>";

                } catch (Exception e) {
                    Log.e(TAG, "ERROR parsing the feeding record. " + e.getStackTrace().toString());

                    // Output the feeding details to the log (ignoring bottle & bottle qty)
                    Log.d(TAG, "Start: " + feeding.getStartTime()
                            + ", End: " + feeding.getEndTime()
                            + ", Baby: " + feeding.getBabyName()
                            + ", Left (seconds): " + (feeding.getLeft() / 1000)
                            + ", Right (seconds): " + (feeding.getRight() / 1000)
                            + ", SNS: " + feeding.getSns());
                }
            }
        }
        // Otherwise there are no feedings to report on
        else {
            bodyHtmlString += "<div class='col-md-3'> <h2>No Data to export</h2></div>";
        }

        return startHtmlString + bodyHtmlString + endHtmlString;
    }

    // Part 1 of options menu with baby names
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

    // Part 2 of options menu with baby names
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        babyName = item.getTitle().toString();

        // instantiate the SharedPreferences Editor and put in the new values
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("babyName", babyName);

        // commit changes made by the editor
        if (editor.commit()) {
            loadReport();
        } else {
            makeToast("Unable to set baby.");
        }

        return super.onOptionsItemSelected(item);
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

    // A helper method for easy long toasts
    private void makeToast(String message){
        Toast.makeText(ViewFeedingsActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
