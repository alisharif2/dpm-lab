package lab3Navigator;

import java.util.List;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class AdvancedNavigator extends SimpleNavigator {
	
	private boolean isClear = true;

	AdvancedNavigator(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, EV3UltrasonicSensor us,
			Odometer odo, List<Point> coordinates) {
		
		super();
	}
	
	
	public void run() {
		long updateStart, updateEnd;

		// Temporary variable to store successive coordinates in
		Point target;
		// Alternative to while loop
		// Only iterate if points are available
		// This is more flexible
		
		while (true) {
			updateStart = System.currentTimeMillis();
			
			// Load the target coordinate
			target = coordinates.remove(0);
			// Call travelTo to go there
			travelTo(target.x, target.y);

			if(!isClear) break;
			
			// Ensure the navigation only runs for a period of time
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < NAVIGATION_PERIOD) {
				try {
					Thread.sleep(NAVIGATION_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// Let's hope that the thread isn't interrupted
				}
			}
		}
	}
}
