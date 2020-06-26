package com.ssns.falldetectionsystem;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.Timer;

import static android.util.Half.EPSILON;

public class ActivityMonitoring extends Service implements SensorEventListener {
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

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    private float[] accMagOrientation = new float[3];
    private float[] rotationMatrix = new float[9];
    private float[] gyroMatrix = new float[9];
    private float[] gyro = new float[3];
    private float[] gyroOrientation = new float[3];
    private float[] fusedOrientation = new float[3];
    private float[] accel = new float[3];
    private float[] magnet = new float[3];

    public static final float EPSILON = 0.000000001f;
    private boolean initState = true;
    public static final float FILTER_COEFFICIENT = 0.98f;

    private static float degreeFloat;
    private static float degreeFloat2;

    //GPS
    private static double latitude, longitude;
    LocationManager locationManager;
    LocationListener locationListener;

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
            mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorGyroscope != null) {
            mSensorManager.registerListener(this, mSensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
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

                //final double alpha = 0.8;

                // Isolate the force of gravity with the low-pass filter.
                //gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                //gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                //gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                // Remove the gravity contribution with the high-pass filter.
                //linear_acceleration[0] = event.values[0] - gravity[0];
                //linear_acceleration[1] = event.values[1] - gravity[1];
                //linear_acceleration[2] = event.values[2] - gravity[2];
                //Log.d("Activity Monitoring","mTextSensorLinearAccelerationX: " + linear_acceleration[0]);
                //Log.d("Activity Monitoring","mTextSensorLinearAccelerationY: " + linear_acceleration[1]);
                //Log.d("Activity Monitoring","mTextSensorLinearAccelerationZ: " + linear_acceleration[2]);
                //mTextSensorLinearAccelerationX.setText(getResources().getString(R.string.linear_acceleration_x, linear_acceleration[0]));
                //mTextSensorLinearAccelerationY.setText(getResources().getString(R.string.linear_acceleration_y, linear_acceleration[1]));
                //mTextSensorLinearAccelerationZ.setText(getResources().getString(R.string.linear_acceleration_z, linear_acceleration[2]));
                totalSumVector = Math.sqrt(accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2]);
                //mTextSensorTotalSumVector.setText(getResources().getString(R.string.totalSumVector, totalSumVector));
                if (totalSumVector > 15) {
                    Log.d("Activity Monitoring","mTextSensorTotalSumVector: " + totalSumVector);
                }
                break;
            case Sensor.TYPE_GYROSCOPE:

                /*float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

                if (omegaMagnitude > EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                int inclinationX = (int) Math.round(Math.toDegrees(Math.acos(axisX)));
                int inclinationY = (int) Math.round(Math.toDegrees(Math.acos(axisY)));
                int inclinationZ = (int) Math.round(Math.toDegrees(Math.acos(axisZ)));*/

                //mRotationX.setText(getResources().getString(R.string.rotation_x, inclinationX));
                //mRotationY.setText(getResources().getString(R.string.rotation_y, inclinationY));
                //mRotationZ.setText(getResources().getString(R.string.rotation_z, inclinationZ));

                //mAngularVelocity.setText(getResources().getString(R.string.angular_velocity, omegaMagnitude));
                gyroFunction(event);
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

    private float[] getRotationMatrixFromOrientation(float[] o) {
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

    private float[] matrixMultiplication(float[] A, float[] B) {
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
        float omegaMagnitude =
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

        int inclinationX = (int) Math.round(Math.toDegrees(Math.acos(normValues[0])));
        int inclinationY = (int) Math.round(Math.toDegrees(Math.acos(normValues[1])));
        int inclinationZ = (int) Math.round(Math.toDegrees(Math.acos(normValues[2])));

       /* mRotationX.setText(getResources().getString(R.string.rotation_x, inclinationX));
        mRotationY.setText(getResources().getString(R.string.rotation_y, inclinationY));
        mRotationZ.setText(getResources().getString(R.string.rotation_z, inclinationZ));*/
        //Log.d("Activity Monitoring","inclinationX: " + inclinationX);
        //Log.d("Activity Monitoring","inclinationY: " + inclinationY);
        //Log.d("Activity Monitoring","inclinationZ: " + inclinationZ);
        //mAngularVelocity.setText(getResources().getString(R.string.angular_velocity, omegaMagnitude));
        //Log.d("Activity Monitoring","AngularVelocity: " + omegaMagnitude);

        float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
        fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
//            Log.d("X:", ""+fusedOrientation[0]);

        fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
//            Log.d("Y:", ""+fusedOrientation[1]);

        fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
//            Log.d("Z:", ""+fusedOrientation[2]);

        degreeFloat = (float) (fusedOrientation[1] * 180 / Math.PI);
        degreeFloat2 = (float) (fusedOrientation[2] * 180 / Math.PI);

        /*mRotationX.setText(getResources().getString(R.string.rotation_x, fusedOrientation[0]));
        mRotationY.setText(getResources().getString(R.string.rotation_y, fusedOrientation[1]));
        mRotationZ.setText(getResources().getString(R.string.rotation_z, fusedOrientation[2]));*/

        //Log.d("Activity Monitoring","OrientationX: " + fusedOrientation[0]);
        //Log.d("Activity Monitoring","OrientationY: " + fusedOrientation[1]);
        //Log.d("Activity Monitoring","OrientationZ: " + fusedOrientation[2]);

        /*mTextSensorLinearAccelerationX.setText(getResources().getString(R.string.linear_acceleration_x, degreeFloat));
        mTextSensorLinearAccelerationY.setText(getResources().getString(R.string.linear_acceleration_y, degreeFloat2));*/

        //Log.d("Activity Monitoring","Degree1: " + degreeFloat);
        //Log.d("Activity Monitoring","Degree2: " + degreeFloat2);


        gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
        System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
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
        Log.d("Stopping Service", "OnDestroy");
        //mSensorManager.unregisterListener(this);
        //sendCount = 0;
        //Toast.makeText(this, "Stopped Tracking", Toast.LENGTH_SHORT).show();
        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        //}else {
           // locationManager.removeUpdates(locationListener);
       // }
    }

}
