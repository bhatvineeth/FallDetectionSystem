package com.ssns.falldetectionsystem;

import android.util.Log;

import java.util.TimerTask;

public class FallDetection extends TimerTask {
    private double mLowerAccFallThreshold = 6; // 0.6g
    private double mUpperAccFallThreshold = 5; // 2.55g
    private double mDelayTime = 30;
    private double mTiltValue = 2.0;
    private double mTilt;
    private double mDelay;
    private boolean mUserConfirmation;

    public static final float FILTER_COEFFICIENT = 0.98f;
    private static float degreeFloat;
    private static float degreeFloat2;
    private float[] fusedOrientation = new float[3];


    public void falldetection() throws InterruptedException {

        float[] gyroOrientation = ActivityMonitoring.getGyroOrientation();
        float[] accMagOrientation = ActivityMonitoring.getAccMagOrientation();

        float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
        fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
//            Log.d("X:", ""+fusedOrientation[0]);

        fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
//            Log.d("Y:", ""+fusedOrientation[1]);

        fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
//            Log.d("Z:", ""+fusedOrientation[2]);

        degreeFloat = (float) (fusedOrientation[1] * 180 / Math.PI);
        degreeFloat2 = (float) (fusedOrientation[2] * 180 / Math.PI);


        if (ActivityMonitoring.getTotalSumVector() < mLowerAccFallThreshold){
                if (ActivityMonitoring.getTotalSumVector() > mUpperAccFallThreshold) {
                    if (Math.abs(ActivityMonitoring.getDegreeFloat()) > mTiltValue ||
                            Math.abs(ActivityMonitoring.getDegreeFloat2()) > mTiltValue) {
                        Log.d("DANGER!!!", "User location at => " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(ActivityMonitoring.getLatitude()) + "," + String.valueOf(ActivityMonitoring.getLongitude()));
                    }
                }
        }

        ActivityMonitoring.setGyroMatrix(ActivityMonitoring.getRotationMatrixFromOrientation(fusedOrientation));
        System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
        ActivityMonitoring.setGyroOrientation(gyroOrientation);

    }

    public void calculate(){

    }

    @Override
    public void run() {
        // TODO: ALGORITHM IMPLEMENTATION
        //Log.d("FallDetection:  ", "Fall Detection");
        try {
            falldetection();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
