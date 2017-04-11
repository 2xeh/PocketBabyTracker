package com.example.pocketbabytracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class ExportActivity extends AppCompatActivity {

    private final String TAG = "Andrea-Export";
    TextView tvExportedFilePath;
    private String fileName;
    private static final int READ_WRITE_ACCESS_CODE = 1;
    //    private static final int WRITE_ACCESS_CODE = 102;
    String sampleHtml;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);

        tvExportedFilePath = (TextView) findViewById(R.id.tvExportedFilePath);


        sampleHtml = "<!DOCTYPE html> <html> <head> <meta charset='utf-8'> <meta name='viewport' content='width=device-width, initial-scale=1'> <title>PocketBaby Tracker - Report</title> <script type='text/javascript' src='http://cdnjs.cloudflare.com/ajax/libs/jquery/2.0.3/jquery.min.js'></script> <script type='text/javascript' src='http://netdna.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js'></script> <link href='http://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.3.0/css/font-awesome.min.css' rel='stylesheet' type='text/css'> <link href='http://pingendo.github.io/pingendo-bootstrap/themes/default/bootstrap.css' rel='stylesheet' type='text/css'> </head> <body> <div class='section'> <div class='container'> <div class='row'> <div class='col-md-3'> <h2>A title</h2> <p>Lorem ipsum dolor sit amet, consectetur adipisici elit, <br>sed eiusmod tempor incidunt ut labore et dolore magna aliqua. <br>Ut enim ad minim veniam, quis nostrud</p></div><div class='col-md-3'> <h2>A title</h2> <p>Lorem ipsum dolor sit amet, consectetur adipisici elit, <br>sed eiusmod tempor incidunt ut labore et dolore magna aliqua. <br>Ut enim ad minim veniam, quis nostrud</p></div><div class='col-md-3'> <h2>A title</h2> <p>Lorem ipsum dolor sit amet, consectetur adipisici elit, <br>sed eiusmod tempor incidunt ut labore et dolore magna aliqua. <br>Ut enim ad minim veniam, quis nostrud</p></div><div class='col-md-3'> <h2>A title</h2> <p>Lorem ipsum dolor sit amet, consectetur adipisici elit, <br>sed eiusmod tempor incidunt ut labore et dolore magna aliqua. <br>Ut enim ad minim veniam, quis nostrud</p></div></div></div></div></body></html>";


    }

    public void startPDFRocket(View view){
        startActivity(new Intent(this, HtmlPdfRocketActivity.class));
    }

    public void exportSummary(View view){
    //turning on the my location layer
    //(runtime location permission is required)
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
        ==PackageManager.PERMISSION_GRANTED &&ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ==PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "Read and Write permissions granted.");
            // mMap.setMyLocationEnabled(true);
            ConvertHTMLStringToPDF();
        }
        else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(
                    ExportActivity.this,
                    new String[]{
                            android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1);
        }


        //// check for internet connection
        //if (NetworkAvailability.hasInternetConnection(this) == false) {
        //    ConvertHTMLStringToPDF(sampleHtml);
        //} else {
        //    Toast.makeText(getBaseContext(), "Network is not available", Toast.LENGTH_SHORT).show();
        //}
    }

        // from PDF to HTML Rocket
        public void ConvertHTMLStringToPDF()
        {
            Log.d(TAG, "Starting ConvertHTMLStringToPDF()");

            String apiKey = "4d1bebd1-a13c-47f8-bb8a-235e7d746bf9";
            String value = sampleHtml;
            String apiURL = "http://api.html2pdfrocket.com/pdf";
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("apiKey", apiKey);
            params.put("value", value);

            // Call the API convert to a PDF
            InputStreamReader request = new InputStreamReader(Request.Method.POST, apiURL,
                    new Response.Listener<byte[]>(){
                        @Override
                        public void onResponse(byte[] response) {
                            try {
                                if(response != null) {

                                    // AA NOTE: let's update this to use today's date
                                    Random r = new Random();
                                    int i1 = r.nextInt(80 - 65) + 65;
                                    fileName = "sample" + i1 + ".pdf";

                                    Log.d(TAG, "file name is: " + fileName);

                                    //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                    //File localFolder = new File(Environment.getExternalStorageDirectory(), "Download")
                                    //File localFolder = Environment.getExternalStoragePublicDirectory("Android/data/com.android.browser/files/Download");
                                    File localFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                                    Log.d(TAG, "localFolder: " + localFolder);

                                    if(!localFolder.exists()) {
                                        Log.d(TAG, "Making directory");
                                        boolean createDirResult = localFolder.mkdirs();

                                        Log.d(TAG, "Making directory successful? " + createDirResult);
                                    }

                                    Log.d(TAG, "Does the localFolder exist?: " + localFolder.exists());

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

                                    tvExportedFilePath.setText("File downloaded to: " + pdfFile.getAbsolutePath());

                                    Log.d(TAG, "sending broadcast");
                                    // now we need to broadcast that the file was downloaded
                                    MediaScannerConnection.scanFile(ExportActivity.this, new String[] { pdfFile.getAbsolutePath()},
                                            null,
                                            new MediaScannerConnection.OnScanCompletedListener() {
                                                @Override
                                                public void onScanCompleted(String path, Uri uri) {
                                                }
                                            });

                                    Log.d(TAG, "broadcast sent");

                                }
                            } catch (Exception ex) {
                                // Toast.makeText(getBaseContext(), "Error while generating PDF file!!", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Error while generating PDF file!!");
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


        //handles the result of the location permission request by implementing the
        // ActivityCompat.OnRequestPermissionsResultCallback
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

            if (requestCode == READ_WRITE_ACCESS_CODE) {
                if (permissions.length == 2 &&
                        permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        permissions[1] == Manifest.permission.WRITE_EXTERNAL_STORAGE &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Log.d(TAG, "permissions are granted, trying to run method");
                    //either check to see if permission is available, or handle a potential SecurityException before calling mMap.setMyLocationEnabled
                    try {
                        ConvertHTMLStringToPDF();
                    } catch(SecurityException e) {
                        Log.d(TAG, "SecurityException in HtmlPdfRocketActivity.onRequestPermissionsResult: " + e.getMessage());
                    }
                } else {
                    // Permission was denied. Display an error message.
                    Toast.makeText(ExportActivity.this, "Permission to read and write data was denied. Unable to generate PDF report.", Toast.LENGTH_LONG).show();
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
}
