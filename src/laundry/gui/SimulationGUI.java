package laundry.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimulationGUI extends JFrame {

    private static final Color BG_COLOR = new Color(18, 25, 40);
    private static final Color HEADER_COLOR = new Color(30, 40, 60);
    private static final Color ARRIVED_COLOR = new Color(80, 90, 110);
    private static final Color WASHING_COLOR = new Color(30, 130, 160);
    private static final Color DRYING_COLOR = new Color(200, 120, 30);
    private static final Color PAYMENT_COLOR = new Color(40, 160, 100);
    private static final Color DONE_COLOR = new Color(60, 70, 90);
    private static final Color TEXT_COLOR = new Color(220, 230, 255);
    private static final Color STAT_BG = new Color(30, 40, 65);
    private static final Color FREE_COLOR = new Color(60, 90, 70);
    private static final Color BUSY_COLOR = new Color(200, 170, 60);
    private static final Color FAIL_COLOR = new Color(200, 60, 60);

    // Resource panel cells
    private final JPanel[] washerCells = new JPanel[6];
    private final JPanel[] dryerCells = new JPanel[4];
    private final JPanel[] kioskCells = new JPanel[2];
    private final JLabel[] washerLabels = new JLabel[6];
    private final JLabel[] dryerLabels = new JLabel[4];
    private final JLabel[] kioskLabels = new JLabel[2];

    // Flow lanes
    private final JPanel arrivedPanel, washingPanel, dryingPanel, paymentPanel, donePanel;
    private final ConcurrentHashMap<String, JLabel> customerLabels = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> customerStage = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> customerStartTimes = new ConcurrentHashMap<>();
    private final JLabel servedLabel, avgTimeLabel, queueLabel, activeLabel;
    private final JLabel waitWasherLabel, waitDryerLabel, waitKioskLabel;
    private final JTextPane logArea;
    private final JLabel bannerLabel;
    public final JButton pauseButton;

    public SimulationGUI() {
        setTitle("Smart Laundry Facility - Simulation Dashboard");
        setSize(1150, 820);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_COLOR);

        // ===== TOP: title + stats =====
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(HEADER_COLOR);
        topPanel.setBorder(new EmptyBorder(8, 20, 8, 20));
        topPanel.setPreferredSize(new Dimension(1150, 110));

        JLabel title = new JLabel("Smart Laundry Facility", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        statsRow.setBackground(HEADER_COLOR);

        servedLabel = makeStatChip("Served: 0");
        avgTimeLabel = makeStatChip("Avg Time: 0.00s");
        queueLabel = makeStatChip("Pay Queue: 0");
        activeLabel = makeStatChip("Active: 0");
        waitWasherLabel = makeStatChip("Waiting-Washer: 0");
        waitDryerLabel = makeStatChip("Waiting-Dryer: 0");
        waitKioskLabel = makeStatChip("Waiting-Kiosk: 0");

        statsRow.add(activeLabel);
        statsRow.add(waitWasherLabel);
        statsRow.add(waitDryerLabel);
        statsRow.add(waitKioskLabel);
        statsRow.add(queueLabel);
        statsRow.add(avgTimeLabel);
        statsRow.add(servedLabel);

        // Control buttons (pause/resume, speed)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controlPanel.setBackground(HEADER_COLOR);

        controlPanel.setPreferredSize(new Dimension(150, 36));
        
        

        pauseButton = new JButton("PAUSE");
        pauseButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pauseButton.setBackground(new Color(220, 80, 80));
        pauseButton.setForeground(Color.WHITE);
        pauseButton.setFocusPainted(false);
        pauseButton.setPreferredSize(new Dimension(120, 35));
        
        controlPanel.add(pauseButton);

        JPanel westContainer = new JPanel();
        westContainer.setLayout(new BoxLayout(westContainer, BoxLayout.Y_AXIS));
        westContainer.setBackground(HEADER_COLOR);
        westContainer.add(title);
        westContainer.add(controlPanel);

        topPanel.add(westContainer, BorderLayout.WEST);
        topPanel.add(statsRow, BorderLayout.EAST);

        // ===== RESOURCE PANEL (washers/dryers/kiosks) =====
        JPanel resourcePanel = new JPanel(new GridLayout(1, 3, 15, 0));
        resourcePanel.setBackground(BG_COLOR);
        resourcePanel.setBorder(new EmptyBorder(10, 15, 5, 15));

        resourcePanel.add(buildResourceSection("WASHING MACHINES", washerCells, washerLabels, 6, 6, WASHING_COLOR));
        resourcePanel.add(buildResourceSection("DRYERS", dryerCells, dryerLabels, 4, 4, DRYING_COLOR));
        resourcePanel.add(buildResourceSection("PAYMENT KIOSKS", kioskCells, kioskLabels, 2, 2, PAYMENT_COLOR));

        // ===== FLOW LANES =====
        JPanel flowPanel = new JPanel(new GridLayout(1, 5, 6, 0));
        flowPanel.setBackground(BG_COLOR);
        flowPanel.setBorder(new EmptyBorder(8, 15, 6, 15));

        arrivedPanel = buildLane("ARRIVED", "[A]", ARRIVED_COLOR, 6);
        washingPanel = buildLane("WASHING", "[W]", WASHING_COLOR, 6);
        dryingPanel = buildLane("DRYING", "[D]", DRYING_COLOR, 4);
        paymentPanel = buildLane("PAYMENT", "[P]", PAYMENT_COLOR, 2);
        donePanel = buildLane("DONE", "[+]", DONE_COLOR, 6);

        flowPanel.add(arrivedPanel);
        flowPanel.add(washingPanel);
        flowPanel.add(dryingPanel);
        flowPanel.add(paymentPanel);
        flowPanel.add(donePanel);

        // Banner for alerts (hidden initially)
        bannerLabel = new JLabel("", SwingConstants.CENTER);
        bannerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bannerLabel.setForeground(Color.WHITE);
        bannerLabel.setBackground(new Color(180, 30, 30));
        bannerLabel.setOpaque(true);
        bannerLabel.setBorder(new EmptyBorder(8, 10, 8, 10));
        bannerLabel.setVisible(false);

        // Combine top + resource + flow into one north container
        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setBackground(BG_COLOR);
        northContainer.add(topPanel);
        northContainer.add(bannerLabel);
        northContainer.add(resourcePanel);
        northContainer.add(flowPanel);

        add(northContainer, BorderLayout.NORTH);

        // ===== LOG AREA =====
        logArea = new JTextPane();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(12, 18, 30));
        logArea.setForeground(new Color(100, 220, 140));
        logArea.setBorder(new EmptyBorder(5, 8, 5, 8));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 65, 100)));
        logScroll.setBackground(BG_COLOR);

        JLabel logTitle = new JLabel("  Event Log", SwingConstants.LEFT);
        logTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logTitle.setForeground(new Color(140, 160, 200));
        logTitle.setBackground(new Color(20, 28, 48));
        logTitle.setOpaque(true);
        logTitle.setBorder(new EmptyBorder(4, 8, 4, 0));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(logTitle, BorderLayout.NORTH);
        bottomPanel.add(logScroll, BorderLayout.CENTER);
        bottomPanel.setPreferredSize(new Dimension(1150, 200));

        add(bottomPanel, BorderLayout.SOUTH);
        setLocationRelativeTo(null);

        // Timer to update elapsed time on each customer bubble
        Timer timer = new Timer(500, e -> {
            if (!simulationPaused) {
                updateBubbleTimers();
            }
        });
        timer.start();
    }

    private volatile boolean simulationPaused = false;

    public void setSimulationPaused(boolean paused) {
        this.simulationPaused = paused;
    }

    private void updateBubbleTimers() {
        long now = System.currentTimeMillis();
        for (java.util.Map.Entry<String, JLabel> entry : customerLabels.entrySet()) {
            String customerName = entry.getKey();
            JLabel bubble = entry.getValue();
            String stage = customerStage.get(customerName);

            if ("done".equals(stage)) {
                continue; // freeze timer once done
            }
            Long start = customerStartTimes.get(customerName);
            if (start == null) {
                continue;
            }

            double elapsed = (now - start) / 1000.0;
            String shortName = customerName.replace("Customer-", "C-");
            bubble.setText("<html><center><b>" + shortName + "</b><br><span style='font-size:9px'>"
                    + String.format("%.1fs", elapsed) + "</span></center></html>");
        }
    }

    // ---- Build resource section (e.g. 6 washer boxes) ----
    private JPanel buildResourceSection(String title, JPanel[] cells, JLabel[] labels, int count, int cols, Color color) {
        JPanel outer = new JPanel(new BorderLayout(5, 5));
        outer.setBackground(BG_COLOR);
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1, true),
                new EmptyBorder(6, 6, 6, 6)));

        JLabel sectionLabel = new JLabel(title, SwingConstants.CENTER);
        sectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sectionLabel.setForeground(color);
        sectionLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        outer.add(sectionLabel, BorderLayout.NORTH);

        int rows = (int) Math.ceil((double) count / cols);
        JPanel grid = new JPanel(new GridLayout(rows, cols, 6, 6));
        grid.setBackground(BG_COLOR);

        for (int i = 0; i < count; i++) {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setBackground(FREE_COLOR);
            cell.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            cell.setPreferredSize(new Dimension(70, 50));

            JLabel idLabel = new JLabel("#" + (i + 1), SwingConstants.CENTER);
            idLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            idLabel.setForeground(Color.WHITE);

            JLabel statusLabel = new JLabel("FREE", SwingConstants.CENTER);
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            statusLabel.setForeground(Color.WHITE);

            cell.add(idLabel, BorderLayout.CENTER);
            cell.add(statusLabel, BorderLayout.SOUTH);

            cells[i] = cell;
            labels[i] = statusLabel;
            grid.add(cell);
        }

        outer.add(grid, BorderLayout.CENTER);
        return outer;
    }

    private final java.util.Map<JPanel, JPanel> laneInnerPanels = new java.util.HashMap<>();

    private JPanel buildLane(String stageName, String icon, Color color, int visibleSlots) {
        JPanel outer = new JPanel(new BorderLayout());
        Color bgDark = new Color(
                Math.max(color.getRed() - 60, 0),
                Math.max(color.getGreen() - 60, 0),
                Math.max(color.getBlue() - 60, 0));
        outer.setBackground(bgDark);
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                new EmptyBorder(8, 6, 8, 6)));

        JLabel header = new JLabel(icon + "  " + stageName, SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(color);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        outer.add(header, BorderLayout.NORTH);

        // Inner panel holds bubbles, goes inside scroll pane
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(bgDark);

        JScrollPane scroll = new JScrollPane(inner);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setBackground(bgDark);
        scroll.getViewport().setBackground(bgDark);

        // Each bubble is ~36px + 4px strut = 40px
        int height = visibleSlots * 40;
        scroll.setPreferredSize(new Dimension(100, height));

        outer.add(scroll, BorderLayout.CENTER);

        laneInnerPanels.put(outer, inner);
        return outer;
    }

    private JLabel makeCustomerBubble(String name, Color stageColor) {
        String shortName = name.replace("Customer-", "C-");
        JLabel bubble = new JLabel(
                "<html><center><b>" + shortName + "</b><br><span style='font-size:9px'>0.0s</span></center></html>",
                SwingConstants.CENTER);
        bubble.setName(name); // tag bubble with customer name for timer updates
        bubble.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bubble.setForeground(Color.WHITE);
        bubble.setOpaque(true);
        bubble.setBackground(stageColor.darker());
        bubble.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(stageColor.brighter(), 1, true),
                new EmptyBorder(6, 6, 6, 6)));
        bubble.setPreferredSize(new Dimension(80, 36));
        bubble.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        bubble.setAlignmentX(Component.CENTER_ALIGNMENT);
        return bubble;
    }

    private JLabel makeStatChip(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_COLOR);
        l.setOpaque(true);
        l.setBackground(STAT_BG);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 80, 130), 1, true),
                new EmptyBorder(4, 14, 4, 14)));
        return l;
    }

    private void moveToLane(String customerName, JPanel targetLane, Color stageColor) {
        SwingUtilities.invokeLater(() -> {
            String currentStage = customerStage.get(customerName);
            JPanel currentLaneOuter = getLaneByStage(currentStage);
            JLabel existingLabel = customerLabels.get(customerName);

            if (currentLaneOuter != null && existingLabel != null) {
                JPanel currentInner = laneInnerPanels.get(currentLaneOuter);
                currentInner.remove(existingLabel);
                currentInner.revalidate();
                currentInner.repaint();
            }

            JLabel bubble = makeCustomerBubble(customerName, stageColor);
            customerLabels.put(customerName, bubble);

            JPanel targetInner = laneInnerPanels.get(targetLane);
            targetInner.add(bubble);
            targetInner.add(Box.createVerticalStrut(4));
            targetInner.revalidate();
            targetInner.repaint();
        });
    }

    private JPanel getLaneByStage(String stage) {
        if (stage == null) {
            return null;
        }
        switch (stage) {
            case "none":
                return null;
            case "arrived":
                return arrivedPanel;
            case "washing":
                return washingPanel;
            case "drying":
                return dryingPanel;
            case "payment":
                return paymentPanel;
            case "done":
                return donePanel;
            default:
                return null;
        }
    }

    // ===== Resource cell updates =====
    private void setCell(JPanel cell, JLabel label, Color bg, String text) {
        SwingUtilities.invokeLater(() -> {
            cell.setBackground(bg);
            label.setText(text);
        });
    }

    public void setWasherStatus(int index, String status, String customerShortName) {
        if (index < 0 || index >= washerCells.length) {
            return;
        }
        Color color = status.equals("BUSY") ? BUSY_COLOR : status.equals("FAILED") ? FAIL_COLOR : FREE_COLOR;
        String text = status.equals("FREE") ? "FREE" : (status.equals("FAILED") ? "FAILED!" : customerShortName);
        setCell(washerCells[index], washerLabels[index], color, text);
    }

    public void setDryerStatus(int index, String status, String customerShortName) {
        if (index < 0 || index >= dryerCells.length) {
            return;
        }
        Color color = status.equals("BUSY") ? BUSY_COLOR : status.equals("FAILED") ? FAIL_COLOR : FREE_COLOR;
        String text = status.equals("FREE") ? "FREE" : (status.equals("FAILED") ? "FAILED!" : customerShortName);
        setCell(dryerCells[index], dryerLabels[index], color, text);
    }

    public void setKioskStatus(int index, String status, String customerShortName) {
        if (index < 0 || index >= kioskCells.length) {
            return;
        }
        Color color = status.equals("BUSY") ? BUSY_COLOR : status.equals("FAILED") ? FAIL_COLOR : FREE_COLOR;
        String text = status.equals("FREE") ? "FREE" : (status.equals("FAILED") ? "FAILED!" : customerShortName);
        setCell(kioskCells[index], kioskLabels[index], color, text);
    }

    // ===== Flow lane updates =====
    public void customerArrived(String customerName) {
        customerStartTimes.put(customerName, System.currentTimeMillis());
        customerStage.put(customerName, "none");
        moveToLane(customerName, arrivedPanel, ARRIVED_COLOR);
        customerStage.put(customerName, "arrived");
    }

    public void customerWashing(String customerName) {
        moveToLane(customerName, washingPanel, WASHING_COLOR);
        customerStage.put(customerName, "washing");
    }

    public void customerDrying(String customerName) {
        moveToLane(customerName, dryingPanel, DRYING_COLOR);
        customerStage.put(customerName, "drying");
    }

    public void customerPaying(String customerName) {
        moveToLane(customerName, paymentPanel, PAYMENT_COLOR);
        customerStage.put(customerName, "payment");
    }

    public void customerDone(String customerName) {
        moveToLane(customerName, donePanel, DONE_COLOR);
        customerStage.put(customerName, "done");
    }

    public void showBanner(String message) {
        SwingUtilities.invokeLater(() -> {
            bannerLabel.setText(message);
            bannerLabel.setVisible(true);
            revalidate();
            repaint();
        });
    }

    public void showCompletionSummary(int served, double avgTime, int maxWashers, int maxDryers, long totalRuntimeMs) {
        SwingUtilities.invokeLater(() -> {
            String message = String.format(
                    "<html><div style='text-align:center; font-family:Segoe UI;'>"
                    + "<h2>Simulation Complete!</h2>"
                    + "<table style='margin:auto;'>"
                    + "<tr><td align='right'><b>Total Customers Served:</b></td><td>&nbsp;%d / 50</td></tr>"
                    + "<tr><td align='right'><b>Average Time per Customer:</b></td><td>&nbsp;%.2f seconds</td></tr>"
                    + "<tr><td align='right'><b>Max Washers Used Simultaneously:</b></td><td>&nbsp;%d / 6</td></tr>"
                    + "<tr><td align='right'><b>Max Dryers Used Simultaneously:</b></td><td>&nbsp;%d / 4</td></tr>"
                    + "<tr><td align='right'><b>Total Simulation Runtime:</b></td><td>&nbsp;%.1f seconds</td></tr>"
                    + "</table></div></html>",
                    served, avgTime, maxWashers, maxDryers, totalRuntimeMs / 1000.0
            );

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Smart Laundry Facility - Simulation Results",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            Color color;
            if (message.contains("FAILED") || message.contains("ALERT") || message.contains("DOWN")) {
                color = new Color(255, 100, 100); // red
            } else if (message.contains("SUCCESSFUL") || message.contains("DONE") || message.contains("finished")) {
                color = new Color(100, 220, 140); // green
            } else if (message.contains("arrived") || message.contains("waiting")) {
                color = new Color(100, 180, 255); // blue
            } else {
                color = new Color(180, 190, 210); // neutral grey
            }

            StyledDocument doc = logArea.getStyledDocument();
            Style style = logArea.addStyle("color", null);
            StyleConstants.setForeground(style, color);

            try {
                doc.insertString(doc.getLength(), message + "\n", style);
            } catch (BadLocationException e) {
                // ignore
            }

            logArea.setCaretPosition(doc.getLength());
        });
    }

    public void updateStats(int served, double avgTime, int queue) {
        SwingUtilities.invokeLater(() -> {
            servedLabel.setText("Served: " + served);
            avgTimeLabel.setText(String.format("Avg: %.1fs", avgTime));
            queueLabel.setText("Pay Queue: " + queue);
            activeLabel.setText("Active: " + (50 - served));
        });
    }

    public void updateWaitCounts(int waitingWasher, int waitingDryer, int waitingKiosk) {
        SwingUtilities.invokeLater(() -> {
            waitWasherLabel.setText("Waiting-Washer: " + waitingWasher);
            waitDryerLabel.setText("Waiting-Dryer: " + waitingDryer);
            waitKioskLabel.setText("Waiting-Kiosk: " + waitingKiosk);
        });
    }
}
