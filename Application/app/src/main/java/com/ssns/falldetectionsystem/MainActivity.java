package com.ssns.falldetectionsystem;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import static android.util.Half.EPSILON;

public class MainActivity extends AppCompatActivity {

    private Button login, logout;
    private Context context = getApplicationContext();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = (Button) findViewById(R.id.login);
        logout = (Button) findViewById(R.id.logout);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
//            return;
        }


        //TODO: CHECK LOGIN, THEN START SERVICE
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(), ActivityMonitoring.class);
                startService(intent);
            }
        });

        //TODO: ON LOGOUT STOP TRACKING
        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(), ActivityMonitoring.class);

                stopService(intent);
            }
        });
    }


    public Context getContext() {
        return context;
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