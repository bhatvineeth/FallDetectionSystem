package com.ssns.falldetectionsimulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class FallDetectionSimulator {

	static double acc[][] = new double[1000][4];
	static double gyro[][] = new double[1000][4];
	
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
					acc[i][0] = Double.parseDouble(line.split(",")[1]);
					acc[i][1] = Double.parseDouble(line.split(",")[2]);
					acc[i][2] = Double.parseDouble(line.split(",")[3]);
					
					gyro[i][0] = Double.parseDouble(line2.split(",")[1]);
					gyro[i][1] = Double.parseDouble(line2.split(",")[2]);
					gyro[i][2] = Double.parseDouble(line2.split(",")[3]);
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
