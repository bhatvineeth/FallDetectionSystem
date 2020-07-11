package com.ssns.falldetectionsimulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class FallDetectionSimulator {

	static float accDataset[][] = new float[1000][4];
	static float gyroDataset[][] = new float[1000][4];
	private float[] gyro = new float[3];
	private static float omegaMagnitude;
	public static final float EPSILON = 0.000000001f;
	private boolean initState = true;
	private static float[] accMagOrientation = new float[3];
	private static float[] gyroMatrix = new float[9];
	private float timestamp;
	private static final float NS2S = 1.0f / 1000000000.0f;
	
	public static void main(String[] args) {
		readDataSets();
		TimerTask task = new TimerTask() {
			public void run() {
	            System.out.println("Task performed on: " + new Date() + "n" +
	              "Thread's name: " + Thread.currentThread().getName());
	        }
	    };
	    Timer timer = new Timer("Timer");
	    
	    long delay = 0;
	    timer.schedule(task, delay, 10);

	}
	
	
    public void gyroFunction(double event[]) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if (initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            //TODO: SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if (timestamp != 0) {
            final float dT = 10000000 * NS2S;
            System.arraycopy(event, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        //timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        //TODO: SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        //TODO: SensorManager.getOrientation(gyroMatrix, gyroOrientation);
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
	
	
	
	
	public static void readDataSets() {
		try {
			File fileAcc = new File("/Users/sjathin/Documents/GitHub/HumanActivityRecognition/Documents/MobiFall_Dataset_v2.0/sub1/FALLS/BSC/BSC_acc_1_1.txt");   
			BufferedReader brAcc=new BufferedReader(new FileReader(fileAcc));
			
			File fileGyro = new File("/Users/sjathin/Documents/GitHub/HumanActivityRecognition/Documents/MobiFall_Dataset_v2.0/sub1/FALLS/BSC/BSC_gyro_1_1.txt");   
			BufferedReader brGyro =new BufferedReader(new FileReader(fileGyro));
			
			
			String line = brAcc.readLine();
			String line2 = brGyro.readLine();
			boolean startRead = false;
			int i = 0;
			while (line != null) {
				if (line.contentEquals("@DATA")) {
					startRead = true;
				}
				if (startRead && !line.contentEquals("@DATA")) {
					accDataset[i][0] = (float) Double.parseDouble(line.split(",")[1]);
					accDataset[i][1] = (float) Double.parseDouble(line.split(",")[2]);
					accDataset[i][2] = (float) Double.parseDouble(line.split(",")[3]);
					
					gyroDataset[i][0] = (float) Double.parseDouble(line2.split(",")[1]);
					gyroDataset[i][1] = (float) Double.parseDouble(line2.split(",")[2]);
					gyroDataset[i][2] = (float) Double.parseDouble(line2.split(",")[3]);
					//System.out.println(gyro[i][0]);
					//System.out.println(acc[i][0]);
				}
				line = brAcc.readLine();
				line2 = brGyro.readLine();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
