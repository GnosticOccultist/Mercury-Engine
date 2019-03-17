package fr.mercury.nucleus.utils;

/**
 * <code>NanoTimer</code> is a timer using the function {@link System#nanoTime()} 
 * to calculate the time.
 * 
 * @author GnosticOccultist
 */
public class NanoTimer {

	protected static final long TIMER_RESOLUTION = 1_000_000_000L;
	
	/**
	 * The time at the start.
	 */
	protected long startTime;
	/**
	 * The previously updated time.
	 */
	protected long previousTime;
	/**
	 * The current time per frame.
	 */
	protected float tpf;
	/**
	 * The current frame-rate (frame per second).
	 */
	protected float fps;
	
	/**
	 * Instantiates a new <code>NanoTimer</code> and starting it by setting
	 * its starting time to {@link System#nanoTime()}.
	 */
	public NanoTimer() {
		startTime = System.nanoTime();
	}
	
	/**
	 * Update the timer by changing its values.
	 */
	public void update() {
		tpf = (getTime() - previousTime) * (1.0f / TIMER_RESOLUTION);
		fps = 1.0f / tpf;
		previousTime = getTime();
	}

	/**
	 * Reset the timer to the current state, when it was created.
	 * <p>
	 * It is just setting the starting time to now.
	 */
	public void reset() {
		startTime = System.nanoTime();
		previousTime = getTime();
	}
	
	/**
	 * Return the amount of time, the timer has run since the start.
	 * 
	 * @return The difference between the current time and the starting time.
	 */
	private long getTime() {
		return System.nanoTime() - startTime;
	}
	
	/**
	 * Return the current time per frame.
	 * 
	 * @return The current time per frame.
	 */
	public float getTimePerFrame() {
		return tpf;
	}
	
	/**
	 * Return the current frame-rate or frame per second.
	 * 
	 * @return The current frame-rate.
	 */
	public float getFrameRate() {
		return fps;
	}
}
