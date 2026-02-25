package io.arsh.tests;

import io.arsh.Network;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

public class TwoDFunctionTest {

    @FunctionalInterface
    public interface TargetFunction2D {
        double calculate(double x, double y);
    }

    private static final TargetFunction2D GAUSSIAN_2D_FUNCTION = (x, y) -> {
        double centerX = (x * 4.0) - 2.0;
        double centerY = (y * 4.0) - 2.0;

        double sigma = 1.0;
        double exponent = -(Math.pow(centerX, 2) + Math.pow(centerY, 2)) / (2.0 * Math.pow(sigma, 2));
        return Math.exp(exponent);
    };

    private static final TargetFunction2D RIPPLE_FUNCTION = (x, y) -> {
        double distance = Math.sqrt(Math.pow(x - 0.5, 2) + Math.pow(y - 0.5, 2));
        return 0.5 + 0.5 * Math.sin(distance * 20.0);
    };

    private static final TargetFunction2D CHECKERBOARD_FUNCTION = (x, y) -> {
        int cells = 8;
        int ix = (int) (x * cells);
        int iy = (int) (y * cells);
        return (ix + iy) % 2 == 0 ? 1.0 : 0.0;
    };

    private static final TargetFunction2D COMPLEX_PATTERN_FUNCTION = (x, y) -> {
        // Clamp to [0, 1] just in case
        x = Math.max(0.0, Math.min(1.0, x));
        y = Math.max(0.0, Math.min(1.0, y));

        // High-frequency checkerboard
        int cells = 10;
        int ix = (int) (x * cells);
        int iy = (int) (y * cells);
        double checker = ((ix + iy) % 2 == 0) ? 1.0 : -1.0;

        // Radial ripple centered at (0.5, 0.5)
        double dx = x - 0.5;
        double dy = y - 0.5;
        double r = Math.sqrt(dx * dx + dy * dy);
        double ripple = Math.sin(20 * r);

        // Diagonal wave interaction
        double diagonal = Math.cos(12 * (x + y));

        // Circular cutoff (sharp decision boundary)
        double circle = r < 0.35 ? 1.0 : -1.0;

        // Nonlinear combination
        double value =
                0.4 * checker +
                        0.3 * ripple +
                        0.2 * diagonal +
                        0.1 * circle;

        // Squash to [0, 1]
        return 1.0 / (1.0 + Math.exp(-5 * value));
    };


    private static final TargetFunction2D MULTI_PEAK_FUNCTION = (x, y) -> {
        double p1 = Math.exp(-15 * (Math.pow(x - 0.2, 2) + Math.pow(y - 0.2, 2)));
        double p2 = Math.exp(-20 * (Math.pow(x - 0.8, 2) + Math.pow(y - 0.8, 2)));
        double p3 = Math.exp(-25 * (Math.pow(x - 0.5, 2) + Math.pow(y - 0.7, 2)));
        return Math.min(1.0, p1 + p2 + p3);
    };

    private static final TargetFunction2D SPIRAL_FUNCTION = (x, y) -> {
        double dx = x - 0.5;
        double dy = y - 0.5;
        double r = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.atan2(dy, dx);
        return Math.abs(Math.sin(angle * 3.0 + r * 15.0)) > 0.7 ? 1.0 : 0.0;
    };

    private static final TargetFunction2D SOFT_CIRCLE = (x, y) -> {
        double dx = x - 0.5;
        double dy = y - 0.5;
        double r = Math.sqrt(dx * dx + dy * dy);
        return 1.0 / (1.0 + Math.exp(20 * (r - 0.35)));
    };


    private static final TargetFunction2D SPIRAL_MOSAIC_FUNCTION = (x, y) -> {
        // Clamp inputs
        x = Math.max(0.0, Math.min(1.0, x));
        y = Math.max(0.0, Math.min(1.0, y));

        // Center coordinates
        double cx = x - 0.5;
        double cy = y - 0.5;

        // Polar coordinates
        double r = Math.sqrt(cx * cx + cy * cy);
        double theta = Math.atan2(cy, cx);

        // Spiral wave
        double spiral = Math.sin(10 * theta + 20 * r);

        // Angular slicing (hard boundaries)
        int slices = 12;
        double slice = ((int) ((theta + Math.PI) / (2 * Math.PI) * slices)) % 2 == 0 ? 1.0 : -1.0;

        // Local Gaussian islands
        double island1 = Math.exp(-60 * ((x - 0.25) * (x - 0.25) + (y - 0.75) * (y - 0.75)));
        double island2 = Math.exp(-80 * ((x - 0.75) * (x - 0.75) + (y - 0.25) * (y - 0.25)));

        // Radial cutoff ring
        double ring = (r > 0.2 && r < 0.4) ? 1.0 : -1.0;

        // Combine components
        double value =
                0.35 * spiral +
                        0.25 * slice +
                        0.20 * ring +
                        0.10 * island1 +
                        0.10 * island2;

        return 0.5 + 0.5 * Math.tanh(4 * value);
    };


