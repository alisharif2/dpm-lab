package lab4Localization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import lejos.hardware.Sound;
import lejos.hardware.Sounds;
import lejos.robotics.SampleProvider;
import lejos.robotics.filter.MedianFilter;
import lejos.utility.Delay;

public class USLocalizer {
	public enum LocalizationType { FALLING_EDGE, RISING_EDGE }
	
	private final float ROTATION_SPEED = 100;
	private final int MIN_WALL_DIST = 45;
	private int filterControl = 0;
	private final int FILTER_OUT = 20;

	private Odometer odo;
	private SampleProvider usSensor;
	private float[] usData;
	private LocalizationType locType;
	
	public USLocalizer(Odometer odo,  SampleProvider usSensor, float[] usData, LocalizationType locType) {
		this.odo = odo;
		this.usSensor = usSensor;
		this.usData = usData;
		this.locType = locType;
	}
	
	public void doLocalization() {
		double alpha, beta;
		odo.getAng();
		
		// Initialize our locomotor
		Navigation localizerLocomotor = new Navigation(odo);
	
		if (locType == LocalizationType.FALLING_EDGE) {
			// rotate the robot until it sees no wall
			localizerLocomotor.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);	// TODO use the turnTo method instead of this garbage
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(true) {
				if(getFilteredData() < MIN_WALL_DIST) {
					alpha = odo.getAng();
					break;
				}
			}
		
			// switch direction and wait until it sees no wall
			localizerLocomotor.setSpeeds(-ROTATION_SPEED, ROTATION_SPEED);	// TODO use the turnTo method instead of this garbage
			Delay.msDelay(2000);
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(true) {
				if(getFilteredData() < MIN_WALL_DIST) {
					beta = odo.getAng();
					break;	
				}
			}
			localizerLocomotor.halt();
	
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			double headingCorrection = 0;
			if(alpha > beta) {
				headingCorrection = 225 - (alpha + beta)/2;
			} else {
				headingCorrection = 45 - (alpha + beta)/2;
			}
						
			// update the odometer position with our new heading(zero degrees)
			odo.setPosition(new double [] {0, 0, headingCorrection + beta - 90}, new boolean [] {false, false, true});
			localizerLocomotor.turnTo(0, true);
		} else {
			/*
			 * The robot should turn until it sees the wall, then look for the
			 * "rising edges:" the points where it no longer sees the wall.
			 * This is very similar to the FALLING_EDGE routine, but the robot
			 * will face toward the wall for most of it.
			 */
			
			//
			// FILL THIS IN
			//
			
			// rotate the robot until it sees no wall
			localizerLocomotor.setSpeeds(ROTATION_SPEED, -ROTATION_SPEED);	// TODO use the turnTo method instead of this garbage
			
			// keep rotating until the robot sees a wall, then latch the angle
			while(true) {
				if(getFilteredData() < MIN_WALL_DIST) {
					alpha = odo.getAng();
					break;
				}
			}
		
			// switch direction and wait until it sees no wall
			Delay.msDelay(1000);
			
			// keep rotating until the robot no longer sees a wall, then latch the angle
			while(true) {
				if(getFilteredData() > MIN_WALL_DIST) {
					beta = odo.getAng();
					break;
				}
			}
			localizerLocomotor.halt();
	
			// angleA is clockwise from angleB, so assume the average of the
			// angles to the right of angleB is 45 degrees past 'north'
			double headingCorrection = 0;
			if(alpha > beta) {
				headingCorrection = 225 - (alpha + beta)/2;
			} else {
				headingCorrection = 45 - (alpha + beta)/2;
			}
			
			// update the odometer position with our new heading(zero degrees)
			int SYSTEMATIC_ERR = 11;
			odo.setPosition(new double [] {0.0, 0.0, headingCorrection + beta - 90 - SYSTEMATIC_ERR}, new boolean [] {false, false, true});
			localizerLocomotor.turnTo(0, true);
		}
	}
	
	// Apply median filtering
	private float getFilteredData() {
		// Don't use even numbers
		final int NUMBER_OF_SAMPLES = 5;
		float filteredDistance, distance;
		ArrayList<Float> samples = new ArrayList<Float>();
		
		for(int i = 0;i < NUMBER_OF_SAMPLES;++i) {
			usSensor.fetchSample(usData, 0);
			distance = usData[0];
			
			// I stole this filter from lab1
			filteredDistance = distance;
			if (distance >= 255 && filterControl < FILTER_OUT) {
				// bad value, do not set the distance var, however do increment the
				// filter value
				filterControl++;
			} else if (distance >= 255) {
				// We have repeated large values, so there must actually be nothing
				// there: leave the distance alone
				filteredDistance = distance;
			} else {
				// distance went below 255: reset filter and leave
				// distance alone.
				filterControl = 0;
				filteredDistance = distance;
			}
			samples.add(filteredDistance);
			// Use an extremely short delay to make sure we get useful sensor readings
			Delay.msDelay(10);
		}
		
		// Sort and pick middle value
		Collections.sort(samples);
		filteredDistance = samples.get((int)(NUMBER_OF_SAMPLES/2));
		
		// Convert to centimeters
		return filteredDistance * 100;
	}

}
