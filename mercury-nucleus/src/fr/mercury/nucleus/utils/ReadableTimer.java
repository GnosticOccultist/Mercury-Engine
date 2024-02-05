package fr.mercury.nucleus.utils;

/**
 * <code>ReadableTimer</code> is an interface to implement a timer based on
 * different time units and providing read-only methods. The main purposes of
 * this interface is to be passed as an argument of the update loop to allow
 * subsequent call to access and effectuate time dependent calculations.
 * 
 * @author GnosticOccultist
 */
public interface ReadableTimer {

    /**
     * A dummy timer to execute some methods that requires a {@link ReadableTimer}
     * as an argument.
     */
    DummyTimer DUMMY_TIMER = new DummyTimer();

    class DummyTimer implements ReadableTimer {

        @Override
        public float getTimePerFrame() {
            return 0;
        }

        @Override
        public float getFrameRate() {
            return 0;
        }

        @Override
        public float getSpeed() {
            return 1.0f;
        }

        @Override
        public boolean isPaused() {
            return false;
        }

        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public long getResolution() {
            return 0;
        }
    }

    /**
     * Return the current time in seconds per frame of the
     * <code>ReadableTimer</code>.
     * 
     * @return The current time in seconds per frame (&ge;0).
     */
    float getTimePerFrame();

    /**
     * Return the frame the current frame-rate or frame per second of the
     * <code>ReadableTimer</code>.
     * 
     * @return The current frame-rate in frames per second (&ge;0).
     */
    float getFrameRate();

    /**
     * Return the speed at which the <code>ReadableTimer</code> should run. It can
     * be applied to {@link #getTimePerFrame()} in order to accelerate time
     * dependent actions in the game-loop.
     * <p>
     * Note that a speed of zero means that it is paused and so {@link #isPaused()}
     * should return true.
     * 
     * @return The speed of the timer (&ge;0).
     */
    float getSpeed();

    /**
     * Return whether the <code>ReadableTimer</code> is paused. If this is the case,
     * then {@link #update()} shouldn't perform.
     * 
     * @return Whether the timer is paused.
     */
    boolean isPaused();

    /**
     * Return the amount of time since the timer has started to run or since the
     * last reset in the <code>ReadableTimer</code> units.
     * 
     * @return The amount of time since the timer has (re)started in timer units.
     */
    long getTime();

    /**
     * Return the resolution of the <code>ReadableTimer</code>, converting from
     * seconds to its units.
     * 
     * @return The resolution used by the timer (&gt;0).
     */
    long getResolution();
}
