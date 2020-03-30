package fr.mercury.nucleus.utils;

/**
 * <code>Timer</code> is an agnostic-resolution interface to create timer based on different time units.
 * 
 * @author GnosticOccultist
 */
public interface Timer {
	
	/**
	 * Return the current time in seconds per frame of the <code>Timer</code>.
	 * 
	 * @return The current time in seconds per frame.
	 */
	float getTimePerFrame();
	
	/**
	 * Return the frame the current frame-rate or frame per second of the <code>Timer</code>.
	 * 
	 * @return The current frame-rate in frames per second.
	 */
	float getFrameRate();
	
	/**
	 * Return the speed at which the <code>Timer</code> should run. It can be applied to {@link #getTimePerFrame()}
	 * in order to accelerate time dependent actions in the game-loop.
	 * 
	 * @return The speed of the timer.
	 */
	float getSpeed();
	
	/**
	 * Return whether the <code>Timer</code> is paused. If this is the case, then {@link #update()} shouldn't perform.
	 * 
	 * @return Whether the timer is paused.
	 */
	boolean isPaused();
	
	/**
	 * Return the amount of time since the timer has started to run or since the last reset in the
	 * <code>Timer</code> units.
	 * 
	 * @return The amount of timer since the timer has (re)started in timer units.
	 */
	long getTime();
	
	/**
	 * Return the resolution of the <code>Timer</code>, converting from seconds to its units.
	 * 
	 * @return The resolution used by the timer (&gt;0).
	 */
	long getResolution();
}
