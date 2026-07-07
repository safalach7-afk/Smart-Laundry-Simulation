package laundry.simulation;

import laundry.gui.SimulationGUI;
import laundry.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LaundrySimulation {

    private static final int NUM_WASHERS = 6;
    private static final int NUM_DRYERS = 4;
    private static final int NUM_KIOSKS = 2;
    private static final int NUM_CUSTOMERS = 50;
    private static final int CONGESTION_THRESHOLD = 30;

    public static void main(String[] args) throws InterruptedException {

        SimulationGUI gui = new SimulationGUI();
        gui.setVisible(true);

        SimulationControl control = new SimulationControl();

        gui.pauseButton.addActionListener(e -> {
            boolean nowPaused = !control.isPaused();
            control.setPaused(nowPaused);
            gui.setSimulationPaused(nowPaused);
            gui.pauseButton.setText(nowPaused ? "Resume" : "Pause");
            gui.log(nowPaused ? "*** SIMULATION PAUSED ***" : "*** SIMULATION RESUMED ***");
        });


        long simulationStartTime = System.currentTimeMillis();

        gui.log("===== SMART LAUNDRY FACILITY SIMULATION =====");
        gui.log("Washers: " + NUM_WASHERS + " | Dryers: " + NUM_DRYERS
                + " | Kiosks: " + NUM_KIOSKS + " | Customers: " + NUM_CUSTOMERS);
        gui.log("================================================");

        ResourcePool washerPool = new ResourcePool(NUM_WASHERS);
        ResourcePool dryerPool = new ResourcePool(NUM_DRYERS);
        ResourcePool kioskPool = new ResourcePool(NUM_KIOSKS);

        WashingMachine washingMachine = new WashingMachine(gui, control);
        Dryer dryer = new Dryer(gui, control);

        List<PaymentKiosk> kiosks = new ArrayList<>();
        for (int i = 0; i < NUM_KIOSKS; i++) {
            kiosks.add(new PaymentKiosk(i, gui, control));
        }

        Statistics stats = new Statistics();
        AtomicInteger paymentQueueCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(50);

        Thread congestionWatcher = new Thread(() -> {
            boolean ownerCalled = false;
            while (!Thread.currentThread().isInterrupted()) {
                if (!ownerCalled && paymentQueueCount.get() >= CONGESTION_THRESHOLD) {
                    gui.log("*** ALERT: Payment queue exceeded "
                            + CONGESTION_THRESHOLD + " customers! Calling the OWNER! ***");
                    gui.showBanner("CONGESTION ALERT: Both payment kiosks DOWN! Owner has been called!");

                    // Bonus scenario: disable both kiosks permanently
                    for (PaymentKiosk k : kiosks) {
                        k.setDisabled(true);
                    }
                    gui.setKioskStatus(0, "FAILED", "OWNER");
                    gui.setKioskStatus(1, "FAILED", "OWNER");

                    ownerCalled = true;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "CongestionWatcher");
        congestionWatcher.start();

        for (int i = 1; i <= NUM_CUSTOMERS; i++) {
            executor.submit(new Customer(i, washerPool, dryerPool,
                    kioskPool, washingMachine, dryer, kiosks, stats, paymentQueueCount, gui, control));
        }
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);
        congestionWatcher.interrupt();

        long totalRuntime = System.currentTimeMillis() - simulationStartTime;

        gui.log("================================================");
        gui.log("===== SIMULATION COMPLETE =====");
        gui.log(stats.summary());
        gui.log("================================================");

        gui.showCompletionSummary(
                stats.getTotalServed(),
                stats.getAverageTimeSeconds(),
                stats.getMaxWashersInUse(),
                stats.getMaxDryersInUse(),
                totalRuntime
        );
    }
}
