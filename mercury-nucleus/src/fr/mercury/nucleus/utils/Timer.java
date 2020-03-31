package fr.mercury.nucleus.utils;

/**
 * <code>Timer</code> is an non-read-only implementation of the {@link ReadableTimer} interface and provide a way to {@link #update()}
 * or {@link #reset()} any implementations without knowing its resolution.
 * 
 * @author GnosticOccultist
 */
public interface Timer extends ReadableTimer {
	
	/**
	 * Update the <code>Timer</code> by updating the value returned by {@link #getTimePerFrame()} and {@link #getFrameRate()}
	 * and refreshing the last update time of the timer.
	 * <p>
	 * Note that if the implementation of this interface is pausable, then the timer shouldn't update if that's the case.
	 */
	void update();
	
	/**
	 * Reset the <code>Timer</code> so the starting time correspond to this call.
	 */
	void reset();
}
