package io.arsh.visualizer;

import io.arsh.Network;
import io.arsh.models.Layer;
import io.arsh.models.Neuron;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.List;

public class Visualizer extends JPanel {

    private final Network network;

    private double scale = 0.8;
    private double offsetX = 50;
    private double offsetY = 120;

    private Point lastMouse;

    private double[] currentInput;
    private int currentTarget = -1;
    private int currentEpoch = 0;
    private int totalEpochs = 0;
    private int trainIndex = 0;
    private int totalTrainItems = 0;
    private int trainingDelay = 50;
    private JSlider speedSlider;
    private boolean isLetterMode = false;

    // Theme Colors (Matching the image)
    private static final Color BG_COLOR = new Color(0, 0, 0);
    private static final Color NEURON_BORDER = new Color(220, 220, 220, 180);
    private static final Color POS_WEIGHT = new Color(255, 255, 255); // White
    private static final Color NEG_WEIGHT = new Color(0, 0, 0); // Dark Yellow

    private static final int MAX_VISIBLE_NEURONS = 18;

    public Visualizer(Network network) {
        this.network = network;
        setPreferredSize(new Dimension(1400, 900));
        setBackground(BG_COLOR);
        setLayout(null); // Absolute positioning for the slider

        setupSpeedSlider();
        enableZoomAndPan();
    }

    private void setupSpeedSlider() {
        speedSlider = new JSlider(0, 1000, 50);
        speedSlider.setBounds(20, 100, 200, 25);
        speedSlider.setBackground(BG_COLOR);
        speedSlider.setForeground(Color.WHITE);
        speedSlider.setOpaque(false);
        speedSlider.addChangeListener(e -> trainingDelay = speedSlider.getValue());
        add(speedSlider);
    }

    public int getTrainingDelay() {
        return trainingDelay;
    }

    private void enableZoomAndPan() {
        addMouseWheelListener(e -> {
            double zoomFactor = 1.1;
            double oldScale = scale;
            if (e.getPreciseWheelRotation() < 0)
                scale *= zoomFactor;
            else
                scale /= zoomFactor;
            scale = Math.max(0.1, Math.min(10.0, scale));
            Point p = e.getPoint();
            offsetX = p.x - (p.x - offsetX) * (scale / oldScale);
            offsetY = p.y - (p.y - offsetY) * (scale / oldScale);
            repaint();
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouse = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point current = e.getPoint();
                offsetX += current.x - lastMouse.x;
                offsetY += current.y - lastMouse.y;
                lastMouse = current;
                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (network == null || network.getLayers().isEmpty())
            return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Draw Static Overlays (Input/Output/Stats)
        drawInputImage(g2);
        drawPredictionBars(g2);
        drawStats(g2);

        // 2. Translate and Scale the Network
        AffineTransform oldTransform = g2.getTransform();
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        drawNetwork(g2);

        g2.setTransform(oldTransform);
    }

    private void drawInputImage(Graphics2D g2) {
        if (currentInput == null)
            return;

        int size = 150;
        int x = 50;
        int y = getHeight() - size - 50;

        // Label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("INPUT IMAGE", x, y - 10);

        // Background
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(x - 5, y - 5, size + 10, size + 10);

        // Pixels (EMNIST is transposed/side-ways by default)
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 28; j++) {
                int index = i * 28 + j;
                float val = (float) currentInput[index];
                g2.setColor(new Color(val, val, val));
                g2.fillRect(x + j * (size / 28), y + i * (size / 28), (size / 28) + 1, (size / 28) + 1);
            }
        }

        // Target Label
        if (currentTarget != -1) {
            g2.setColor(POS_WEIGHT);
            String targetStr = isLetterMode ? String.valueOf((char) ('A' + currentTarget - 1))
                    : String.valueOf(currentTarget);
            g2.drawString("TARGET: " + targetStr, x, y + size + 25);
        }
    }

