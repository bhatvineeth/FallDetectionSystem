package com.ssns.falldetectionsystem;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.SmsManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static android.util.Half.EPSILON;

public class ActivityMonitoring extends Service implements SensorEventListener {

    Handler handler = new Handler(Looper.getMainLooper());
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorGyroscope;
    private Sensor mSensorMagnetic;
    private Timer timer = new Timer();
    private double[] gravity = new double[3];
    private double[] linear_acceleration = new double[3];
    private static double totalSumVector = 0.0;
    public static final int TIME_CONSTANT = 30;
    private static double mAngularVelocity = 20;
    private static float omegaMagnitude;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    private static float[] accMagOrientation = new float[3];
    private float[] rotationMatrix = new float[9];

    private static float[] gyroMatrix = new float[9];
    private float[] gyro = new float[3];
    private static float[] gyroOrientation = new float[3];
    private float[] fusedOrientation = new float[3];
    private float[] accel = new float[3];
    private float[] magnet = new float[3];

    public static final float EPSILON = 0.000000001f;
    private boolean initState = true;
    public static final float FILTER_COEFFICIENT = 0.98f;

    private static float degreeFloat;
    private static float degreeFloat2;

    public static boolean alarmFlag = false;

    public static long startTimer = 0;

    //GPS
    private static double latitude, longitude;
    LocationManager locationManager;
    LocationListener locationListener;

    public static boolean userConfirmation= false;

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Log.d("Latitude ", "" + latitude);
                Log.d("Longitude ", "" + longitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details
        }
        latitude = locationManager.getLastKnownLocation(locationProvider).getLatitude();
        longitude = locationManager.getLastKnownLocation(locationProvider).getLongitude();

        Log.d("Latitude ", "" + latitude);
        Log.d("Longitude ", "" + longitude);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        String sensor_error = getResources().getString(R.string.error_no_sensor);


        if (mSensorAccelerometer == null) {
           //mTextSensorLinearAccelerationX.setText(sensor_error);
            //mTextSensorLinearAccelerationY.setText(sensor_error);
            //mTextSensorLinearAccelerationZ.setText(sensor_error);
        }

        if(mSensorGyroscope == null) {
            //mRotationX.setText(sensor_error);
            //mRotationY.setText(sensor_error);
            //mRotationZ.setText(sensor_error);
        }
        initListeners();

        timer.scheduleAtFixedRate(new FallDetection(), 1000, TIME_CONSTANT);

