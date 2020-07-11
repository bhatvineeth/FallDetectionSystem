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
	private static float[] gyro = new float[3];
	private static float omegaMagnitude;
	public static final float EPSILON = 0.000000001f;
	private static boolean initState = true;
	private static float[] accMagOrientation = new float[3];
	private static float[] gyroMatrix = new float[9];
	private static float timestamp;
	private static final float NS2S = 1.0f / 1000000000.0f;

	static float accelerometerData[] = new float[3];
	static float gyroscopeData[] = new float[3];
	private static float[] accel = new float[3]; 
	private static float[] rotationMatrix = new float[9];
    private static float[] magnet = new float[3];
    private static double totalSumVector = 0.0;
    
	private float[] fusedOrientation = new float[3];
	private static float[] gyroOrientation = new float[3];
	public static long startTimer = 0;
	private static double totalSumVector = 0.0; 
	    
	static int i = 0;
	
	public static void main(String[] args) {
		readDataSets();
		TimerTask task = new TimerTask() {
			public void run() {

				accelerometerData[0] = accDataset[i][0];
				accelerometerData[1] = accDataset[i][1];
				accelerometerData[2] = accDataset[i][2];

				gyroscopeData[0] = gyroDataset[i][0];
				gyroscopeData[1] = gyroDataset[i][1];
				gyroscopeData[2] = gyroDataset[i][2];

				System.arraycopy(accelerometerData, 0, accel, 0, 3);
				if (getRotationMatrix(rotationMatrix, null, accel, magnet)) {
					getOrientation(rotationMatrix, accMagOrientation);
				}
				totalSumVector = Math.sqrt(accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2]);
				if (totalSumVector > 15) {
					//Log.d("Activity Monitoring","mTextSensorTotalSumVector: " + totalSumVector);
				}
				gyroFunction(gyroscopeData);

				}
	    };
	    Timer timer = new Timer("Timer");
	    
	    long delay = 0;
	    timer.schedule(task, delay, 10);

	}
	

    public static void gyroFunction(float event[]) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if (initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            //TODO: getOrientation(initMatrix, test);
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
        //TODO: getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        //TODO: getOrientation(gyroMatrix, gyroOrientation);
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

    private static void getRotationVectorFromGyro(float[] gyroValues,
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

	public static boolean getRotationMatrix(float[] R, float[] I,
											float[] gravity, float[] geomagnetic) {
		// TODO: move this to native code for efficiency
		float Ax = gravity[0];
		float Ay = gravity[1];
		float Az = gravity[2];

		final float normsqA = (Ax * Ax + Ay * Ay + Az * Az);
		final float g = 9.81f;
		final float freeFallGravitySquared = 0.01f * g * g;
		if (normsqA < freeFallGravitySquared) {
			// gravity less than 10% of normal value
			return false;
		}

		final float Ex = geomagnetic[0];
		final float Ey = geomagnetic[1];
		final float Ez = geomagnetic[2];
		float Hx = Ey * Az - Ez * Ay;
		float Hy = Ez * Ax - Ex * Az;
		float Hz = Ex * Ay - Ey * Ax;
		final float normH = (float) Math.sqrt(Hx * Hx + Hy * Hy + Hz * Hz);

		if (normH < 0.1f) {
			// device is close to free fall (or in space?), or close to
			// magnetic north pole. Typical values are  > 100.
			return false;
		}
		final float invH = 1.0f / normH;
		Hx *= invH;
		Hy *= invH;
		Hz *= invH;
		final float invA = 1.0f / (float) Math.sqrt(Ax * Ax + Ay * Ay + Az * Az);
		Ax *= invA;
		Ay *= invA;
		Az *= invA;
		final float Mx = Ay * Hz - Az * Hy;
		final float My = Az * Hx - Ax * Hz;
		final float Mz = Ax * Hy - Ay * Hx;
		if (R != null) {
			if (R.length == 9) {
				R[0] = Hx;     R[1] = Hy;     R[2] = Hz;
				R[3] = Mx;     R[4] = My;     R[5] = Mz;
				R[6] = Ax;     R[7] = Ay;     R[8] = Az;
			} else if (R.length == 16) {
				R[0]  = Hx;    R[1]  = Hy;    R[2]  = Hz;   R[3]  = 0;
				R[4]  = Mx;    R[5]  = My;    R[6]  = Mz;   R[7]  = 0;
				R[8]  = Ax;    R[9]  = Ay;    R[10] = Az;   R[11] = 0;
				R[12] = 0;     R[13] = 0;     R[14] = 0;    R[15] = 1;
			}
		}
		if (I != null) {
			// compute the inclination matrix by projecting the geomagnetic
			// vector onto the Z (gravity) and X (horizontal component
			// of geomagnetic vector) axes.
			final float invE = 1.0f / (float) Math.sqrt(Ex * Ex + Ey * Ey + Ez * Ez);
			final float c = (Ex * Mx + Ey * My + Ez * Mz) * invE;
			final float s = (Ex * Ax + Ey * Ay + Ez * Az) * invE;
			if (I.length == 9) {
				I[0] = 1;     I[1] = 0;     I[2] = 0;
				I[3] = 0;     I[4] = c;     I[5] = s;
				I[6] = 0;     I[7] = -s;     I[8] = c;
			} else if (I.length == 16) {
				I[0] = 1;     I[1] = 0;     I[2] = 0;
				I[4] = 0;     I[5] = c;     I[6] = s;
				I[8] = 0;     I[9] = -s;     I[10] = c;
				I[3] = I[7] = I[11] = I[12] = I[13] = I[14] = 0;
				I[15] = 1;
			}
		}
		return true;
	}

	public static float[] getOrientation(float[] R, float[] values) {
		/*
		 * 4x4 (length=16) case:
		 *   /  R[ 0]   R[ 1]   R[ 2]   0  \
		 *   |  R[ 4]   R[ 5]   R[ 6]   0  |
		 *   |  R[ 8]   R[ 9]   R[10]   0  |
		 *   \      0       0       0   1  /
		 *
		 * 3x3 (length=9) case:
		 *   /  R[ 0]   R[ 1]   R[ 2]  \
		 *   |  R[ 3]   R[ 4]   R[ 5]  |
		 *   \  R[ 6]   R[ 7]   R[ 8]  /
		 *
		 */
		if (R.length == 9) {
			values[0] = (float) Math.atan2(R[1], R[4]);
			values[1] = (float) Math.asin(-R[7]);
			values[2] = (float) Math.atan2(-R[6], R[8]);
		} else {
			values[0] = (float) Math.atan2(R[1], R[5]);
			values[1] = (float) Math.asin(-R[9]);
			values[2] = (float) Math.atan2(-R[8], R[10]);
		}

		return values;
	}
	
	
	public static float getOmegaMagnitude() {
        return omegaMagnitude;
    }
    
       public class FallDetection extends TimerTask {
        //private double mLowerAccFallThreshold = 6.962721499999999; // 0.71g
        //private double mUpperAccFallThreshold = 19.122967499999998; // 1.95g
        //private double mAngularVelocityThreshold = 0.026529; // 1.52 deg / s
        //private double mTiltValue = 60; // 60 deg
        private double mLowerAccFallThreshold = 20; // 0.71g
        private double mUpperAccFallThreshold = 2; // 1.95g
        private double mAngularVelocityThreshold = 0.0001; // 1.52 deg / s
        private double mTiltValue = 1; // 60 deg
        private double mTilt;
        private double mDelay;
        private boolean mUserConfirmation;

        public static final float FILTER_COEFFICIENT = 0.98f;
        private  float degreeFloat;
        private  float degreeFloat2;


    
    public void falldetection() throws InterruptedException {

        float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
        fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
//        Log.d("X:", ""+fusedOrientation[0]);

        fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
//        Log.d("Y:", ""+fusedOrientation[1]);

        fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
//        Log.d("Z:", ""+fusedOrientation[2]);

        degreeFloat = (float) (fusedOrientation[1] * 180 / Math.PI);
        degreeFloat2 = (float) (fusedOrientation[2] * 180 / Math.PI);
        System.out.printf("degreeFloat:", ""+degreeFloat);
        System.out.printf("degreeFloat2:", ""+degreeFloat2);
        System.out.printf("mAngularVelocityThreshold:", ""+FallDetectionSimulator.getOmegaMagnitude());


        if( startTimer != 0 && ((System.currentTimeMillis() - startTimer)>=30000)){
            //send sms
            startTimer = 0;
            //String textMsg = "Hello, I have fallen down here-> " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(ActivityMonitoring.getLatitude()) + "," + String.valueOf(ActivityMonitoring.getLongitude()) + "need help immediately!!";
            String textMsg = "Sorry by mistake";
            //smsManager.sendTextMessage("015906196190", null, textMsg, null, null);
            System.out.printf("SMS!!!", "SMS Sent");
        }
        if (totalSumVector < mLowerAccFallThreshold){
            if (totalSumVector > mUpperAccFallThreshold) {
                if ( omegaMagnitude > mAngularVelocityThreshold) {
                    if (degreeFloat > mTiltValue || degreeFloat2 > mTiltValue) {
                        if(startTimer == 0){
                            startTimer = System.currentTimeMillis();
                        }
                            System.out.printf("Notification!!!", "Notification Sent");
                          //  startAlert();
                           // System.out.printf("DANGER!!!", "User location at => " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(FallDetectionSimulator.getLatitude()) + "," + String.valueOf(FallDetectionSimulator.getLongitude()));
                    }
                }
            }
        }

        gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
        System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
    }

}