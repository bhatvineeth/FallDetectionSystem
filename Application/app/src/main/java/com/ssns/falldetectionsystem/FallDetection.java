package com.ssns.falldetectionsystem;

import android.util.Log;

import java.util.TimerTask;

public class FallDetection extends TimerTask {
    private int mLowerAccFallThreshold;
    private int mUpperAccFallThreshold;
    private double mDelayTime=30;
    private double mTiltValue = 90.0;
    private double mTilt;
    private double mDelay;
    private  double mtotalsumAccVal;
    private boolean mUserConfirmation;


    public void falldetection(){
        mtotalsumAccVal = ActivityMonitoring.getTotalSumVector();
        if (mtotalsumAccVal < mLowerAccFallThreshold){
            if (mDelay< mDelayTime ){
                if (mtotalsumAccVal> mUpperAccFallThreshold &&  mTilt> mTiltValue ){
                  //TODO: Alert sms method.
                }
            }

        }
        else{
            //TODO: Sensor data reading
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
