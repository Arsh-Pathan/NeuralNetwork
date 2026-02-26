package io.arsh.visualizer;

import io.arsh.Network;
import io.arsh.models.Layer;
import io.arsh.models.Neuron;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.List;

public class Visualizer extends JPanel {

    private final Network network;
    private final DecimalFormat df = new DecimalFormat("#.##");

    private double scale = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    private Point lastMouse;

    public Visualizer(Network network) {
        this.network = network;
        setPreferredSize(new Dimension(1400, 900));
        setBackground(new Color(20, 22, 26));

        enableZoomAndPan();
    }

    private void enableZoomAndPan() {

        addMouseWheelListener(e -> {
            double zoomFactor = 1.1;
            double oldScale = scale;

            if (e.getPreciseWheelRotation() < 0) {
                scale *= zoomFactor;
            } else {
                scale /= zoomFactor;
            }

            scale = Math.max(0.2, Math.min(5.0, scale));

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
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        AffineTransform oldTransform = g2.getTransform();
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        drawNetwork(g2);

        g2.setTransform(oldTransform);
    }

    private void drawNetwork(Graphics2D g2) {

        List<Layer> layers = network.getLayers();
        int layerCount = layers.size();
        int width = getWidth();
        int height = getHeight();

        int marginX = 100;
        int marginY = 80;
        int availableWidth = width - 2 * marginX;
        int layerSpacing = availableWidth / Math.max(1, layerCount - 1);

        int maxNeurons = layers.stream()
                .mapToInt(l -> l.getNeurons().size())
                .max().orElse(1);

        int availableHeight = height - 2 * marginY;
        int neuronSpacing = Math.max(20, availableHeight / Math.max(1, maxNeurons - 1));
        int neuronRadius = Math.max(10, Math.min(18, neuronSpacing / 2));

        for (int i = 0; i < layerCount; i++) {
            Layer layer = layers.get(i);
            int neuronCount = layer.getNeurons().size();
            int layerX = marginX + i * layerSpacing;
            int totalHeight = (neuronCount - 1) * neuronSpacing;
            int startY = (height - totalHeight) / 2;

            // Connections
            if (i > 0) {
                Layer prev = layers.get(i - 1);
                int prevX = marginX + (i - 1) * layerSpacing;
                int prevCount = prev.getNeurons().size();
                int prevTotalHeight = (prevCount - 1) * neuronSpacing;
                int prevStartY = (height - prevTotalHeight) / 2;

                for (int n = 0; n < neuronCount; n++) {
                    Neuron neuron = layer.getNeurons().get(n);
                    int y1 = startY + n * neuronSpacing;

                    double[] weights = neuron.getWeights();
                    for (int w = 0; w < weights.length; w++) {
                        double weight = weights[w];
                        int y0 = prevStartY + w * neuronSpacing;

                        float intensity = (float) Math.min(1.0, Math.abs(weight));
                        int alpha = (int) (80 + 175 * intensity);

                        Color color = weight >= 0
                                ? new Color(70, 170, 255, alpha)
                                : new Color(255, 255, 255, alpha);

                        g2.setColor(color);
                        g2.setStroke(new BasicStroke(0.8f + 2f * intensity));
                        g2.drawLine(prevX + neuronRadius, y0, layerX - neuronRadius, y1);
                    }
                }
            }

            for (int n = 0; n < neuronCount; n++) {
                int y = startY + n * neuronSpacing;
                Neuron neuron = layer.getNeurons().get(n);
                float act = (float) Math.max(0, Math.min(1, neuron.getValue()));

                Color bg = getBackground();
                Color fill = (act < 0.05f) ? bg
                        : new Color(
                                (int) (bg.getRed() + act * (120 - bg.getRed())),
                                (int) (bg.getGreen() + act * (200 - bg.getGreen())),
                                (int) (bg.getBlue() + act * (255 - bg.getBlue())));

                g2.setColor(fill);
                g2.fillOval(layerX - neuronRadius, y - neuronRadius,
                        neuronRadius * 2, neuronRadius * 2);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.3f));
                g2.drawOval(layerX - neuronRadius, y - neuronRadius,
                        neuronRadius * 2, neuronRadius * 2);

                g2.setFont(new Font("Consolas", Font.BOLD, 10));
                String val = df.format(neuron.getValue());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, layerX - fm.stringWidth(val) / 2,
                        y + fm.getAscent() / 2 - 2);
            }

            g2.setFont(new Font("Consolas", Font.BOLD, 12));
            g2.setColor(new Color(200, 200, 200, 200));
            String label = (i == 0) ? "Input" : (i == layerCount - 1 ? "Output" : "Hidden " + i);
            g2.drawString(label,
                    layerX - g2.getFontMetrics().stringWidth(label) / 2, 25);
        }
    }

    public static void show(Network network) {
        JFrame frame = new JFrame("Neural Network Visualizer");
        Visualizer panel = new Visualizer(network);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JScrollPane(panel));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);

        new Timer(1000 / 30, e -> panel.repaint()).start();
    }
}
