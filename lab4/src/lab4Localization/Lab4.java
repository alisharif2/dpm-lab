package lab4Localization;

import java.util.List;
import java.util.Stack;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class Lab4 {
	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	// Ultrasonic sensor connected to port 1
	// Color sensor connected to port 2
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final EV3UltrasonicSensor us = new EV3UltrasonicSensor(LocalEV3.get().getPort("S1"));
	
	// Globally accessible constants
	public static final int bandCenter = 30;			// Offset from the wall (cm)
	public static final int bandWidth = 8;				// Width of dead band (cm)
	public static final int motorLow = 150;				// Speed of slower rotating wheel (deg/sec)
	public static final int motorHigh = 300;			// Speed of the faster rotating wheel (deg/seec)
	public static final double WHEEL_RADIUS = 2.1;		// Radius of the wheels
	public static final double TRACK = 11.8;			// Wheel center to wheel center distance
	
	// Create our globally accessible odometer
	public static Odometer odometer;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
