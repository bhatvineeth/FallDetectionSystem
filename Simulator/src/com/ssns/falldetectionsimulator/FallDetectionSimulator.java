package com.ssns.falldetectionsimulator;

import java.io.*;
import java.util.*;

public class FallDetectionSimulator {

	static Map<String, Float[]> accMap = new HashMap<String, Float[]>();
	static Map<String, Float[]> gyroMap = new HashMap<String, Float[]>();

	static Float accDataset[][] = new Float[1000][4];
	static Float gyroDataset[][] = new Float[1000][4];
	private static float[] gyro = new float[3];
	private static float omegaMagnitude;
	public static final float EPSILON = 0.000000001f;
	private static boolean initState = true;
	private static float[] accMagOrientation = new float[3];
	private static float[] gyroMatrix = new float[9];

	private static final float NS2S = 1.0f / 1000.0f;

	static float accelerometerData[] = new float[3];
	static float gyroscopeData[] = new float[3];
	private static float[] accel = new float[3]; 
	private static float[] rotationMatrix = new float[9];
    private static float[] magnet = new float[3];
    private static double totalSumVector = 0.0;
    
	private static float[] fusedOrientation = new float[3];
	private static float[] gyroOrientation = new float[3];
	public static long startTimer = 0;

    private static double mLowerAccFallThreshold = 6.962721499999999; // 0.71g
    private static double mUpperAccFallThreshold = 19.122967499999998; // 1.95g
    private static double mAngularVelocityThreshold = 0.026529; // 1.52 deg / s
    private static double mTiltValue = 60; // 60 deg


    public static final float FILTER_COEFFICIENT = 0.98f;
	static private  float degreeFloat;
	static private  float degreeFloat2;
	private static long currentTime = 2318294309000L;
	private static long prevtimestamp;
	static FileWriter myWriter;

