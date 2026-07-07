package laundry.simulation;

import laundry.gui.SimulationGUI;
import laundry.model.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Customer implements Runnable {

    private final int customerId;
    private final ResourcePool washerPool;
    private final ResourcePool dryerPool;
    private final ResourcePool kioskPool;
    private final WashingMachine washingMachine;
    private final Dryer dryer;
    private final List<PaymentKiosk> kiosks;
    private final Statistics stats;
    private final AtomicInteger paymentQueueCount;
    private final SimulationGUI gui;
    private final SimulationControl control;
    private final Random random = new Random();

    public Customer(int customerId, ResourcePool washerPool, ResourcePool dryerPool,
            ResourcePool kioskPool, WashingMachine washingMachine, Dryer dryer,
            List<PaymentKiosk> kiosks, Statistics stats, AtomicInteger paymentQueueCount,
            SimulationGUI gui, SimulationControl control) {
        this.customerId = customerId;
        this.washerPool = washerPool;
        this.dryerPool = dryerPool;
        this.kioskPool = kioskPool;
        this.washingMachine = washingMachine;
        this.dryer = dryer;
        this.kiosks = kiosks;
        this.stats = stats;
        this.paymentQueueCount = paymentQueueCount;
        this.gui = gui;
        this.control = control;
    }

    private void pushWaitCounts() {
        gui.updateWaitCounts(stats.getWaitingForWasher(), stats.getWaitingForDryer(), stats.getWaitingForKiosk());
    }

    @Override
    public void run() {
        String name = "Customer-" + customerId;
        long startTime = System.currentTimeMillis();
        try {
            // ARRIVAL
            control.sleep(random.nextInt(3000));
            gui.customerArrived(name);
            gui.log(String.format(">> [%s] %s has arrived.",
                    Thread.currentThread().getName(), name));

            // WASHING STAGE
            stats.incrementWaitingForWasher();
            pushWaitCounts();
            int washerIndex = washerPool.acquire();
            stats.decrementWaitingForWasher();
            pushWaitCounts();
            gui.customerWashing(name);
            try {
                washingMachine.wash(washerIndex, name, stats);
            } finally {
                washerPool.release(washerIndex);
            }

            // DRYING STAGE
            stats.incrementWaitingForDryer();
            pushWaitCounts();
            int dryerIndex = dryerPool.acquire();
            stats.decrementWaitingForDryer();
            pushWaitCounts();
            gui.customerDrying(name);
            try {
                dryer.dry(dryerIndex, name, stats);
            } finally {
                dryerPool.release(dryerIndex);
            }

            // PAYMENT STAGE
            paymentQueueCount.incrementAndGet();
            stats.incrementWaitingForKiosk();
            pushWaitCounts();
            int kioskIndex = kioskPool.acquire();
            stats.decrementWaitingForKiosk();
            pushWaitCounts();
            gui.customerPaying(name);
            try {
                paymentQueueCount.decrementAndGet();
                PaymentKiosk kiosk = kiosks.get(kioskIndex);
                kiosk.process(name);
            } finally {
                kioskPool.release(kioskIndex);
            }

            // DONE
            long totalTime = System.currentTimeMillis() - startTime;
            stats.recordCompletion(totalTime);
            gui.customerDone(name);
            gui.log(String.format("<< [%s] %s DONE (%.2fs).",
                    Thread.currentThread().getName(), name, totalTime / 1000.0));
            gui.updateStats(stats.getTotalServed(),
                    stats.getAverageTimeSeconds(), paymentQueueCount.get());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            gui.log(String.format("[%s] %s was interrupted.",
                    Thread.currentThread().getName(), name));
        }
    }
}
