package laundry.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe statistics collector. Demonstrates: AtomicInteger/AtomicLong
 * (lock-free thread safety) and synchronized blocks for compound
 * (read-then-write) operations.
 */
public class Statistics {

    private final AtomicInteger totalServed = new AtomicInteger(0);
    private final AtomicLong totalTimeMillis = new AtomicLong(0);
    private final AtomicInteger waitingForWasher = new AtomicInteger(0);
    private final AtomicInteger waitingForDryer = new AtomicInteger(0);
    private final AtomicInteger waitingForKiosk = new AtomicInteger(0);
    private final AtomicInteger washersInUse = new AtomicInteger(0);
    private final AtomicInteger dryersInUse = new AtomicInteger(0);
    private int maxWashersInUse = 0;
    private int maxDryersInUse = 0;

    private final Object lock = new Object();

    public void incrementWashersInUse() {
        int current = washersInUse.incrementAndGet();
        synchronized (lock) {
            if (current > maxWashersInUse) {
                maxWashersInUse = current;
            }
        }
    }

    public void decrementWashersInUse() {
        washersInUse.decrementAndGet();
    }

    public void incrementDryersInUse() {
        int current = dryersInUse.incrementAndGet();
        synchronized (lock) {
            if (current > maxDryersInUse) {
                maxDryersInUse = current;
            }
        }
    }

    public void decrementDryersInUse() {
        dryersInUse.decrementAndGet();
    }

    public int getCurrentWashersInUse() {
        return washersInUse.get();
    }

    public int getCurrentDryersInUse() {
        return dryersInUse.get();
    }

    public void incrementWaitingForWasher() {
        waitingForWasher.incrementAndGet();
    }

    public void decrementWaitingForWasher() {
        waitingForWasher.decrementAndGet();
    }

    public int getWaitingForWasher() {
        return waitingForWasher.get();
    }

    public void incrementWaitingForDryer() {
        waitingForDryer.incrementAndGet();
    }

    public void decrementWaitingForDryer() {
        waitingForDryer.decrementAndGet();
    }

    public int getWaitingForDryer() {
        return waitingForDryer.get();
    }

    public void incrementWaitingForKiosk() {
        waitingForKiosk.incrementAndGet();
    }

    public void decrementWaitingForKiosk() {
        waitingForKiosk.decrementAndGet();
    }

    public int getWaitingForKiosk() {
        return waitingForKiosk.get();
    }

    public void recordCompletion(long timeTakenMillis) {
        totalServed.incrementAndGet();
        totalTimeMillis.addAndGet(timeTakenMillis);
    }

    public int getTotalServed() {
        return totalServed.get();
    }

    public double getAverageTimeSeconds() {
        int served = totalServed.get();
        if (served == 0) {
            return 0;
        }
        return (totalTimeMillis.get() / 1000.0) / served;
    }

    public int getMaxWashersInUse() {
        synchronized (lock) {
            return maxWashersInUse;
        }
    }

    public int getMaxDryersInUse() {
        synchronized (lock) {
            return maxDryersInUse;
        }
    }

    public synchronized String summary() {
        return String.format(
                "Total Served: %d | Avg Time: %.2fs | Max Washers Used: %d/6 | Max Dryers Used: %d/4",
                getTotalServed(), getAverageTimeSeconds(), getMaxWashersInUse(), getMaxDryersInUse()
        );
    }
}
