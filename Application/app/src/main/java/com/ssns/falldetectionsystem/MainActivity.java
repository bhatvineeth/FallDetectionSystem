package com.ssns.falldetectionsystem;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static android.util.Half.EPSILON;

public class MainActivity extends AppCompatActivity {

    // TextViews to display current sensor values
    //private TextView mTextSensorLight;
    //private TextView mTextSensorLinearAccelerationX;
    //private TextView mTextSensorLinearAccelerationY;
    //private TextView mTextSensorLinearAccelerationZ;
    //private TextView mTextSensorTotalSumVector;

    //private TextView mRotationX;
    //private TextView mRotationY;

    //private TextView mAngularVelocity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mTextSensorLight = (TextView) findViewById(R.id.label_light);
        //mTextSensorLinearAccelerationX = (TextView) findViewById(R.id.linear_acceleration_x);
        //mTextSensorLinearAccelerationY = (TextView) findViewById(R.id.linear_acceleration_y);
        //mTextSensorLinearAccelerationZ = (TextView) findViewById(R.id.linear_acceleration_z);
        //mTextSensorTotalSumVector = (TextView) findViewById(R.id.totalSumVector);

        //mRotationX = (TextView) findViewById(R.id.rotation_x);
        //mRotationY = (TextView) findViewById(R.id.rotation_y);
        //mRotationZ = (TextView) findViewById(R.id.rotation_z);
        //mAngularVelocity =  (TextView) findViewById(R.id.angular_velocity);

        //TODO: CHECK LOGIN, THEN START SERVICE
        Intent intent= new Intent(getApplicationContext(), ActivityMonitoring.class);
        startService(intent);

        //TODO: ON LONGOUT STOP TRACKING
        //stopService(intent);
    }


    public void initiate(){

    }

    public void detectFall(){
        // Falldetection class
    }

    public void triggerAlert(){

    }

    public void userAcknowledgment(){

    }

    public void  saveDetails(){
        // User has details
    }

}