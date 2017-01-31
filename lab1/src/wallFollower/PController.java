package wallFollower;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {

	private final int bandCenter, bandwidth;
	private final int FILTER_OUT = 20;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;

	private int max_speed = 300, min_speed = 175;
	private int avg_speed = (max_speed + min_speed)/2;

	private int left_slope = 0, right_slope = 0, right_c = 0;

	private int left_speed = 0, right_speed = 0;

	public PController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int bandCenter,
			int bandwidth) {
		// Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		filterControl = 0;

		left_slope = max_speed / (bandCenter + bandwidth);
		right_slope = (min_speed - max_speed) / (2 * bandwidth);
		right_c = max_speed + (-right_slope) * (bandCenter - bandwidth);
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

	@Override
	public void processUSData(int distance) {
		// TODO Auto-generated method stub
		this.distance = distance;
		// rudimentary filter - toss out invalid samples corresponding to null
		// signal.
		// (n.b. this was not included in the Bang-bang controller, but easily
		// could have).
		//
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the s_distance var, however do increment
			// the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the s_distance alone
			this.distance = distance;
		} else {
			// s_distance went below 255: reset filter and leave
			// s_distance alone.
			filterControl = 0;
			this.distance = distance;
		}
/*
		right_speed = ((avg_speed)/bandwidth)*(distance - bandCenter) + avg_speed;
		left_speed = -((avg_speed)/bandwidth)*(distance - bandCenter) + avg_speed;
		
		if(Math.abs(left_speed) >= max_speed) left_speed = max_speed;
		if(Math.abs(right_speed) >= max_speed) right_speed = max_speed;
		*/
		
		if (this.distance > bandCenter + bandwidth) {
			left_speed = (max_speed);
			right_speed = (min_speed);
		} else if (this.distance <= bandCenter - bandwidth) {
			left_speed = -(max_speed);
			right_speed = (max_speed);
		} else {
			left_speed = (left_slope * this.distance);
			right_speed = (right_slope * this.distance + right_c);
		}

		leftMotor.setSpeed(Math.abs(left_speed));
		rightMotor.setSpeed(Math.abs(right_speed));

		if (left_speed >= 0)
			leftMotor.forward();
		else
			leftMotor.backward();

		if (right_speed >= 0)
			rightMotor.forward();
		else
			leftMotor.backward();

	}

}
