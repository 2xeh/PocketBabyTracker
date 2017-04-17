package com.example.pocketbabytracker;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExportActivity extends AppCompatActivity {

    private final String TAG = "Andrea-Export";
    TextView tvSelectedBaby;
    private static final int MENU_FIRST = Menu.FIRST;

    private String fileName;
    private static final int READ_WRITE_ACCESS_CODE = 1;
    private DatabaseQuery databaseQuery;
    private SharedPreferences sharedPreferences;
    private String babyName;
    private String babyReport = "";
    private File pdfToEmail;
    private boolean isReportEmpty = true;


    String startHtmlString = "<!DOCTYPE html> <html> <head> <meta charset='utf-8'> <meta name='viewport' content='width=device-width, initial-scale=1'> <title>PocketBaby Tracker - Report</title> <script type='text/javascript' src='http://cdnjs.cloudflare.com/ajax/libs/jquery/2.0.3/jquery.min.js'> </script> <script type='text/javascript' src='http://netdna.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js'> </script> <link href='http://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.3.0/css/font-awesome.min.css' rel='stylesheet' type='text/css'> <link href='http://pingendo.github.io/pingendo-bootstrap/themes/default/bootstrap.css' rel='stylesheet' type='text/css'> </head> <body> <div class='section' > <div class='container' style='margin:100px;'>";
    String endHtmlString = "</div> </div> </body> </html>";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        // Ensure Read/Write permissions are enabled
        requestReadWritePermissions();

        // Set the databaseQuery
        databaseQuery = new DatabaseQuery(this);

        // Get the baby name out of shared preferences
        Log.d(TAG, "get the shared preferences");
        sharedPreferences = this.getSharedPreferences("pocketBaby", this.MODE_PRIVATE);
        babyName = sharedPreferences.getString("babyName", "");

        // a control to show current baby selected
        tvSelectedBaby = (TextView) findViewById(R.id.tvExportCurrentBaby);
        Log.d(TAG, "Getting child from preferences and setting on screen");
        tvSelectedBaby.setText("Selected child: " + babyName);

        // Get the HTML string to convert to pdf
        babyReport = generateHtmlString();
        Log.d("htmlString", babyReport);

    }

    private String generateHtmlString() {

        // Initialize the body with baby name
        String bodyHtmlString = " <h1> Feedings for baby: " + babyName + "</h1> ";

        // get all of the feedings
        List<FeedingElements> feedingElements = databaseQuery.getFeedingsForBaby(babyName);

        // So long as there are feedings to report on, add a div for each feeding
        if (!feedingElements.isEmpty()) {
            // set the flag to indicate there is data to report on
            isReportEmpty = false;

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
                    bodyHtmlString += "<div class='row'><div class='col-md-8'> <h3> Start Time: "
                            + startDOWString + ", "
                            + startMonthString + " "
                            + startDay + ", "
                            + startYear + " - "
                            + startHour + ":" + String.format("%02d", startMinute) + "hrs"
                            + "</h3> <p>"
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
            // Set the flag to indicate there is nothing to report on
            isReportEmpty = true;
            bodyHtmlString += "<div class='col-md-3'> <h2>No Data to export</h2></div>";
        }

        return startHtmlString + bodyHtmlString + endHtmlString;
    }

    public void requestReadWritePermissions() {
        if( !( ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            )){

            // Show rationale and request permission.
            ActivityCompat.requestPermissions(
                    ExportActivity.this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1);
        }
    }

    public void exportSummary(View view){
        // make sure permissions are set
        requestReadWritePermissions();

        // check for internet connection
        if (NetworkAvailability.hasInternetConnection(this)) { //&& !isReportEmpty
            ConvertHTMLStringToPDF();
        } else {
            Toast.makeText(getBaseContext(), "Network is not available", Toast.LENGTH_SHORT).show();
        }
    }

        // from PDF to HTML Rocket
        public void ConvertHTMLStringToPDF() {

            Log.d(TAG, "Starting ConvertHTMLStringToPDF()");

            String apiKey = "4d1bebd1-a13c-47f8-bb8a-235e7d746bf9";
            String value = babyReport;
            String apiURL = "http://api.html2pdfrocket.com/pdf";
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("apiKey", apiKey);
            params.put("value", value);

            // Call the API convert to a PDF
            InputStreamReader request = new InputStreamReader(Request.Method.POST, apiURL,
                    new Response.Listener<byte[]>(){
                        @Override
                        public void onResponse(byte[] response) {

                            Log.d(TAG, "onResponse() of InputStreamReader for ConvertHtmlStringToPDF");
                            try {
                                if(response != null) {

                                    // get today's date
                                    Calendar today = Calendar.getInstance();
                                    String todayString = "-"
                                            + today.get(Calendar.DAY_OF_MONTH)
                                            + "-" + (today.get(Calendar.MONTH) + 1)
                                            + "-" + today.get(Calendar.YEAR);
                                    fileName = "PocketBaby-" + babyName + todayString +".pdf";
                                    Log.d(TAG, "file name is: " + fileName);

                                    File localFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                    Log.d(TAG, "localFolder: " + localFolder);

                                    if(!localFolder.exists()) {
                                        Log.d(TAG, "Making directory");
                                        boolean createDirResult = localFolder.mkdirs();
                                    }

                                    // Write stream output to local file
                                    File pdfFile =  new File (localFolder, fileName);
                                    OutputStream opStream = new FileOutputStream(pdfFile);

                                    Log.d(TAG, "pdf file created, output stream instantiated");

                                    pdfFile.setWritable(true);

                                    Log.d(TAG, "Opening stream for writing");
                                    opStream.write(response);

                                    Log.d(TAG, "Flushing stream");
                                    opStream.flush();

                                    Log.d(TAG, "Closing stream");
                                    opStream.close();

                                    Log.d(TAG, "File downloaded to: " + pdfFile.getAbsolutePath());
                                    pdfToEmail = pdfFile;

                                }
                            } catch (Exception ex) {
                                Toast.makeText(getBaseContext(), "Error: Unable to generate PDF file", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Error while generating PDF file!");
                            }
                        }}, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Volley error from Response.ErrorListener");
                    error.printStackTrace();
                }
            }, params);
            RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
            mRequestQueue.add(request);
        }


        // handles the result of the location permission request by implementing the
        // ActivityCompat.OnRequestPermissionsResultCallback
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

            // Caution: string comparison for permissions
            if (requestCode == READ_WRITE_ACCESS_CODE) {
                if ((permissions.length == 2) &&
                        (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) &&
                        (grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                        (permissions[1].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) &&
                        (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                    Log.d(TAG, "Read write permissions are granted");

                    try {
                        ConvertHTMLStringToPDF();
                    } catch(SecurityException e) {
                        Log.d(TAG, "SecurityException in HtmlPdfRocketActivity.onRequestPermissionsResult: " + e.getMessage());
                    }
                } else {
                    // Permission was denied. Display an error message.
                    Toast.makeText(ExportActivity.this, "Permission to read and write data was denied.", Toast.LENGTH_LONG).show();
                }
            }
        }

        // Helper class to send request to web API
        class InputStreamReader extends Request<byte[]> {
            private final Response.Listener<byte[]> mListener;
            private Map<String, String> mParams;
            public Map<String, String> responseHeaders;
            public InputStreamReader(int method, String mUrl, Response.Listener<byte[]> listener,
                                     Response.ErrorListener errorListener, HashMap<String, String> params) {

                super(method, mUrl, errorListener);
                // Every time it should make new request, should not use cache
                setShouldCache(false);
                mListener = listener;
                mParams = params;
            }
            @Override
            protected Map<String, String> getParams()
                    throws com.android.volley.AuthFailureError {
                return mParams;
            }
            @Override
            protected void deliverResponse(byte[] response) {
                mListener.onResponse(response);
            }
            @Override
            protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
                //Initialise local responseHeaders map with response headers received
                responseHeaders = response.headers;
                //Pass the response data here
                return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
            }
        }

    // Thanks to this StackOverflow answer:
    // http://stackoverflow.com/questions/2197741/how-can-i-send-emails-from-my-android-application#2197841
    public void sendEmail(View view){

        Uri path = Uri.fromFile(pdfToEmail);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");

        // Set up the fields for email. The user can set recipient or add to message in their own email client
        intent.putExtra(Intent.EXTRA_SUBJECT, "PocketBaby Report for baby " + babyName);
        intent.putExtra(Intent.EXTRA_TEXT, "Attached is a pdf report of the feedings for baby " + babyName + ".");
        intent.putExtra(Intent.EXTRA_STREAM, path);

        try {
            startActivity(Intent.createChooser(intent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ExportActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
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

        String babyName = item.getTitle().toString();

        tvSelectedBaby.setText("Selected child: " + babyName);


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



    // A helper method for easy long toasts
    private void makeToast(String message){
        Toast.makeText(ExportActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
