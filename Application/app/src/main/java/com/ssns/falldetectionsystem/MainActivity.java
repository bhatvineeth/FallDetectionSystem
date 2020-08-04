package com.ssns.falldetectionsystem;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import static android.util.Half.EPSILON;

public class MainActivity extends Activity {

    private Button login, logout;
    SmsManager smsManager = SmsManager.getDefault();
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = (Button) findViewById(R.id.login);
        logout = (Button) findViewById(R.id.logout);

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(101);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS};
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
//            return;
        }

        final Intent intent= new Intent(getApplicationContext(), ActivityMonitoring.class);

        //TODO: CHECK LOGIN, THEN START SERVICE
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                TextView logMessage = (TextView)findViewById(R.id.logMessage);
                logMessage.setText("Log in successful");
                Button loginButon = (Button) findViewById(R.id.login);
                loginButon.setEnabled(false);
                Button logoutButon = (Button) findViewById(R.id.logout);
                logoutButon.setEnabled(true);

                startService(intent);
            }
        });

        //TODO: ON LOGOUT STOP TRACKING
        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                TextView logMessage = (TextView)findViewById(R.id.logMessage);
                logMessage.setText("Log out successful");
                Button loginButon = (Button) findViewById(R.id.login);
                loginButon.setEnabled(true);
                //Intent intent= new Intent(getApplicationContext(), ActivityMonitoring.class);
                stopService(intent);
            }

        });
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