    private void drawPredictionBars(Graphics2D g2) {
        if (network == null)
            return;
        List<Layer> layers = network.getLayers();
        double[] output = layers.get(layers.size() - 1).getValues();
        if (output == null)
            return;

        int x = getWidth() - 300;
        int count = output.length;
        int rowHeight = count > 10 ? 18 : 25;
        int panelHeight = count * rowHeight + 20;
        int y = getHeight() - panelHeight - 50;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("LIVE PREDICTIONS", x, y - 10);

        for (int i = 0; i < count; i++) {
            int barY = y + i * rowHeight;
            double p = output[i];

            // Bar BG
            g2.setColor(new Color(255, 255, 255, 20));
            g2.fillRoundRect(x + 25, barY + 2, 200, rowHeight - 4, 4, 4);

            // Bar Fill
            g2.setColor(p > 0.5 ? POS_WEIGHT : new Color(100, 100, 100));
            int barWidth = (int) (Math.max(0, Math.min(1, p)) * 200);
            g2.fillRoundRect(x + 25, barY + 2, barWidth, rowHeight - 4, 4, 4);

            // Label & Percentage
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.PLAIN, count > 10 ? 10 : 12));
            String labelStr = (isLetterMode && i < 26) ? String.valueOf((char) ('A' + i)) : String.valueOf(i);
            g2.drawString(labelStr + ":", x, barY + rowHeight - 7);
            g2.drawString(String.format("%.1f%%", p * 100), x + 235, barY + rowHeight - 7);
        }
    }

    private void drawStats(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        int y = 30;
        g2.drawString("EPOCH: " + (currentEpoch + 1) + "/" + totalEpochs, 20, y);
        g2.drawString("TRAIN PROGRESS: " + trainIndex + "/" + totalTrainItems, 20, y + 20);

        g2.drawString("TRAINING DELAY: " + trainingDelay + " ms", 20, y + 60);

        // Simple progress bar
        if (totalTrainItems > 0) {
            int bw = 200;
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillRect(20, y + 30, bw, 5);
            g2.setColor(POS_WEIGHT);
            g2.fillRect(20, y + 30, (int) ((double) trainIndex / totalTrainItems * bw), 5);
        }
    }

    public void updateTraining(double[] input, int target, int epoch, int totalEpochs, int index, int total) {
        this.currentInput = input;
        this.currentTarget = target;
        this.currentEpoch = epoch;
        this.totalEpochs = totalEpochs;
        this.trainIndex = index;
        this.totalTrainItems = total;
        // Auto-detect letter mode based on target range (EMNIST letters are 1-26, but
        // let's check index range)
        if (target >= 1 && target <= 26 && network.getLayers().getLast().getNeurons().size() > 10) {
            this.isLetterMode = true;
        }
        repaint();
    }

    public void setLetterMode(boolean letterMode) {
        this.isLetterMode = letterMode;
    }

    private void drawNetwork(Graphics2D g2) {
        List<Layer> layers = network.getLayers();
        int layerCount = layers.size();

        int layerGap = 260;
        int neuronGap = 40;
        int neuronSize = 24;
        int neuronRadius = neuronSize / 2;

        // Pre-calculate neuron positions for visible neurons only
        int[][] neuronY = new int[layerCount][];
        boolean[] isCapped = new boolean[layerCount];

        for (int i = 0; i < layerCount; i++) {
            int count = layers.get(i).getNeurons().size();
            int visibleCount = Math.min(count, MAX_VISIBLE_NEURONS);
            isCapped[i] = count > MAX_VISIBLE_NEURONS;

            neuronY[i] = new int[visibleCount];
            int totalHeight = (visibleCount - 1) * neuronGap;
            int startY = -totalHeight / 2 + 200; // Centered
            for (int n = 0; n < visibleCount; n++) {
                neuronY[i][n] = startY + n * neuronGap;
            }
        }

        // Draw Connections First (Bottom to Top)
        for (int i = 1; i < layerCount; i++) {
            Layer currentLayer = layers.get(i);
            int prevCount = neuronY[i - 1].length;
            int currCount = neuronY[i].length;

            for (int n = 0; n < currCount; n++) {
                Neuron neuron = currentLayer.getNeurons().get(n);
                double[] weights = neuron.getWeights();
                int x1 = i * layerGap;
                int y1 = neuronY[i][n];

                for (int w = 0; w < prevCount; w++) {
                    double weight = weights[w];
                    int x0 = (i - 1) * layerGap;
                    int y0 = neuronY[i - 1][w];

                    float strength = (float) Math.abs(weight);
                    float alpha = Math.min(1.0f, 0.15f + strength * 0.6f);

                    Color c = weight > 0 ? POS_WEIGHT : NEG_WEIGHT;
                    g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)));
                    g2.setStroke(new BasicStroke(0.6f + strength * 1.2f));
                    g2.draw(new Line2D.Float(x0 + neuronRadius, y0, x1 - neuronRadius, y1));
                }
            }
        }

        // Draw Neurons
        for (int i = 0; i < layerCount; i++) {
            Layer layer = layers.get(i);
            int x = i * layerGap;
            int visibleCount = neuronY[i].length;

            if (i == 0) { // Inputs Label (Bracket style)
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 24));
                g2.drawString("784", x - 100, 210);

                g2.setStroke(new BasicStroke(2));
                int topY = neuronY[i][0] - 15;
                int botY = neuronY[i][visibleCount - 1] + 15;
                int bracketX = x - 45;

                // Draw a bracket "["
                g2.drawLine(bracketX, topY, bracketX - 10, topY); // Top tip
                g2.drawLine(bracketX, botY, bracketX - 10, botY); // Bot tip
                g2.drawLine(bracketX, topY, bracketX, botY); // Main vertical line
            }

            for (int n = 0; n < visibleCount; n++) {
                int y = neuronY[i][n];
                double val = layer.getNeurons().get(n).getValue();
                float act = (float) Math.max(0, Math.min(1, val));

                // Dot dot dot for capped layers
                if (isCapped[i] && n == visibleCount / 2) {
                    g2.setColor(Color.WHITE);
                    g2.fillOval(x - 2, y - 15, 4, 4);
                    g2.fillOval(x - 2, y, 4, 4);
                    g2.fillOval(x - 2, y + 15, 4, 4);
                    continue;
                }

                // Neuron Circle
                g2.setColor(BG_COLOR);
                g2.fillOval(x - neuronRadius, y - neuronRadius, neuronSize, neuronSize);

                // Activation Glow/Fill
                if (act > 0.05f) {
                    g2.setColor(new Color(255, 255, 255, (int) (act * 255)));
                    g2.fillOval(x - neuronRadius + 2, y - neuronRadius + 2, neuronSize - 4, neuronSize - 4);
                }

                g2.setColor(NEURON_BORDER);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x - neuronRadius, y - neuronRadius, neuronSize, neuronSize);

                // Output labels (0-9 or A-Z)
                if (i == layerCount - 1) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 16));
                    String labelStr = isLetterMode ? String.valueOf((char) ('A' + n)) : String.valueOf(n);
                    g2.drawString(labelStr, x + 25, y + 7);
                }
            }
        }
    }

    public static Visualizer show(Network network) {
        JFrame frame = new JFrame("Neural Network Training Visualizer");
        Visualizer panel = new Visualizer(network);
        frame.getContentPane().add(panel);
        frame.setBackground(BG_COLOR);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        panel.offsetX = frame.getWidth() / 2.0 - 400; // Better initial centering
        panel.offsetY = frame.getHeight() / 2.0 - 50;
        new Timer(50, e -> panel.repaint()).start();
        return panel;
    }
}
