package laundry.model;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Manages a pool of indexed resources (e.g. 6 washers numbered 0-5).
 * Demonstrates: BlockingQueue as a thread-safe alternative to manual
 * Semaphore + index bookkeeping. take() blocks if empty (all busy),
 * put() returns an index to the pool (mutual exclusion guaranteed).
 */
public class ResourcePool {
    private final BlockingQueue<Integer> available;

    public ResourcePool(int size) {
        available = new ArrayBlockingQueue<>(size);
        for (int i = 0; i < size; i++) {
            available.add(i);
        }
    }

    /** Blocks until a resource index is available, then removes it. */
    public int acquire() throws InterruptedException {
        return available.take();
    }

    /** Returns a resource index back to the pool. */
    public void release(int index) {
        available.add(index);
    }
}