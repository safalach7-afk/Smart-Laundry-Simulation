package laundry.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared control flags for pause/resume and speed adjustment. Demonstrates:
 * AtomicBoolean/AtomicInteger as lightweight thread-safe coordination flags
 * read by many threads, plus Thread.sleep() with a busy-wait check for
 * cooperative pausing.
 */
public class SimulationControl {

    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicInteger speedMultiplier = new AtomicInteger(1); // 1x or 2x

    public void setPaused(boolean value) {
        paused.set(value);
    }

    public boolean isPaused() {
        return paused.get();
    }

    public void setSpeedMultiplier(int multiplier) {
        speedMultiplier.set(multiplier);
    }

    public int getSpeedMultiplier() {
        return speedMultiplier.get();
    }

    /**
     * Sleep for baseMillis, adjusted by speed multiplier, while cooperatively
     * pausing if paused=true.
     */
    public void sleep(long baseMillis) throws InterruptedException {
        long adjusted = baseMillis / speedMultiplier.get();
        long remaining = adjusted;
        long step = 100; // check pause every 100ms

        while (remaining > 0) {
            while (paused.get()) {
                Thread.sleep(100); // wait while paused
            }
            long sleepNow = Math.min(step, remaining);
            Thread.sleep(sleepNow);
            remaining -= sleepNow;
        }
    }
}
