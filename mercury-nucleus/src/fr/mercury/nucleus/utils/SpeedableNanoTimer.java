package fr.mercury.nucleus.utils;

/**
 * <code>SpeedableNanoTimer</code> is an extension of a <code>NanoTimer</code> with
 * a modifiable speed and a paused flag.
 * 
 * @author GnosticOccultist
 * 
 * @see NanoTimer
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
	 * Return the speed at which the <code>SpeedableNanoTimer</code> should run. It can be applied to 
	 * {@link #getTimePerFrame()} in order to accelerate time dependent actions in the game-loop.
	 * 
	 * @return The speed of the timer.
	 */
	public float getSpeed() {
		return speed;
	}
	
	/**
	 * Sets the desired speed at which the <code>SpeedableNanoTimer</code> should run. It can be applied to 
	 * {@link #getTimePerFrame()} in order to accelerate time dependent actions in the game-loop.
	 * <p>
	 * Note that a speed of 0 will pause the timer.
	 * 
	 * @param speed The desired speed of the timer.
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	/**
	 * Sets whether the <code>SpeedableNanoTimer</code> should be paused. If this is the case, then {@link #update()} 
	 * shouldn't perform. The timer can also be paused by setting its speed to 0.
	 * 
	 * @param paused Whether the timer should be paused.
	 */
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	/**
	 * Return whether the <code>SpeedableNanoTimer</code> is paused. If this is the case, then {@link #update()} 
	 * shouldn't perform. The timer can also be paused by setting its speed to 0.
	 * 
	 * @return Whether the timer is paused or the speed is equal to 0.
	 */
	public boolean isPaused() {
		return paused || speed == 0;
	}
}
