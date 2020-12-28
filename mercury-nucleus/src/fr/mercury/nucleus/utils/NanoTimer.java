package fr.mercury.nucleus.utils;

/**
 * <code>NanoTimer</code> is an implementation of {@link Timer} which uses the nanoseconds for unit with the
 * {@link System#nanoTime()} method.
 * 
 * @author GnosticOccultist
 * 
 * @see SpeedableNanoTimer
 */
public class NanoTimer implements Timer {

	/**
	 * The resolution of the nano-timer, converting seconds to nanoseconds.
	 */
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
	 * Updates the <code>NanoTimer</code> by updating its {@link #getTimePerFrame()}, {@link #getFrameRate()} values
	 * and refreshing its previous update time.
	 */
	public void update() {
		long time = getTime();
		tpf = (time - previousTime) * (1.0f / TIMER_RESOLUTION);
		fps = 1.0f / tpf;
		previousTime = time;
	}

	/**
	 * Resets the <code>NanoTimer</code> by setting the current time as its starting time.
	 */
	public void reset() {
		startTime = System.nanoTime();
		previousTime = getTime();
	}
	
	/**
	 * Return the amount of time since the timer has started to run or since the last reset in nanoseconds.
	 * 
	 * @return The amount of timer since the timer has (re)started in nanoseconds.
	 */
	@Override
	public long getTime() {
		return System.nanoTime() - startTime;
	}
	
	/**
	 * Return the current time in seconds per frame of the <code>NanoTimer</code>.
	 * 
	 * @return The current time in seconds per frame.
	 */
	@Override
	public float getTimePerFrame() {
		return tpf;
	}
	
	/**
	 * Return the frame the current frame-rate or frame per second of the <code>NanoTimer</code>.
	 * 
	 * @return The current frame-rate in frames per second.
	 */
	@Override
	public float getFrameRate() {
		return fps;
	}
	
	/**
	 * Return always false since the <code>NanoTimer</code> can't be paused.
	 * Please use the {@link SpeedableNanoTimer} implementation instead if desired so.
	 * 
	 * @see SpeedableNanoTimer
	 */
	@Override
	public boolean isPaused() {
		return false;
	}
	
	/**
	 * Return always 1.0F since the <code>NanoTimer</code> can't be speeded up.
	 * Please use the {@link SpeedableNanoTimer} implementation instead if desired so.
	 * 
	 * @see SpeedableNanoTimer
	 */
	@Override
	public float getSpeed() {
		return 1F;
	}
	
	/**
	 * Return the resolution of the <code>NanoTimer</code>, converting from seconds to nanoseconds.
	 * 
	 * @return The resolution used by the nano timer (1_000_000_000).
	 */
	@Override
	public long getResolution() {
		return TIMER_RESOLUTION;
	}
}
