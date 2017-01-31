package lab3Navigator;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

//
//  Control of the wall follower is applied periodically by the 
//  UltrasonicPoller thread.  The while loop at the bottom executes
//  in a loop.  Assuming that the us.fetchSample, and cont.processUSData
//  methods operate in about 20mS, and that the thread sleeps for
//  50 mS at the end of each loop, then one cycle through the loop
//  is approximately 70 mS.  This corresponds to a sampling rate
//  of 1/70mS or about 14 Hz.
//

public class UltrasonicPoller extends Thread{
	private SampleProvider us;
	private UltrasonicController cont;
	private float[] usData;
	private SimpleNavigator simpleNavigator;
	public Point target;
	public int distance;
	
	public UltrasonicPoller(SampleProvider us, float[] usData, UltrasonicController cont, SimpleNavigator simpleNavigator) {
		this.us = us;
		this.cont = cont;
		this.usData = usData;
		this.simpleNavigator = simpleNavigator;
	}

//  Sensors now return floats using a uniform protocol.
//  Need to convert US result to an integer [0,255]
	public void run() {

		while (true) {
			us.fetchSample(usData,0);							// acquire data
			distance = (int)(usData[0]*100.0);					// extract from buffer, cast to int
			if(distance < 30 && !simpleNavigator.isAvoiding) {
				simpleNavigator.halt();
				(new Thread() {
					public void run() {
						Delay.msDelay(3);
						simpleNavigator.go();
					}
				}).start();
			}
			if(simpleNavigator.isAvoiding) cont.processUSData(distance, simpleNavigator);
			try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
		}
	}

}
