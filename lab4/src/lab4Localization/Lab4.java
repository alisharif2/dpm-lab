package lab4Localization;

import java.io.File;

import lejos.hardware.*;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.*;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

public class Lab4 {

	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor port connected to input S1
	// Color sensor port connected to input S2
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final Port usPort = LocalEV3.get().getPort("S3");		
	private static final Port colorPort = LocalEV3.get().getPort("S4");		

	
	public static void main(String[] args) {
		
		//Setup ultrasonic sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		@SuppressWarnings("resource")							    	// Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usValue = usSensor.getMode("Distance");			// colorValue provides samples from this instance
		float[] usData = new float[usValue.sampleSize()];				// colorData is the buffer in which data are returned
		
		//Setup color sensor
		// 1. Create a port object attached to a physical port (done above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		SensorModes colorSensor = new EV3ColorSensor(colorPort);
		SampleProvider colorValue = colorSensor.getMode("RGB");			// colorValue provides samples from this instance
		float[] colorData = new float[colorValue.sampleSize()];			// colorData is the buffer in which data are returned
				
		// setup the odometer and display
		Odometer odo = new Odometer(leftMotor, rightMotor, 30, true);
		LCDInfo lcd = new LCDInfo(odo);
		Navigation nav = new Navigation(odo);

		// perform the ultrasonic localization
		USLocalizer usl = new USLocalizer(odo, usValue, usData, USLocalizer.LocalizationType.RISING_EDGE);
		LightLocalizer lsl = new LightLocalizer(odo, colorValue, colorData);

		/*
		 * The basic process is to figure out our heading and then move towards actual (0, 0)
		 * Calculate our actual position and run US localization again to get the heading
		 * The US localizer is exceptionally accurate at heading correction due to its filter
		 */
		usl.doLocalization();
		Button.waitForAnyPress();
		nav.travelTo(9, -9); // Arbitrarily chose values to move towards
		
		// perform the light sensor localization and reorient
		lsl.doLocalization();
		usl.doLocalization(); // Correct heading error introduced by light sensor localization
		
		// Finally move towards the actual origin and turn to zero
		nav.travelTo(0, 0);
		nav.turnTo(0, true);
		
		// Play completion music because we're champions :)
		//Sound.playSample(new File("/home/root/sounds/music.wav"));

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);	
		
	}

}
