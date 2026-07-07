package laundry.model;

import laundry.gui.SimulationGUI;
import java.util.Random;

public class Dryer {

    private final Random random = new Random();
    private final SimulationGUI gui;
    private final SimulationControl control;

    public Dryer(SimulationGUI gui, SimulationControl control) {
        this.gui = gui;
        this.control = control;
    }

    public void dry(int dryerIndex, String customerName, Statistics stats) throws InterruptedException {
        String shortName = customerName.replace("Customer-", "C-");
        stats.incrementDryersInUse();
        gui.setDryerStatus(dryerIndex, "BUSY", shortName);
        try {
            gui.log(String.format("[Dryer-%d][%s] %s is drying...",
                    dryerIndex + 1, Thread.currentThread().getName(), customerName));

            int dryTime = 3000 + random.nextInt(2000);
            control.sleep(dryTime);

            gui.log(String.format("[Dryer-%d][%s] %s finished drying.",
                    dryerIndex + 1, Thread.currentThread().getName(), customerName));
        } finally {
            stats.decrementDryersInUse();
            gui.setDryerStatus(dryerIndex, "FREE", shortName);
        }
    }
}
