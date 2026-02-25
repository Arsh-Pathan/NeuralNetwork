package io.arsh.tests;

import io.arsh.Network;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class PolynomialTest {

    // --- 1. Interface for the Target Function ---

    @FunctionalInterface
    public interface TargetFunction {
        double calculate(double x);
    }

    private static TargetFunction POLYNOMIAL_FUNCTION() {
        Random r = new Random();

        // random coefficients (centered so shapes vary a lot)
        double a = r.nextDouble() * 2 - 1;
        double b = r.nextDouble() * 2 - 1;
        double c = r.nextDouble() * 2 - 1;
        double d = r.nextDouble() * 2 - 1;
        double e = r.nextDouble() * 2 - 1;

        System.out.printf(
                "Polynomial coefficients: a=%.3f b=%.3f c=%.3f d=%.3f e=%.3f%n",
                a, b, c, d, e
        );

        return x -> {
            double z = (x * 2.0) - 1.0; // normalize [-1,1]

            double value =
                    a * Math.pow(z, 5) +
                            b * Math.pow(z, 4) +
                            c * Math.pow(z, 3) +
                            d * Math.pow(z, 2) +
                            e * z +
                            0.5;

            return Math.max(0.0, Math.min(1.0, value));
        };
    }

    private static class NetworkPainter extends JPanel {

        private final Network network;
        private final TargetFunction targetFunction;
        private static final int PADDING = 20;
        private double currentError = 0.0;

        public NetworkPainter(Network network, TargetFunction targetFunction) {
            this.network = network;
            this.targetFunction = targetFunction;
            this.setPreferredSize(new Dimension(800, 600));
            this.setBackground(Color.WHITE);
        }

        public void setCurrentError(double error) {
            this.currentError = error;
        }

        private int mapX(double x) {
            return (int) (PADDING + x * (getWidth() - 2 * PADDING));
        }

        private int mapY(double y) {
            return (int) (PADDING + (1 - y) * (getHeight() - 2 * PADDING));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw Axis and Labels
            g2d.setColor(Color.LIGHT_GRAY);
            int zeroY = mapY(0.0);
            int zeroX = mapX(0.0);
            g2d.drawLine(zeroX, zeroY, mapX(1.0), zeroY);
            g2d.drawLine(zeroX, zeroY, zeroX, mapY(1.0));
            g2d.setColor(Color.BLACK);
            g2d.drawString("0", zeroX - 10, zeroY + 15);
            g2d.drawString("1", mapX(1.0) - 10, zeroY + 15);
            g2d.drawString("1", zeroX - 15, mapY(1.0) + 5);

            // 2. Draw Target Function (Green line)
            g2d.setColor(new Color(0, 100, 0));
            g2d.setStroke(new BasicStroke(2));
            double step = 0.005;
            int prevX = -1;
            int prevY = -1;
            for (double x = 0; x <= 1.0; x += step) {
                double y = targetFunction.calculate(x);
                int currentX = mapX(x);
                int currentY = mapY(y);

                if (prevX != -1) {
                    g2d.drawLine(prevX, prevY, currentX, currentY);
                }
                prevX = currentX;
                prevY = currentY;
            }

            // 3. Draw Network Approximation (Red line)
            g2d.setColor(new Color(200, 0, 0));
            g2d.setStroke(new BasicStroke(2));
            prevX = -1;
            prevY = -1;
            for (double x = 0; x <= 1.0; x += step) {
                double[] output = network.forward(new double[]{x});
                double yApprox = output[0];

                int currentX = mapX(x);
                int currentY = mapY(yApprox);

                if (prevX != -1) {
                    g2d.drawLine(prevX, prevY, currentX, currentY);
                }
                prevX = currentX;
                prevY = currentY;
            }

            // 4. Display training status and error
            g2d.setColor(Color.BLUE);
            g2d.drawString("Green: Target Function (High-Degree Polynomial)", mapX(0) + 10, mapY(1.0) - 10);
            g2d.drawString("Red: Network Approximation", mapX(0) + 10, mapY(1.0) + 5);
            g2d.drawString("Current Error (MSE): " + String.format("%.6f", currentError), mapX(0) + 10, mapY(1.0) + 20);
        }
    }


    // --- 3. Main Execution Logic ---

    public static void main(String[] args) {
        Network network = new Network(1, 18, 14, 8, 3, 1).show();
        network.setLearningRate(0.001);

        JFrame frame = new JFrame("Polynomial Approximation Test");
        PolynomialTest.TargetFunction fun = POLYNOMIAL_FUNCTION();
        NetworkPainter painter = new NetworkPainter(network, fun);
        frame.add(painter, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Thread(() -> runTraining(network, painter, fun)).start();
    }

    private static void runTraining(Network network, NetworkPainter painter, TargetFunction targetFunction) {
        // Training logic is identical to the StepFunctionTest,
        // using the new network and target function
        Random random = new Random();
        int iterationCount = 0;
        int repaintInterval = 2000;
        double totalError = 0.0;
        int errorCount = 0;
        while (true) {
            double x = random.nextDouble();
            double yTarget = targetFunction.calculate(x);

            double[] input = new double[]{x};
            double[] target = new double[]{yTarget};

            double[] output = network.forward(input);
            network.train(input, target);

            double error = Math.pow(yTarget - output[0], 2);
            totalError += error;
            errorCount++;

            iterationCount++;

            if (iterationCount % repaintInterval == 0) {
                double averageMSE = totalError / errorCount;
                painter.setCurrentError(averageMSE);

                totalError = 0.0;
                errorCount = 0;

                SwingUtilities.invokeLater(painter::repaint);

                System.out.printf("Polynomial Test | Iterations: %d | Avg MSE: %.6f\n", iterationCount, averageMSE);

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}