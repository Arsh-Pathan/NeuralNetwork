package io.arsh.tests;

import io.arsh.Network;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class FunctionApproximationTest {

    @FunctionalInterface
    public interface TargetFunction {
        double calculate(double x);
    }

    private static final TargetFunction COMPLEX_SINE_FUNCTION = x -> {
        double angle = x * 4.0 * Math.PI;
        double value = (0.8 * Math.sin(angle) + 0.4 * Math.sin(angle * 3.0));
        value += 0.5 * (x - 0.5);
        double normalized = (value + 1.2) / 2.4;
        return Math.max(0.0, Math.min(1.0, normalized));
    };

    private static final TargetFunction SIMPLE_SINE_FUNCTION = x -> {
        double angle = x * 2.0 * Math.PI;
        double value = Math.sin(angle);
        return (value + 1.0) / 2.0;
    };

    private static final TargetFunction STEP_FUNCTION = x -> {
        if (x < 0.4) {
            return 0.1;
        } else if (x < 0.8) {
            return 0.8;
        } else {
            return 0.3;
        }
    };

    private static final TargetFunction EXPONENTIAL_DECAY_FUNCTION = x -> {
        double scaledX = x * 5.0;
        double value = Math.exp(-scaledX);

        if (x > 0.45 && x < 0.55) {
            value += 0.4;
        }

        return Math.max(0.0, Math.min(1.0, value / 1.4));
    };

    private static final TargetFunction SPIRAL_APPROXIMATION_FUNCTION = x -> {
        double angle = x * 10.0 * Math.PI;
        double amplitude = 0.5 * x;
        double value = amplitude * Math.sin(angle);

        return (value + 0.5) * 0.8 + 0.1;
    };

    private static final TargetFunction TRIANGULAR_WAVE_FUNCTION = x -> {
        double value = 2.0 * Math.abs(x - 0.5);
        return 1.0 - value;
    };

    private static final TargetFunction GAUSSIAN_PEAK_FUNCTION = x -> {
        double mu = 0.5;
        double sigma = 0.15;
        double value = Math.exp(-0.5 * Math.pow((x - mu) / sigma, 2));
        return 0.1 + value * 0.8;
    };

    private static final TargetFunction INVERTED_QUADRATIC_FUNCTION = x -> {
        double value = 4.0 * x * (1.0 - x);
        return 0.1 + value * 0.8;
    };

    private static final TargetFunction SAWTOOTH_WAVE_FUNCTION = x -> {
        return x;
    };

    private static final TargetFunction CHIRP_FUNCTION = x -> {
        double frequency = 1.0 + 9.0 * x;
        double value = Math.sin(2.0 * Math.PI * frequency * x);
        return (value + 1.0) * 0.4 + 0.1;
    };

    private static final TargetFunction HIGH_FREQUENCY_NOISE_FUNCTION = x -> {
        double value = 0.5 * Math.sin(x * 20.0 * Math.PI);
        value += 0.2 * Math.sin(x * 50.0 * Math.PI);
        double normalized = (value + 1.2) / 2.4;
        return Math.max(0.0, Math.min(1.0, normalized));
    };

    private static final TargetFunction MULTIPLE_JUMPS_FUNCTION = x -> {
        if (x < 0.2) return 0.9;
        if (x < 0.4) return 0.2;
        if (x < 0.6) return 0.7;
        if (x < 0.8) return 0.4;
        return 0.1;
    };

    private static final TargetFunction ABS_SINE_COMBO_FUNCTION = x -> {
        double abs_val = 0.7 * Math.abs(x - 0.5);
        double sine_val = 0.3 * Math.sin(x * 6.0 * Math.PI);
        double value = abs_val + sine_val;
        return 1.0 - value;
    };

    private static final TargetFunction SIGMOID_TRANSITION_FUNCTION = x -> {
        double steepness = 20.0;
        double midpoint = 0.5;
        double raw_sigmoid = 1.0 / (1.0 + Math.exp(-steepness * (x - midpoint)));
        return 0.1 + raw_sigmoid * 0.8;
    };

    private static final TargetFunction[] ALL_FUNCTIONS = {
            COMPLEX_SINE_FUNCTION,
            SIMPLE_SINE_FUNCTION,
            STEP_FUNCTION,
            EXPONENTIAL_DECAY_FUNCTION,
            SPIRAL_APPROXIMATION_FUNCTION,
            TRIANGULAR_WAVE_FUNCTION,
            GAUSSIAN_PEAK_FUNCTION,
            INVERTED_QUADRATIC_FUNCTION,
            SAWTOOTH_WAVE_FUNCTION,
            CHIRP_FUNCTION,
            HIGH_FREQUENCY_NOISE_FUNCTION,
            MULTIPLE_JUMPS_FUNCTION,
            ABS_SINE_COMBO_FUNCTION,
            SIGMOID_TRANSITION_FUNCTION
    };

    private static final String[] FUNCTION_NAMES = {
            "Complex Sine",
            "Simple Sine",
            "Step Function",
            "Exponential Decay",
            "Spiral Approximation (High Freq)",
            "Triangular Wave",
            "Gaussian Peak",
            "Inverted Quadratic (Parabola)",
            "Sawtooth Wave (Linear)",
            "Chirp Wave (Increasing Freq)",
            "High Frequency Noise",
            "Multiple Discontinuous Jumps",
            "Absolute Value & Sine Combo",
            "Steep Sigmoid Transition"
    };

    private static class NetworkPainter extends JPanel {

        private final Network network;
        private final TargetFunction targetFunction;
        private final String functionName;
        private static final int PADDING = 20;
        private double currentError = 0.0;

        public NetworkPainter(Network network, TargetFunction targetFunction, String functionName) {
            this.network = network;
            this.targetFunction = targetFunction;
            this.functionName = functionName;
            this.setPreferredSize(new Dimension(800, 600));
            this.setBackground(Color.DARK_GRAY);
        }

        public void setCurrentError(double error) {
            this.currentError = error;
        }

        private int mapX(double x) {
            return (int) (PADDING + x * (getWidth() - 2 * PADDING));
        }

        private int mapY(double y) {
            return (int) (getHeight() - PADDING - y * (getHeight() - 2 * PADDING));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(Color.LIGHT_GRAY);
            int zeroY = mapY(0.0);
            g2d.drawLine(mapX(0.0), zeroY, mapX(1.0), zeroY);
            int zeroX = mapX(0.0);
            g2d.drawLine(zeroX, mapY(0.0), zeroX, mapY(1.0));

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("0", zeroX - 10, zeroY + 15);
            g2d.drawString("1 (Input)", mapX(1.0) - 40, zeroY + 15);
            g2d.drawString("1 (Output)", zeroX - 45, mapY(1.0) + 5);

            g2d.setColor(Color.WHITE);
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

            g2d.setColor(new Color(0, 225, 255));
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

            int textYStart = PADDING + 10;
            g2d.setColor(Color.WHITE);
            g2d.drawString("Target Function: " + functionName, mapX(0) + 10, textYStart);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Current Avg MSE: " + String.format("%.6f", currentError), mapX(0) + 10, textYStart + 15);
        }
    }

    public static void main(String[] args) {
        Random selector = new Random();
        int randomIndex = selector.nextInt(ALL_FUNCTIONS.length);
        TargetFunction selectedFunction = ALL_FUNCTIONS[randomIndex];
        String selectedFunctionName = FUNCTION_NAMES[randomIndex];

        System.out.println("Starting Approximation for: " + selectedFunctionName);

        Network network = new Network(1, 30, 30, 15, 1);
        network.setLearningRate(0.05);

        JFrame frame = new JFrame("Live Function Approximation: " + selectedFunctionName);
        NetworkPainter painter = new NetworkPainter(network, selectedFunction, selectedFunctionName);
        frame.add(painter, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Thread(() -> runTraining(network, painter, selectedFunction)).start();
    }

    private static void runTraining(Network network, NetworkPainter painter, TargetFunction targetFunction) {
        Random random = new Random();
        long startTime = System.currentTimeMillis();

        int iterationCount = 0;
        int repaintInterval = 5000;
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

                long currentTime = System.currentTimeMillis();
                double seconds = (currentTime - startTime) / 1000.0;
                System.out.printf("Iterations: %d | Time: %.1fs | Avg MSE: %.6f\n",
                        iterationCount, seconds, averageMSE);

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}