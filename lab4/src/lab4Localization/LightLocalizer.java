package lab4Localization;

import java.util.ArrayList;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	
	public static float ROTATION_SPEED = 100;
	public static double COLOR_SENSOR_OFFSET = 1.4;
	
	public LightLocalizer(Odometer odo, SampleProvider colorSensor, float[] colorData) {
		this.odo = odo;
		this.colorSensor = colorSensor;
		this.colorData = colorData;
	}
	
	public void doLocalization() {
		// drive to location listed in tutorial
		// start rotating and clock all 4 gridlines
		// do trig to compute (0,0) and 0 degrees
		// when done travel to (0,0) and turn to 0 degrees
		
		Navigation localizerLocomotor = new Navigation(this.odo);
		
		/* Store the angles that each axis is crossed at in an array
		 * [0] -> y-
		 * [1] -> x+
		 * [2] -> y+
		 * [3] -> x-
		 */
		ArrayList<Double> angles = new ArrayList<Double>();
		
		// Start the robot rotating
		localizerLocomotor.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);
		
		while(angles.size() < 4) {
			if(getFilteredData() < 0.2) {
				Sound.beep();
				angles.add(odo.getAng());
				Delay.msDelay(200);
			}
		}
		localizerLocomotor.halt();
		
		// Now let's do some math
		// Lets calculate our x position
		double x = (-COLOR_SENSOR_OFFSET) * Math.cos(Math.toRadians(Math.abs(angles.get(0) - angles.get(2)))/2);
		// Lets calculate our y position
		double y = (-COLOR_SENSOR_OFFSET) * Math.cos(Math.toRadians(Math.abs(angles.get(1) - angles.get(3)))/2);			
		odo.setPosition(new double [] {x, y, 0.0}, new boolean [] {true, true, false});
		
		localizerLocomotor.turnTo(0, true);
		localizerLocomotor.travelTo(0, 0);
	}
	
	// Takes 50ms to get one sample => 20 Hz polling rate
	public float getFilteredData() {
		float filteredData = 0;
		int NUMBER_OF_SAMPLES = 5;
		
		for(int i = 0;i < NUMBER_OF_SAMPLES;++i) {
			colorSensor.fetchSample(colorData, 0);
			filteredData += colorData[0] + colorData[1] + colorData[2];
			Delay.msDelay(10);
		}
		
		filteredData = filteredData / 5;
		return filteredData;
	}

}
