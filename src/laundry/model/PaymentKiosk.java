package laundry.model;

import laundry.gui.SimulationGUI;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class PaymentKiosk {

    private final int id; // 0-based index
    private final ReentrantLock lock = new ReentrantLock();
    private final Random random = new Random();
    private volatile boolean disabled = false;
    private final SimulationGUI gui;
    private final SimulationControl control;

    public PaymentKiosk(int id, SimulationGUI gui, SimulationControl control) {
        this.id = id;
        this.gui = gui;
        this.control = control;
    }

    public int getId() {
        return id;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void process(String customerName) throws InterruptedException {
        String shortName = customerName.replace("Customer-", "C-");
        lock.lock();
        gui.setKioskStatus(id, "BUSY", shortName);
        try {
            while (true) {
                if (disabled) {
                    gui.log(String.format("[Kiosk-%d][%s] Kiosk DOWN. %s waiting...",
                            id + 1, Thread.currentThread().getName(), customerName));
                    gui.setKioskStatus(id, "FAILED", shortName);
                    control.sleep(2000);
                    continue;
                }

                gui.log(String.format("[Kiosk-%d][%s] %s is paying...",
                        id + 1, Thread.currentThread().getName(), customerName));

                int payTime = 1000 + random.nextInt(1000);
                control.sleep(payTime);

                if (random.nextInt(100) < 5) {
                    gui.log(String.format("[Kiosk-%d][%s] Payment FAILED for %s. Retrying in 2s...",
                            id + 1, Thread.currentThread().getName(), customerName));
                    gui.setKioskStatus(id, "FAILED", shortName);
                    control.sleep(2000);
                    gui.setKioskStatus(id, "BUSY", shortName);
                    continue;
                }

                gui.log(String.format("[Kiosk-%d][%s] %s payment SUCCESSFUL.",
                        id + 1, Thread.currentThread().getName(), customerName));
                break;
            }
        } finally {
            gui.setKioskStatus(id, "FREE", shortName);
            lock.unlock();
        }
    }
}
