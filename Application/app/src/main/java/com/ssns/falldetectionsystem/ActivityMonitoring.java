package com.ssns.falldetectionsystem;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Timer;

import static android.util.Half.EPSILON;

public class ActivityMonitoring extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorGyroscope;
    private Timer timer = new Timer();
    private double[] gravity = new double[3];
    private double[] linear_acceleration = new double[3];
    private double totalSumVector = 0.0;
    public static final int TIME_CONSTANT = 30;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;


    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
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
            // Event came from the light sensor.
            case Sensor.TYPE_LIGHT:
                //mTextSensorLight.setText(getResources().getString(
                        //R.string.label_light, currentValue));
                break;
            case Sensor.TYPE_ACCELEROMETER:

                final double alpha = 0.8;

                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                // Remove the gravity contribution with the high-pass filter.
                linear_acceleration[0] = event.values[0] - gravity[0];
                linear_acceleration[1] = event.values[1] - gravity[1];
                linear_acceleration[2] = event.values[2] - gravity[2];
                Log.d("Activity Monitoring","mTextSensorLinearAccelerationX: " + linear_acceleration[0]);
                Log.d("Activity Monitoring","mTextSensorLinearAccelerationY: " + linear_acceleration[1]);
                Log.d("Activity Monitoring","mTextSensorLinearAccelerationZ: " + linear_acceleration[2]);
                //mTextSensorLinearAccelerationX.setText(getResources().getString(R.string.linear_acceleration_x, linear_acceleration[0]));
                //mTextSensorLinearAccelerationY.setText(getResources().getString(R.string.linear_acceleration_y, linear_acceleration[1]));
                //mTextSensorLinearAccelerationZ.setText(getResources().getString(R.string.linear_acceleration_z, linear_acceleration[2]));
                totalSumVector = Math.sqrt((linear_acceleration[0] * linear_acceleration[0]) + (linear_acceleration[1] * linear_acceleration[1])
                        + (linear_acceleration[2] * linear_acceleration[2]));
                //mTextSensorTotalSumVector.setText(getResources().getString(R.string.totalSumVector, totalSumVector));
                //Log.d("Activity Monitoring","mTextSensorTotalSumVector: " + totalSumVector);
                break;
            case Sensor.TYPE_GYROSCOPE:

                float axisX = event.values[0];
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
                int inclinationZ = (int) Math.round(Math.toDegrees(Math.acos(axisZ)));

                //mRotationX.setText(getResources().getString(R.string.rotation_x, inclinationX));
                //mRotationY.setText(getResources().getString(R.string.rotation_y, inclinationY));
                //mRotationZ.setText(getResources().getString(R.string.rotation_z, inclinationZ));

                //mAngularVelocity.setText(getResources().getString(R.string.angular_velocity, omegaMagnitude));

                break;
            default:
                // do nothing
        }

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

}
