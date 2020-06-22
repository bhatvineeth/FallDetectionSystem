package com.ssns.falldetectionsystem;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static android.util.Half.EPSILON;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    // Individual light and proximity sensors.
    private Sensor mSensorLight;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorGyroscope;
    private double[] gravity = new double[3];
    private double[] linear_acceleration = new double[3];
    private double totalSumVector = 0.0;

    // TextViews to display current sensor values
    private TextView mTextSensorLight;
    private TextView mTextSensorLinearAccelerationX;
    private TextView mTextSensorLinearAccelerationY;
    private TextView mTextSensorLinearAccelerationZ;
    private TextView mTextSensorTotalSumVector;

    private TextView mRotationX;
    private TextView mRotationY;
    private TextView mRotationZ;
    private TextView mAngularVelocity;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mTextSensorLight = (TextView) findViewById(R.id.label_light);
        mTextSensorLinearAccelerationX = (TextView) findViewById(R.id.linear_acceleration_x);
        mTextSensorLinearAccelerationY = (TextView) findViewById(R.id.linear_acceleration_y);
        mTextSensorLinearAccelerationZ = (TextView) findViewById(R.id.linear_acceleration_z);
        mTextSensorTotalSumVector = (TextView) findViewById(R.id.totalSumVector);

        mRotationX = (TextView) findViewById(R.id.rotation_x);
        mRotationY = (TextView) findViewById(R.id.rotation_y);
        mRotationZ = (TextView) findViewById(R.id.rotation_z);
        mAngularVelocity =  (TextView) findViewById(R.id.angular_velocity);

        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        String sensor_error = getResources().getString(R.string.error_no_sensor);

        if (mSensorLight == null) {
            mTextSensorLight.setText(sensor_error);
        }

        if (mSensorAccelerometer == null) {
            mTextSensorLinearAccelerationX.setText(sensor_error);
            mTextSensorLinearAccelerationY.setText(sensor_error);
            mTextSensorLinearAccelerationZ.setText(sensor_error);
        }

        if(mSensorGyroscope == null) {
            mRotationX.setText(sensor_error);
            mRotationY.setText(sensor_error);
            mRotationZ.setText(sensor_error);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mSensorLight != null) {
            mSensorManager.registerListener(this, mSensorLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorGyroscope != null) {
            mSensorManager.registerListener(this, mSensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float currentValue = event.values[0];
        switch (sensorType) {
            // Event came from the light sensor.
            case Sensor.TYPE_LIGHT:
                mTextSensorLight.setText(getResources().getString(
                        R.string.label_light, currentValue));
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
                mTextSensorLinearAccelerationX.setText(getResources().getString(R.string.linear_acceleration_x, linear_acceleration[0]));
                mTextSensorLinearAccelerationY.setText(getResources().getString(R.string.linear_acceleration_y, linear_acceleration[1]));
                mTextSensorLinearAccelerationZ.setText(getResources().getString(R.string.linear_acceleration_z, linear_acceleration[2]));
                totalSumVector = Math.sqrt((linear_acceleration[0] * linear_acceleration[0]) + (linear_acceleration[1] * linear_acceleration[1])
                + (linear_acceleration[2] * linear_acceleration[2]));
                mTextSensorTotalSumVector.setText(getResources().getString(R.string.totalSumVector, totalSumVector));
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

                mRotationX.setText(getResources().getString(R.string.rotation_x, inclinationX));
                mRotationY.setText(getResources().getString(R.string.rotation_y, inclinationY));
                mRotationZ.setText(getResources().getString(R.string.rotation_z, inclinationZ));

                mAngularVelocity.setText(getResources().getString(R.string.angular_velocity, omegaMagnitude));

                break;
            default:
                // do nothing
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
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