        return START_STICKY;
    }

    public void initListeners() {
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (mSensorGyroscope != null) {
            mSensorManager.registerListener(this, mSensorGyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        }
        if (mSensorMagnetic != null) {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    public void calculate(){

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();
        float currentValue = event.values[0];
        switch (sensorType) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accel, 0, 3);
                if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
                    SensorManager.getOrientation(rotationMatrix, accMagOrientation);
               }

                totalSumVector = Math.sqrt(accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2]);

                if (totalSumVector > 15) {
                    Log.d("Activity Monitoring","mTextSensorTotalSumVector: " + totalSumVector);
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroFunction(event);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                // copy new magnetometer data into magnet array
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
            default:
                // do nothing
        }

    }

    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if (initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    public static float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float) Math.sin(o[1]);
        float cosX = (float) Math.cos(o[1]);
        float sinY = (float) Math.sin(o[2]);
        float cosY = (float) Math.cos(o[2]);
        float sinZ = (float) Math.sin(o[0]);
        float cosZ = (float) Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f;
        xM[1] = 0.0f;
        xM[2] = 0.0f;
        xM[3] = 0.0f;
        xM[4] = cosX;
        xM[5] = sinX;
        xM[6] = 0.0f;
        xM[7] = -sinX;
        xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY;
        yM[1] = 0.0f;
        yM[2] = sinY;
        yM[3] = 0.0f;
        yM[4] = 1.0f;
        yM[5] = 0.0f;
        yM[6] = -sinY;
        yM[7] = 0.0f;
        yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ;
        zM[1] = sinZ;
        zM[2] = 0.0f;
        zM[3] = -sinZ;
        zM[4] = cosZ;
        zM[5] = 0.0f;
        zM[6] = 0.0f;
        zM[7] = 0.0f;
        zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private static float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor) {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        omegaMagnitude =
                (float) Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if (omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public boolean stopService(Intent name) {
            mSensorManager.unregisterListener(this);
            timer.cancel();
        return super.stopService(name);
    }


    public static double getTotalSumVector(){
        return totalSumVector;
    }

    public static double getmAngularVelocity(){
        return mAngularVelocity;
    }

    public static float getDegreeFloat() {
        return degreeFloat;
    }

    public static float getDegreeFloat2() {
        return degreeFloat2;
    }

    public static double getLatitude() {
        return latitude;
    }

    public static double getLongitude() {
        return longitude;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mPeriodicEventHandler.removeCallbacks(doPeriodicTask);
        stopSelf();
        Log.d("Stopping Service", "OnDestroy");
        //mSensorManager.unregisterListener(this);
        //sendCount = 0;
        //Toast.makeText(this, "Stopped Tracking", Toast.LENGTH_SHORT).show();
        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        //}else {
           // locationManager.removeUpdates(locationListener);
       // }
    }

    public static float[] getGyroOrientation() {
        return gyroOrientation;
    }

    public static void setGyroOrientation(float[] gyroOrientation) {
       gyroOrientation = gyroOrientation;
    }

    public static float[] getAccMagOrientation() {
        return accMagOrientation;
    }

    public static void setAccMagOrientation(float[] accMagOrientation) {
       accMagOrientation = accMagOrientation;
    }

    public static float[] getGyroMatrix() {
        return gyroMatrix;
    }

    public static void setGyroMatrix(float[] gyroMatrix) {
        gyroMatrix = gyroMatrix;
    }
    public static float getOmegaMagnitude() {
        return omegaMagnitude;
    }

    public static void setOmegaMagnitude(float omegaMagnitude) {
        omegaMagnitude = omegaMagnitude;
    }

    public class FallDetection extends TimerTask {
        //private double mLowerAccFallThreshold = 6.962721499999999; // 0.71g
        //private double mUpperAccFallThreshold = 19.122967499999998; // 1.95g
        //private double mAngularVelocityThreshold = 0.026529; // 1.52 deg / s
        //private double mTiltValue = 60; // 60 deg
        private double mLowerAccFallThreshold = 1; // 0.71g
        private double mUpperAccFallThreshold = 15; // 1.95g
        private double mAngularVelocityThreshold = 0.0001; // 1.52 deg / s
        private double mTiltValue = 1; // 60 deg
        private double mTilt;
        private double mDelay;
        private boolean mUserConfirmation;

        public static final float FILTER_COEFFICIENT = 0.98f;
        private  float degreeFloat;
        private  float degreeFloat2;


        SmsManager smsManager = SmsManager.getDefault();

        public void falldetection() throws InterruptedException {

            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
            fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
//            Log.d("X:", ""+fusedOrientation[0]);

            fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
//            Log.d("Y:", ""+fusedOrientation[1]);

            fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
//            Log.d("Z:", ""+fusedOrientation[2]);

            degreeFloat = (float) (fusedOrientation[1] * 180 / Math.PI);
            degreeFloat2 = (float) (fusedOrientation[2] * 180 / Math.PI);
            Log.d("degreeFloat:", ""+degreeFloat);
            Log.d("degreeFloat2:", ""+degreeFloat2);
            Log.d("mAngularVelocityThreshold:", ""+ActivityMonitoring.getOmegaMagnitude());


            if( startTimer != 0 && ((System.currentTimeMillis() - startTimer)>=30000)){
                //send sms
                startTimer = 0;
                //String textMsg = "Hello, I have fallen down here-> " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(ActivityMonitoring.getLatitude()) + "," + String.valueOf(ActivityMonitoring.getLongitude()) + "need help immediately!!";
                String textMsg = "Sorry by mistake";
                //smsManager.sendTextMessage("015906196190", null, textMsg, null, null);
                Log.d("SMS!!!", "SMS Sent");
                Intent stopIntent = new Intent(getApplicationContext(), RingtonePlayingService.class);
                getApplicationContext().stopService(stopIntent);
            }
            //if (totalSumVector < mLowerAccFallThreshold){
                if (totalSumVector > mUpperAccFallThreshold) {
                    if ( omegaMagnitude > mAngularVelocityThreshold) {
                        if (degreeFloat > mTiltValue || degreeFloat2 > mTiltValue) {
                            if(startTimer == 0){
                                startTimer = System.currentTimeMillis();
                            }
                                createChannels();
                                Notification.Builder nb = getAndroidChannelNotification("Confirmation", "Fall Detected, are you able to continue?");
                                getManager().notify(101, nb.build());
                                Log.d("Notification!!!", "Notification Sent");
                                startAlert();
                                Log.d("DANGER!!!", "User location at => " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(ActivityMonitoring.getLatitude()) + "," + String.valueOf(ActivityMonitoring.getLongitude()));
                        }
                    }
                }
            //}

            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
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
    public static final String ANDROID_CHANNEL_ID = "com.chikeandroid.tutsplustalerts.ANDROID";

    public Notification.Builder getAndroidChannelNotification(String title, String body) {

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.setAction("YES_ACTION");
        PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), 101, intent, 0);

        return new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setAutoCancel(true).setUsesChronometer(true).setTimeoutAfter(30000)
                .addAction(R.drawable.ic_launcher_foreground, "YES", pIntent);
    }

    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";

    public void createChannels() {

        // create android channel
        NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        // Sets whether notifications posted to this channel should display notification lights
        androidChannel.enableLights(true);
        // Sets whether notification posted to this channel should vibrate.
        androidChannel.enableVibration(true);
        // Sets the notification light color for notifications posted to this channel
        androidChannel.setLightColor(Color.GREEN);
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(androidChannel);

    }

    NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }


    public void startAlert() {


        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.setAction("ALARM_ACTION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 234324243, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent);
    }

    public static boolean isAlarmFlag() {
        return alarmFlag;
    }

    public static void setAlarmFlag(boolean alarmFlag) {
        ActivityMonitoring.alarmFlag = alarmFlag;
    }
}
