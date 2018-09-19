package fr.mercury.nucleus.utils;

/**
 * <code>SpeedableNanoTimer</code> is an extension of a <code>NanoTimer</code> with
 * a modifiable speed and a paused flag.
 * 
 * @author GnosticOccultist
 */
public class SpeedableNanoTimer extends NanoTimer {
	
	/**
	 * The speed of the timer.
	 */
	private float speed = 1f;
	/**
	 * Whether the timer is paused.
	 */
	private boolean paused = false;
	
	/**
	 * Update the timer by changing its values.
	 */
	@Override
	public void update() {
		if(isPaused()) {
			return;
		}
		
		super.update();
	}
	
	/**
	 * Return the speed of the timer.
	 * 
	 * @return The speed of the timer.
	 */
	public float getSpeed() {
		return speed;
	}
	
	/**
	 * Set the speed of the timer.
	 * 
	 * @param speed The speed of the timer.
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	/**
	 * Set whether the timer should be paused.
	 * 
	 * @param paused Whether the timer is paused.
	 */
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	/**
	 * @return Whether the timer is paused or with a speed of 0.
	 */
	public boolean isPaused() {
		return paused || speed == 0;
	}
}