	static {
		try {
			myWriter = new FileWriter("/Users/sjathin/Documents/GitHub/HumanActivityRecognition/test.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//myWriter.close();
	    
	static int i = 0;

	public FallDetectionSimulator() throws IOException {
	}

	public static void main(String[] args) throws IOException {
		readDataSets();

		String nanoTime;
		while(true) {
			// 2328285952000 2328284010000
			nanoTime = String.valueOf(currentTime);
			if (currentTime >= 2328285952000L) {
				System.out.println("Completed");
				myWriter.write("Completed"+ "\n");
				myWriter.close();
				break;
			}
			if(accMap.containsKey(nanoTime)){
				accelerometerData[0] =  accMap.get(nanoTime)[0];
				accelerometerData[1] =  accMap.get(nanoTime)[1];
				accelerometerData[2] =  accMap.get(nanoTime)[2];

				System.arraycopy(accelerometerData, 0, accel, 0, 3);
				if (getRotationMatrix(rotationMatrix, null, accel, magnet)) {
					getOrientation(rotationMatrix, accMagOrientation);
				}
				totalSumVector = Math.sqrt(accel[0] * accel[0] + accel[1] * accel[1] + accel[2] * accel[2]);

				//System.out.println("TotalSumVector: " + totalSumVector);
				myWriter.write("TotalSumVector: " + totalSumVector + "\n");

			}
			if(gyroMap.containsKey(nanoTime)){
				gyroscopeData[0] =  gyroMap.get(nanoTime)[0];
				gyroscopeData[1] =  gyroMap.get(nanoTime)[1];
				gyroscopeData[2] =  gyroMap.get(nanoTime)[2];
				gyroFunction(gyroscopeData);
			}
			currentTime = currentTime + 1000;
			falldetection();

		}

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
            getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if (currentTime != 2318294309000L) {
            final float dT = (currentTime - prevtimestamp) * NS2S;
            System.arraycopy(event, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
		prevtimestamp = currentTime;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        getOrientation(gyroMatrix, gyroOrientation);
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
			//File fileAcc = new File("/Users/vineeth/Documents/HIS/Semester 2/SSNS/FallDetectionSystem/Documents/MobiFall_Dataset_v2.0/sub1/FALLS/BSC/BSC_acc_1_1.txt");
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
					accMap.put(line.split(",")[0], accDataset[i]);

					gyroDataset[i][0] = (float) Double.parseDouble(line2.split(",")[1]);
					gyroDataset[i][1] = (float) Double.parseDouble(line2.split(",")[2]);
					gyroDataset[i][2] = (float) Double.parseDouble(line2.split(",")[3]);
					gyroMap.put(line2.split(",")[0], gyroDataset[i]);
					//System.out.println(gyro[i][0]);
					//System.out.println(acc[i][0]);
					i++;
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
	
	
    public static void falldetection() throws IOException {

        float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
        fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
//        Log.d("X:", ""+fusedOrientation[0]);

        fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
//        Log.d("Y:", ""+fusedOrientation[1]);

        fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
//        Log.d("Z:", ""+fusedOrientation[2]);

		degreeFloat = (float) (fusedOrientation[1] * 180 / Math.PI);
		degreeFloat2 = (float) (fusedOrientation[2] * 180 / Math.PI);
		//System.out.println("degreeFloat:"+degreeFloat);
        //System.out.println("degreeFloat2:"+degreeFloat2);
        //System.out.println("mAngularVelocityThreshold:"+omegaMagnitude);
		myWriter.write("degreeFloat:"+degreeFloat+ "\n");


		//myWriter.write("degreeFloat2:"+degreeFloat2+ "\n");
		myWriter.write("mAngularVelocityThreshold:"+omegaMagnitude+ "\n");


        if( startTimer != 0 && ((System.currentTimeMillis() - startTimer)>=30000)){
            //send sms
            startTimer = 0;
            //String textMsg = "Hello, I have fallen down here-> " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(ActivityMonitoring.getLatitude()) + "," + String.valueOf(ActivityMonitoring.getLongitude()) + "need help immediately!!";
            String textMsg = "Sorry by mistake";
            //smsManager.sendTextMessage("015906196190", null, textMsg, null, null);
            System.out.println("SMS Sent");
			myWriter.write("SMS Sent"+ "\n");
        }

        //if (totalSumVector < mLowerAccFallThreshold){
            if (totalSumVector > mUpperAccFallThreshold) {
                if ( omegaMagnitude > mAngularVelocityThreshold) {
                    if (Math.abs(degreeFloat) > mTiltValue || Math.abs(degreeFloat2) > mTiltValue) {
                        if(startTimer == 0){
                            startTimer = System.currentTimeMillis();
                        }
                            System.out.println("Notification!!!");
							System.out.println("degreeFloat:"+degreeFloat);
							System.out.println("degreeFloat2:"+degreeFloat2);
							System.out.println("omegaMagnitude : " + mAngularVelocityThreshold);
							System.out.println("SMV : " + totalSumVector);
							myWriter.write("FALL !!!!! DANGER"+ "\n");
                          //  startAlert();
                           // System.out.printf("DANGER!!!", "User location at => " + "https://www.google.com/maps/search/?api=1&query=" + String.valueOf(FallDetectionSimulator.getLatitude()) + "," + String.valueOf(FallDetectionSimulator.getLongitude()));
                    }
                }
            }
        //}

        gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
        System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
    }

    /** Helper function to convert a rotation vector to a rotation matrix.
     *  Given a rotation vector (presumably from a ROTATION_VECTOR sensor), returns a
     *  9  or 16 element rotation matrix in the array R.  R must have length 9 or 16.
     *  If R.length == 9, the following matrix is returned:
     * <pre>
     *   /  R[ 0]   R[ 1]   R[ 2]   \
     *   |  R[ 3]   R[ 4]   R[ 5]   |
     *   \  R[ 6]   R[ 7]   R[ 8]   /
     *</pre>
     * If R.length == 16, the following matrix is returned:
     * <pre>
     *   /  R[ 0]   R[ 1]   R[ 2]   0  \
     *   |  R[ 4]   R[ 5]   R[ 6]   0  |
     *   |  R[ 8]   R[ 9]   R[10]   0  |
     *   \  0       0       0       1  /
     *</pre>
     *  @param rotationVector the rotation vector to convert
     *  @param R an array of floats in which to store the rotation matrix
     */
    public static void getRotationMatrixFromVector(float[] R, float[] rotationVector) {

        float q0;
        float q1 = rotationVector[0];
        float q2 = rotationVector[1];
        float q3 = rotationVector[2];

        if (rotationVector.length >= 4) {
            q0 = rotationVector[3];
        } else {
            q0 = 1 - q1 * q1 - q2 * q2 - q3 * q3;
            q0 = (q0 > 0) ? (float) Math.sqrt(q0) : 0;
        }

        float sq_q1 = 2 * q1 * q1;
        float sq_q2 = 2 * q2 * q2;
        float sq_q3 = 2 * q3 * q3;
        float q1_q2 = 2 * q1 * q2;
        float q3_q0 = 2 * q3 * q0;
        float q1_q3 = 2 * q1 * q3;
        float q2_q0 = 2 * q2 * q0;
        float q2_q3 = 2 * q2 * q3;
        float q1_q0 = 2 * q1 * q0;

        if (R.length == 9) {
            R[0] = 1 - sq_q2 - sq_q3;
            R[1] = q1_q2 - q3_q0;
            R[2] = q1_q3 + q2_q0;

            R[3] = q1_q2 + q3_q0;
            R[4] = 1 - sq_q1 - sq_q3;
            R[5] = q2_q3 - q1_q0;

            R[6] = q1_q3 - q2_q0;
            R[7] = q2_q3 + q1_q0;
            R[8] = 1 - sq_q1 - sq_q2;
        } else if (R.length == 16) {
            R[0] = 1 - sq_q2 - sq_q3;
            R[1] = q1_q2 - q3_q0;
            R[2] = q1_q3 + q2_q0;
            R[3] = 0.0f;

            R[4] = q1_q2 + q3_q0;
            R[5] = 1 - sq_q1 - sq_q3;
            R[6] = q2_q3 - q1_q0;
            R[7] = 0.0f;

            R[8] = q1_q3 - q2_q0;
            R[9] = q2_q3 + q1_q0;
            R[10] = 1 - sq_q1 - sq_q2;
            R[11] = 0.0f;

            R[12] = R[13] = R[14] = 0.0f;
            R[15] = 1.0f;
        }
    }

}
