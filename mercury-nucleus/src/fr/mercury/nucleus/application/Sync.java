package fr.mercury.nucleus.application;

import fr.mercury.nucleus.utils.NanoTimer;

/**
 * <code>Sync</code> is a highly accurate sync method that continually adapts to the system, 
 * it runs on to provide reliable results.
 * <p>
 * This class originally comes from LWJGL 2 and has been adpated to work with Mercury-Engine.
 * 
 * @author Riven
 * @author kappaOne
 * @author GnosticOccultist
 */
class Sync {

    /**
     * Whether the sync has been initialized.
     */
    private static boolean initialized = false;
    /**
     * The time to sleep/yield until the next frame.
     */
    private static long nextFrame = 0;
    /**
     * A store class for calculating the averages of the previous sleep times.
     */
    private static RunningAverage sleepDurations = new RunningAverage(10);
    /**
     * A store class for calculating the averages of the previous yield times.
     */
    private static RunningAverage yieldDurations = new RunningAverage(10);

    /**
     * An accurate sync method that will attempt to run at a constant frame rate. It
     * should be called once every frame.
     * 
     * @param fps The desired frame rate, in frames per second (&gt;0).
     */
    public static void sync(int fps) {
        if (fps <= 0) {
            return;
        }

        if (!initialized) {
            initialize();
        }

        try {
            // Sleep until the average sleep time is greater than the time remaining
            // until the next frame.
            for (long t0 = getTime(), t1; (nextFrame - t0) > sleepDurations.average(); t0 = t1) {
                Thread.sleep(1);
                // Update the average sleep time.
                sleepDurations.add((t1 = getTime()) - t0);
            }

            // Slowly dampen sleep average if too high to avoid yielding too much.
            sleepDurations.dampenForLowResTicker();

            // Yield until the average yield time is greater than the time remaining
            // until the next frame.
            for (long t0 = getTime(), t1; (nextFrame - t0) > yieldDurations.average(); t0 = t1) {
                Thread.yield();
                // Update the average yield time.
                yieldDurations.add((t1 = getTime()) - t0);
            }

        } catch (InterruptedException ex) {
            // Ignore.
        }

        // Schedule next frame, drop frame(s) if already too late for next frame.
        nextFrame = Math.max(nextFrame + NanoTimer.TIMER_RESOLUTION / fps, getTime());
    }

    /**
     * This method will initialize the sync method by setting initial values for
     * sleep/yield durations and the next frame value.
     * <p>
     * If running on windows, it will start a daemon {@link Thread} executing the
     * sleep timer fix.
     */
    private static void initialize() {
        initialized = true;

        sleepDurations.initialize(1000 * 1000);
        yieldDurations.initialize((int) (-(getTime() - getTime()) * 1.333));

        nextFrame = getTime();

        /*
         * On windows the sleep functions can be highly inaccurate by over 10ms making
         * it unusable. However it can be forced to be a bit more accurate by running a
         * separate sleeping daemon thread.
         */
        var timerAccuracy = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(Long.MAX_VALUE);
                }
            } catch (Exception ex) {
            }
        });

        timerAccuracy.setName("Sync Timer");
        timerAccuracy.setDaemon(true);
        timerAccuracy.start();
    }

    /**
     * Return the current system time in nanoseconds.
     * 
     * @return The current time in nanoseconds (&gt;0).
     */
    private static long getTime() {
        var time = System.currentTimeMillis() & 0x7FFFFFFFFFFFFFFFL;
        var res = 1000L;
        return (time * NanoTimer.TIMER_RESOLUTION) / res;
    }

    private static class RunningAverage {

        /**
         * A threshold corresponding to 10 ms.
         */
        private static final long DAMPEN_THRESHOLD = 10 * 1000L * 1000L;
        private static final float DAMPEN_FACTOR = 0.9f;

        private final long[] slots;
        private int offset;

        public RunningAverage(int slotCount) {
            this.slots = new long[slotCount];
            this.offset = 0;
        }

        public void initialize(long value) {
            while (offset < slots.length) {
                slots[offset++] = value;
            }
        }

        public void add(long value) {
            slots[offset++ % slots.length] = value;
            offset %= slots.length;
        }

        public long average() {
            var sum = 0L;
            for (var i = 0; i < slots.length; ++i) {
                sum += slots[i];
            }
            return sum / slots.length;
        }

        public void dampenForLowResTicker() {
            if (average() > DAMPEN_THRESHOLD) {
                for (var i = 0; i < slots.length; ++i) {
                    slots[i] *= DAMPEN_FACTOR;
                }
            }
        }
    }
}
