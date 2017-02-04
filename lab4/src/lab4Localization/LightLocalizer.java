package lab4Localization;

import java.util.ArrayList;

import lejos.hardware.Sound;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class LightLocalizer {
	private Odometer odo;
	private SampleProvider colorSensor;
	private float[] colorData;	
	
	public static final float ROTATION_SPEED = 100;
	public static final double COLOR_SENSOR_OFFSET = 14;	// Distance between center of rotation and the color sensor in cm
	private static final double BLACK_RGB = 0.2;
	private static final int AXIS_CROSS_DELAY = 200, SENSOR_POLL_PERIOD = 10;
	
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
		
		// This code assumes wer're in the third quadrant
		while(angles.size() < 4) {
			if(getFilteredData() < BLACK_RGB) {
				Sound.beep();
				angles.add(odo.getAng());
				Delay.msDelay(AXIS_CROSS_DELAY);
			}
		}
		localizerLocomotor.halt();
		
		// Now let's do some math
		// Lets calculate our x position
		double x = (-COLOR_SENSOR_OFFSET) * Math.cos(Math.toRadians(Math.abs(angles.get(0) - angles.get(2)))/2);
		// Lets calculate our y position
		double y = (COLOR_SENSOR_OFFSET) * Math.cos(Math.toRadians(Math.abs(angles.get(1) - angles.get(3)))/2);
		// Let's calculate our heading correction
		//double headingCorrection = odo.getAng() + 270 - angles.get(0) + (Math.abs(angles.get(0) - angles.get(2)))/2;
		odo.setPosition(new double [] {x, y, 0.0}, new boolean [] {true, true, false});
	}
	
	// Takes 50ms to get one sample => 20 Hz polling rate
	public float getFilteredData() {
		float filteredData = 0;
		int NUMBER_OF_SAMPLES = 5;
		
		// Apply mean filtering
		for(int i = 0;i < NUMBER_OF_SAMPLES;++i) {
			colorSensor.fetchSample(colorData, 0);
			// Sum RGB components to get net RGB color value
			// Good enough to identify color intensity
			filteredData += colorData[0] + colorData[1] + colorData[2];
			Delay.msDelay(SENSOR_POLL_PERIOD);
		}
		
		filteredData = filteredData / 5;
		return filteredData;
	}

}
