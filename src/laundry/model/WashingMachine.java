package laundry.model;

import laundry.gui.SimulationGUI;
import java.util.Random;

public class WashingMachine {

    private final Random random = new Random();
    private final SimulationGUI gui;
    private final SimulationControl control;

    public WashingMachine(SimulationGUI gui, SimulationControl control) {
        this.gui = gui;
        this.control = control;
    }

    public void wash(int washerIndex, String customerName, Statistics stats) throws InterruptedException {
        String shortName = customerName.replace("Customer-", "C-");
        stats.incrementWashersInUse();
        gui.setWasherStatus(washerIndex, "BUSY", shortName);
        try {
            while (true) {
                gui.log(String.format("[Washer-%d][%s] %s is washing...",
                        washerIndex + 1, Thread.currentThread().getName(), customerName));

                int washTime = 4000 + random.nextInt(2000);
                control.sleep(washTime);

                if (random.nextInt(100) < 5) {
                    gui.log(String.format("[Washer-%d][%s] Machine FAILED mid-cycle for %s. Retrying...",
                            washerIndex + 1, Thread.currentThread().getName(), customerName));
                    gui.setWasherStatus(washerIndex, "FAILED", shortName);
                    control.sleep(1000);
                    gui.setWasherStatus(washerIndex, "BUSY", shortName);
                    continue;
                }

                gui.log(String.format("[Washer-%d][%s] %s finished washing.",
                        washerIndex + 1, Thread.currentThread().getName(), customerName));
                break;
            }
        } finally {
            stats.decrementWashersInUse();
            gui.setWasherStatus(washerIndex, "FREE", shortName);
        }
    }
}
