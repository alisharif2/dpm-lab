package wallFollower;
import lejos.hardware.motor.*;
import lejos.utility.Delay;

public class BangBangController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorLow, motorHigh, FILTER_OUT = 20;
	private int distance, filterControl = 0;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	private int left_speed = 0, right_speed = 0;
	
	static boolean frontIsClear = true;
	
	public BangBangController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int bandCenter,
							  int bandwidth, int motorLow, int motorHigh) {
		//Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.rightMotor = rightMotor;
		this.leftMotor = leftMotor;
	}
	
	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		// TODO: process a movement based on the us distance passed in (BANG-BANG style)
		// Rudimentary filter copied from PController.java
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}
		
		int error = Math.abs(this.distance - bandCenter);
		
		left_speed = motorHigh;
		right_speed = motorHigh;
		
		// Inside allowed area
		if(error < bandwidth) {
			leftMotor.setSpeed(motorHigh);
			rightMotor.setSpeed(motorHigh);
			leftMotor.forward();
			rightMotor.forward();
		}
		else if(this.distance > bandCenter + bandwidth) {
			leftMotor.setSpeed(motorHigh);
			rightMotor.setSpeed(motorLow);
			leftMotor.forward();
			rightMotor.forward();
		}
		// too close to the wall
		else {
			rightMotor.setSpeed(motorHigh);
			leftMotor.setSpeed(motorHigh);
			rightMotor.forward();
			leftMotor.backward();
		}

	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}

	@Override
	public EV3LargeRegulatedMotor getLeftMotor() {
		// TODO Auto-generated method stub
		return leftMotor;
	}

	@Override
	public EV3LargeRegulatedMotor getRightMotor() {
		// TODO Auto-generated method stub
		return rightMotor;
	}

}
