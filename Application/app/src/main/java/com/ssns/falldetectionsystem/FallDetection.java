package com.ssns.falldetectionsystem;

import android.util.Log;

import java.util.TimerTask;

public class FallDetection extends TimerTask {
    private double mLowerAccFallThreshold = 6; // 0.6g
    private double mUpperAccFallThreshold = 25; // 2.55g
    private double mDelayTime = 30;
    private double mTiltValue = 60.0;
    private double mTilt;
    private double mDelay;
    private boolean mUserConfirmation;


    public void falldetection() throws InterruptedException {
        if (ActivityMonitoring.getTotalSumVector() < mLowerAccFallThreshold){
            wait(30);
                if (ActivityMonitoring.getTotalSumVector() > mUpperAccFallThreshold) {
                    if (Math.abs(ActivityMonitoring.getDegreeFloat()) > mTiltValue ||
                            Math.abs(ActivityMonitoring.getDegreeFloat2()) > mTiltValue) {
                        Log.d("DANGER!!!", "User location at => " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(ActivityMonitoring.getLatitude()) + "," + String.valueOf(ActivityMonitoring.getLongitude()));
                    }
                }
        }
    }

    public void calculate(){

    }

    @Override
    public void run() {
        // TODO: ALGORITHM IMPLEMENTATION
        Log.d("FallDetection:  ", "Fall Detection");
      //  falldetection();
    }
}