    private static final TargetFunction2D XOR_2D_FUNCTION = (x, y) -> {
        return (x > 0.5) ^ (y > 0.5) ? 1.0 : 0.0;
    };

    private static class TwoDHeatmapPainter extends JPanel {

        private final Network network;
        private final TargetFunction2D targetFunction;
        private final BufferedImage image;
        private final int RESOLUTION = 64;
        private double currentError = 0.0;
        private final boolean isTarget;

        public TwoDHeatmapPainter(Network network, TargetFunction2D targetFunction, boolean isTarget) {
            this.network = network;
            this.targetFunction = targetFunction;
            this.isTarget = isTarget;
            this.setPreferredSize(new Dimension(RESOLUTION * 5, RESOLUTION * 5));
            this.image = new BufferedImage(RESOLUTION, RESOLUTION, BufferedImage.TYPE_INT_RGB);
            this.setBackground(Color.DARK_GRAY);
            updateImage();
        }

        public void setCurrentError(double error) {
            this.currentError = error;
        }

        private Color valueToColor(double value) {
            value = Math.max(0, Math.min(1, value));
            float hue = (float) (0.66 * (1.0 - value));
            return Color.getHSBColor(hue, 1.0f, 1.0f);
        }

        public void updateImage() {
            synchronized(network) {
                for (int i = 0; i < RESOLUTION; i++) {
                    for (int j = 0; j < RESOLUTION; j++) {
                        double x = (double) i / (RESOLUTION - 1);
                        double y = (double) j / (RESOLUTION - 1);

                        double z;
                        if (isTarget) {
                            z = targetFunction.calculate(x, y);
                        } else {
                            double[] output = network.forward(new double[]{x, y});
                            z = output[0];
                        }

                        image.setRGB(i, j, valueToColor(z).getRGB());
                    }
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);

            g2d.setColor(Color.WHITE);
            String title = isTarget ? "Target Function (Gaussian)" : "Network Approximation";
            g2d.drawString(title, 10, 20);

            if (!isTarget) {
                g2d.drawString("Current MSE: " + String.format("%.6f", currentError), 10, 40);
            }
        }
    }

    public static void main(String[] args) {
        Network network = new Network(2, 40, 20, 2, 1).show();
        network.setLearningRate(0.001);

        JFrame frame = new JFrame("Live 2D Function Approximation Test (Gaussian)");

        TwoDHeatmapPainter targetPainter = new TwoDHeatmapPainter(network, CHECKERBOARD_FUNCTION, true);

        TwoDHeatmapPainter approxPainter = new TwoDHeatmapPainter(network, CHECKERBOARD_FUNCTION, false);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, targetPainter, approxPainter);
        splitPane.setResizeWeight(0.5);

        frame.add(splitPane);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        Network finalNetwork = network;
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(e.getWindow(),
                        "Are you sure you want to exit?", "Confirm Exit",
                        JOptionPane.YES_NO_OPTION);

                if (result == JOptionPane.YES_OPTION) {
                    try {
                        finalNetwork.save("C:\\Users\\ArshPathan\\OneDrive\\Desktop\\models\\model1.bin");
                        System.out.println("Model Saved!");
                        System.exit(0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Network finalNetwork1 = network;
        new Thread(() -> runTraining(finalNetwork1, approxPainter, CHECKERBOARD_FUNCTION)).start();

        new Timer(100, e -> {
            approxPainter.updateImage();
            approxPainter.repaint();
            targetPainter.repaint();
        }).start();

    }

    private static void runTraining(Network network, TwoDHeatmapPainter approxPainter, TargetFunction2D targetFunction) {
        Random random = new Random();
        int iterationCount = 0;
        int repaintInterval = 1000;
        double totalError = 0.0;
        int errorCount = 0;

        while (true) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            double zTarget = targetFunction.calculate(x, y);

            double[] input = new double[]{x, y};
            double[] target = new double[]{zTarget};

            double[] output = network.forward(input);
            synchronized(network) {
                network.train(input, target);
            }
            network.train(input, target);

            double error = Math.pow(zTarget - output[0], 2);
            totalError += error;
            errorCount++;
            iterationCount++;

            if (iterationCount % repaintInterval == 0) {
                double averageMSE = totalError / errorCount;
                approxPainter.setCurrentError(averageMSE);

                totalError = 0.0;
                errorCount = 0;

                System.out.printf("2D Test | Iterations: %d | Avg MSE: %.6f\n", iterationCount, averageMSE);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}