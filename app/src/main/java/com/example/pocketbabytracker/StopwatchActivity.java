package com.example.pocketbabytracker;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;


// Used this tutorial to inform timers: http://www.android-examples.com/android-create-stopwatch-example-tutorial-in-android-studio/
public class StopwatchActivity extends AppCompatActivity {

        Chronometer chTestChronometer;

        // attributes for left side timer
        TextView tvLeft;
        Button btStartLeft, btPauseLeft, btResetLeft, btLapLeft;
        long leftMillisTime, leftStartTime, leftTimeBuff, leftUpdateTime = 0L ;
        Handler leftHandler;
        int leftSeconds, leftMinutes, leftMillis;

        // attributes for right side timer
        TextView tvRight;
        Button btStartRight, btPauseRight, btResetRight, btLapRight;
        long rightMillisTime, rightStartTime, rightTimeBuff, rightUpdateTime = 0L ;
        Handler rightHandler;
        int rightSeconds, rightMinutes, rightMillis;



    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_stopwatch);

            // a chronometer to test against
            chTestChronometer = (Chronometer)findViewById(R.id.chTestChronometer);
            chTestChronometer.start();

            tvLeft = (TextView)findViewById(R.id.tvSwLeft);
            btStartLeft = (Button)findViewById(R.id.btSwStartLeft);
            btPauseLeft = (Button)findViewById(R.id.btSwPauseLeft);
            btResetLeft = (Button)findViewById(R.id.btSwResetLeft);
            btLapLeft = (Button)findViewById(R.id.btSWSaveLapLeft) ;

            tvRight = (TextView)findViewById(R.id.tvSwRight);
            btStartRight = (Button)findViewById(R.id.btSwStartRight);
            btPauseRight = (Button)findViewById(R.id.btSwPauseRight);
            btResetRight = (Button)findViewById(R.id.btSwResetRight);
            btLapRight = (Button)findViewById(R.id.btSWSaveLapRight) ;

            // Left side handler for starting and stopping time
            leftHandler = new Handler() ;

            // Left Button listeners
            btStartLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leftStartTime = SystemClock.uptimeMillis();
                    leftHandler.postDelayed(leftRunnable, 0);
                    btResetLeft.setEnabled(false);
                }
            });

            btPauseLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leftTimeBuff += leftMillisTime;
                    leftHandler.removeCallbacks(leftRunnable);
                    btResetLeft.setEnabled(true);
                }
            });

            btResetLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    leftMillisTime = 0L ;
                    leftStartTime = 0L ;
                    leftTimeBuff = 0L ;
                    leftUpdateTime = 0L ;
                    leftSeconds = 0 ;
                    leftMinutes = 0 ;
                    leftMillis = 0 ;
                    tvLeft.setText("00:00:00");
                }
            });


            // Right side handler for starting and stopping time
            rightHandler = new Handler() ;

            // Right Button listeners
            btStartRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rightStartTime = SystemClock.uptimeMillis();
                    rightHandler.postDelayed(rightRunnable, 0);
                    btResetRight.setEnabled(false);
                }
            });

            btPauseRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rightTimeBuff += rightMillisTime;
                    rightHandler.removeCallbacks(rightRunnable);
                    btResetRight.setEnabled(true);
                }
            });

            btResetRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rightMillisTime = 0L ;
                    rightStartTime = 0L ;
                    rightTimeBuff = 0L ;
                    rightUpdateTime = 0L ;
                    rightSeconds = 0 ;
                    rightMinutes = 0 ;
                    rightMillis = 0 ;
                    tvRight.setText("00:00:00");
                }
            });

        }

        public Runnable leftRunnable = new Runnable() {

            public void run() {

                leftMillisTime = SystemClock.uptimeMillis() - leftStartTime;
                leftUpdateTime = leftTimeBuff + leftMillisTime;
                leftSeconds = (int) (leftUpdateTime / 1000);
                leftMinutes = leftSeconds / 60;
                leftSeconds = leftSeconds % 60;
                leftMillis = (int) (leftUpdateTime % 1000);

                tvLeft.setText("" + leftMinutes + ":"
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

                tvRight.setText("" + rightMinutes + ":"
                        + String.format("%02d", rightSeconds) + ":"
                        + String.format("%03d", rightMillis));

                rightHandler.postDelayed(this, 0);
            }
        };




    }
