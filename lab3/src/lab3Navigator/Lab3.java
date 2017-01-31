// Lab2.java

package lab3Navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab3 {
	
	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final EV3UltrasonicSensor us = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
	
	// Depending on which option on the menu is selected the coordinates
	public static List<Point> coordinates = new Stack<Point>();
	
	// Globally accessible constants
	public static final int bandCenter = 30;			// Offset from the wall (cm)
	public static final int bandWidth = 8;				// Width of dead band (cm)
	public static final int motorLow = 150;			// Speed of slower rotating wheel (deg/sec)
	public static final int motorHigh = 300;			// Speed of the faster rotating wheel (deg/seec)
	public static final double WHEEL_RADIUS = 2.1;		// Radius of the wheels
	public static final double TRACK = 11.8;			// Wheel center to wheel center distance
	
	public static Odometer odometer;

	public static void main(String[] args) {
		int buttonChoice;

		// some objects that need to be instantiated
		final TextLCD t = LocalEV3.get().getTextLCD();
		odometer = new Odometer(leftMotor, rightMotor);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer,t);
	
		// Instantiate two navigators
		/*
		 * The simple navigator cannot avoid obstacles and just simply moves from coordinate to coordinate
		 * 
		 * The advanced navigator moves from coordinate to coordinate, however, if an obstacle is present, the
		 * navigator will attempt to move around it
		 */
		final SimpleNavigator simpleNavigator = new SimpleNavigator();
		
		
		// Setup controller objects
		
		BangBangController bangbang = new BangBangController(leftMotor, rightMotor, bandCenter,
															 bandWidth, motorLow, motorHigh);
		
		// Setup ultrasonic sensor
		// Note that the EV3 version of leJOS handles sensors a bit differently.
		// There are 4 steps involved:
		// 1. Create a port object attached to a physical port (done already above)
		// 2. Create a sensor instance and attach to port
		// 3. Create a sample provider instance for the above and initialize operating mode
		// 4. Create a buffer for the sensor data
		SampleProvider usDistance = us.getMode("Distance");			// usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()];		// usData is the buffer in which data are returned
		
		// Setup Ultrasonic Poller									// This thread samples the US and invokes
		UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData, bangbang, simpleNavigator);			// the selected controller on each cycle
		
		do {
			// clear the display
			t.clear();

			// Ask the user which controller they would like to use
			// The right controller is for the first part of the lab
			// The left controller is for the second part of the lab
			t.drawString("< Left | Right >   ", 0, 0);
			t.drawString("       |           ", 0, 1);
			t.drawString(" Avoid | Navigate  ", 0, 2);
			t.drawString(" block | list of   ", 0, 3);
			t.drawString("       | points    ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		
		
		if (buttonChoice == Button.ID_LEFT) {
			// Hardcode coordinates here for block avoidance challenge
			coordinates.add(new Point(0, 60));
			coordinates.add(new Point(60, 0));

			// Start threads
			odometer.start();
			odometryDisplay.start();
			simpleNavigator.start();
			usPoller.start();
			
		} else {
			// Hardcode coordinates here for navigation challenge
			coordinates.add(new Point(60, 30));
			coordinates.add(new Point(30, 30));
			coordinates.add(new Point(30, 60));
			coordinates.add(new Point(60, 0));
			
			// Start threads
			odometer.start();
			odometryDisplay.start();
			simpleNavigator.start();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
