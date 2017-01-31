package lab3Navigator;

import java.util.List;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class SimpleNavigator extends Thread {
	// Define and load important constants
	private static final int ROTATE_SPEED = 150;
	private static final int FORWARD_SPEED = 250;
	protected static final long NAVIGATION_PERIOD = 50;
	private double leftRadius = Lab3.WHEEL_RADIUS;
	private double rightRadius = Lab3.WHEEL_RADIUS;
	private double track = Lab3.TRACK;

	// Objects to access the robot's components
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private Odometer odo;

	// Used to check if the robot is currently navigating
	public static boolean navigating = false;
	public boolean isAvoiding;

	// The list of coordinates provided to navigate to
	protected List<Point> coordinates;

	// Get relevant information from main thread
	SimpleNavigator() {
		this.leftMotor = Lab3.leftMotor;
		this.rightMotor = Lab3.rightMotor;
		this.odo = Lab3.odometer;
		this.coordinates = Lab3.coordinates;
		this.isAvoiding = false;
	}

	public void run() {
		long updateStart, updateEnd;

		// Temporary variable to store successive coordinates in
		Point target;
		// Alternative to while loop
		// Only iterate if points are available
		// This is more flexible
		while (!coordinates.isEmpty()) {
			if(isAvoiding) continue;
			updateStart = System.currentTimeMillis();
			// Load the target coordinate
			target = coordinates.remove(0);
			// Call travelTo to go there
			travelTo(target.x, target.y);

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

	// This method does a bunch of math and moves the robot
	// This method is blocking
	public void travelTo(double x, double y) {
		// Set the navigating flag
		navigating = true;

		// Calculate the new heading using getAngle
		double newHeading = getAngle(x, y);
		// Turn to the new heading
		turnTo(newHeading);

		// Get the distance between the points
		double distance = Math.sqrt(Math.pow(odo.getX() - x, 2) + Math.pow(odo.getY() - y, 2));

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		leftMotor.rotate(convertDistance(leftRadius, distance), true);
		rightMotor.rotate(convertDistance(rightRadius, distance), false);

		// Update the navigating flag because we're done
		navigating = false;
	}

	// Turn by a certain amount of radians
	private void turnTo(double theta) {
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		leftMotor.rotate(-convertAngle(leftRadius, track, Math.toDegrees(theta)), true);
		rightMotor.rotate(convertAngle(rightRadius, track, Math.toDegrees(theta)), false);
	}

	// Get angle between robot's direction vector and target point's relative
	// position vector
	private double getAngle(double x, double y) {
		double heading = odo.getTheta();
		double theta_d = Math.atan2(y - odo.getY(), x - odo.getX()); // returns
		// angle
		double err = theta_d - heading;
		if (err > Math.PI)
			return err - (2 * Math.PI);
		if (err < -Math.PI)
			return err + (2 * Math.PI);
		return err;
	}

	boolean isNavigating() {
		return navigating;
	}

	// Conversion formulae taken from lab 2
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double track, double angle) {
		return convertDistance(radius, Math.PI * track * angle / 360.0);
	}

	public void halt() {
		if(!this.isAvoiding) {
			leftMotor.stop();
			rightMotor.stop();
		}
		this.isAvoiding = true;
	}
	
	public void go() {
		this.isAvoiding = false;
	}
}
