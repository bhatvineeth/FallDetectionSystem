package com.ssns.falldetectionsystem;

import android.telephony.SmsManager;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import java.util.TimerTask;

public class FallDetection extends TimerTask {
    private double mLowerAccFallThreshold = 6.962721499999999; // 0.71g
    private double mUpperAccFallThreshold = 19.122967499999998; // 1.95g
    private double mAngularVelocityThreshold = 0.026529; // 1.52 deg / s
    private double mTiltValue = 60; // 60 deg
    private double mTilt;
    private double mDelay;
    private boolean mUserConfirmation;

    public static final float FILTER_COEFFICIENT = 0.98f;
    private static float degreeFloat;
    private static float degreeFloat2;
    private float[] fusedOrientation = new float[3];

    SmsManager smsManager = SmsManager.getDefault();

    public void falldetection() {

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
                    if (ActivityMonitoring.getOmegaMagnitude() > mAngularVelocityThreshold) {
                        if (degreeFloat > mTiltValue || degreeFloat2 > mTiltValue) {
                            MainActivity mainActivity = new MainActivity();
                            if (!mainActivity.alertView()){
                                Log.d("DANGER!!!", "User location at => " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(ActivityMonitoring.getLatitude()) + "," + String.valueOf(ActivityMonitoring.getLongitude()));
                                String textMsg = "Hello, I have fallen down here-> " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(ActivityMonitoring.getLatitude()) + "," + String.valueOf(ActivityMonitoring.getLongitude()) + "need help immediately!!";
                                smsManager.sendTextMessage("015906196190", null, textMsg, null, null);
                            }
                        }
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
        try {
            falldetection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
