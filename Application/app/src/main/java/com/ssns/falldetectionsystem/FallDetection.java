package com.ssns.falldetectionsystem;

import android.util.Log;

import java.util.TimerTask;

public class FallDetection extends TimerTask {
    private int mLowerAccFallThreshold;
    private int mUpperAccFallThreshold;
    private double mTilt;
    private double mDelay;

    public void falldetection(){
    }

    public void calculate(){

    }

    @Override
    public void run() {
        // TODO: ALGORITHM IMPLEMENTATION
        Log.d("FallDetection:  ", "Fall Detection");

    }
}
