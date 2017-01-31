package lab3Navigator;
import lejos.hardware.motor.*;

public interface UltrasonicController {
	
	public int readUSDistance();
	
	public EV3LargeRegulatedMotor getLeftMotor();
	
	public EV3LargeRegulatedMotor getRightMotor();

	public void processUSData(int distance);

}
