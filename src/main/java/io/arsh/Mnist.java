package io.arsh;

import io.arsh.visualizer.Visualizer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class Mnist extends JPanel {

    private final BufferedImage canvas = new BufferedImage(280, 280, BufferedImage.TYPE_BYTE_GRAY);
    private final Graphics2D g2 = canvas.createGraphics();
    private Network net;
    private PredictionPanel predictionPanel;
    private Timer clearTimer;

    private static final Color DARK_BG = new Color(20, 22, 26);
    private static final Color ACCENT = new Color(100, 200, 255);

    private boolean letterMode = false;

    public Mnist(Network net, PredictionPanel predictionPanel) {
        this.net = net;
        this.predictionPanel = predictionPanel;

        setPreferredSize(new Dimension(280, 280));
        setBackground(Color.BLACK);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 280, 280);

        MouseAdapter mouse = new MouseAdapter() {
            int lastX, lastY;

            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
                drawFeatheredPoint(lastX, lastY);
                repaint();
                if (clearTimer != null)
                    clearTimer.stop();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX(), y = e.getY();
                drawFeatheredLine(lastX, lastY, x, y);
                lastX = x;
                lastY = y;
                repaint();
                livePredict();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                livePredict();
                clearTimer = new Timer(3000, ev -> clear());
                clearTimer.setRepeats(false);
                clearTimer.start();
            }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private void drawFeatheredPoint(int x, int y) {
        g2.setColor(Color.WHITE);
        for (int i = 0; i < 5; i++) {
            float alpha = 1.0f - (i / 5.0f);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.4f));
            int r = 18 + i * 2;
            g2.fillOval(x - r / 2, y - r / 2, r, r);
        }
        g2.setComposite(AlphaComposite.SrcOver);
        g2.fillOval(x - 8, y - 8, 8, 8);
    }

    private void drawFeatheredLine(int x1, int y1, int x2, int y2) {
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1, y1, x2, y2);

        // Secondary softer
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2.setStroke(new BasicStroke(28, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1, y1, x2, y2);
        g2.setComposite(AlphaComposite.SrcOver);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, 0, 0, null);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(ACCENT);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    private double[] centerImage(double[] img) {
        double cx = 0, cy = 0, sum = 0;
        for (int y = 0; y < 28; y++)
            for (int x = 0; x < 28; x++) {
                double v = img[y * 28 + x];
                cx += x * v;
                cy += y * v;
                sum += v;
            }
        if (sum == 0)
            return img;
        cx /= sum;
        cy /= sum;
        int shiftX = (int) Math.round(14 - cx);
        int shiftY = (int) Math.round(14 - cy);

        double[] shifted = new double[784];
        for (int y = 0; y < 28; y++)
            for (int x = 0; x < 28; x++) {
                int nx = x + shiftX, ny = y + shiftY;
                if (nx >= 0 && nx < 28 && ny >= 0 && ny < 28)
                    shifted[ny * 28 + nx] = img[y * 28 + x];
            }
        return shifted;
    }

    public double[] getMNISTInput() {
        Image tmp = canvas.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        BufferedImage small = new BufferedImage(28, 28, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = small.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(tmp, 0, 0, null);
        g.dispose();

        double[] input = new double[784];
        for (int y = 0; y < 28; y++)
            for (int x = 0; x < 28; x++)
                input[y * 28 + x] = small.getRaster().getSample(x, y, 0) / 255.0;

        return centerImage(input);
    }

    private void livePredict() {
        double[] out = net.forward(getMNISTInput());
        predictionPanel.updatePredictions(out, letterMode);
    }

    public void setLetterMode(boolean mode) {
        this.letterMode = mode;
        predictionPanel.setLetterMode(mode);
        livePredict();
    }

    public void clear() {
        if (clearTimer != null)
            clearTimer.stop();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 280, 280);
        predictionPanel.clear();
        repaint();
    }

    static class PredictionPanel extends JPanel {
        private double[] probabilities = new double[10];
        private boolean letterMode = false;
        private static final Color BAR_COLOR = new Color(100, 200, 255, 180);

        PredictionPanel() {
            setPreferredSize(new Dimension(300, 280));
            setBackground(DARK_BG);
            setBorder(new EmptyBorder(10, 20, 10, 20));
        }

        void updatePredictions(double[] probs, boolean letterMode) {
            this.probabilities = probs;
            this.letterMode = letterMode;
            repaint();
        }

        void setLetterMode(boolean mode) {
            this.letterMode = mode;
            this.probabilities = new double[mode ? 26 : 10];
            repaint();
        }

        void clear() {
            this.probabilities = new double[letterMode ? 26 : 10];
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int maxIdx = 0;
            for (int i = 1; i < 10; i++)
                if (probabilities[i] > probabilities[maxIdx])
                    maxIdx = i;

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString(letterMode ? "ALPHABET PREDICTIONS" : "DIGIT PREDICTIONS", 0, 20);

            int count = letterMode ? 26 : 10;
            for (int i = 0; i < count; i++) {
                int y = 40 + i * (letterMode ? 10 : 22);
                double p = (i < probabilities.length) ? probabilities[i] : 0;

                String labelStr = letterMode ? String.valueOf((char) ('A' + i)) : String.valueOf(i);

                g2.setColor(new Color(255, 255, 255, 100));
                g2.setFont(new Font("Monospaced", Font.PLAIN, letterMode ? 9 : 12));
                g2.drawString(labelStr + ":", 0, y + (letterMode ? 8 : 12));

                // Bar BG
                g2.setColor(new Color(255, 255, 255, 20));
                g2.fillRoundRect(25, y, 200, letterMode ? 8 : 14, 4, 4);

                // Bar Fill
                if (i == maxIdx && p > 0.1)
                    g2.setColor(ACCENT);
                else
                    g2.setColor(BAR_COLOR);

                int barWidth = (int) (p * 200);
                g2.fillRoundRect(25, y, barWidth, letterMode ? 8 : 14, 4, 4);

                // Percentage
                if (!letterMode || p > 0.01) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Monospaced", Font.PLAIN, letterMode ? 9 : 12));
                    g2.drawString(String.format("%.1f%%", p * 100), 235, y + (letterMode ? 8 : 12));
                }
            }
        }
    }

    public static Mnist open(Network net) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        JFrame frame = new JFrame("Neural Intuition - AI Visualizer");
        frame.getContentPane().setBackground(DARK_BG);

        PredictionPanel predPanel = new PredictionPanel();
        Mnist canvasPanel = new Mnist(net, predPanel);

        JPanel mainContent = new JPanel(new BorderLayout(20, 0));
        mainContent.setBackground(DARK_BG);
        mainContent.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainContent.add(canvasPanel, BorderLayout.WEST);
        mainContent.add(predPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(DARK_BG);

        JButton clearBtn = new JButton("CLEAR");
        JButton modeBtn = new JButton("SWITCH TO ALPHABET");
        JButton vizBtn = new JButton("NETWORK VISUALIZER");

        styleButton(clearBtn);
        styleButton(modeBtn);
        styleButton(vizBtn);

        clearBtn.addActionListener(e -> canvasPanel.clear());
        modeBtn.addActionListener(e -> {
            boolean switchToAlphabet = modeBtn.getText().contains("ALPHABET");
            String modelFile = switchToAlphabet ? "letters.nn" : "mnist.nn";
            Network newNet = Network.load(modelFile);

            if (newNet != null) {
                canvasPanel.net = newNet;
                canvasPanel.setLetterMode(switchToAlphabet);
                modeBtn.setText(switchToAlphabet ? "SWITCH TO DIGITS" : "SWITCH TO ALPHABET");
                canvasPanel.clear();
            } else {
                JOptionPane.showMessageDialog(frame, "Model file '" + modelFile + "' not found. Train it first!");
            }
        });
        vizBtn.addActionListener(e -> {
            Visualizer v = Visualizer.show(canvasPanel.net);
            v.setLetterMode(canvasPanel.letterMode);
        });

        bottomPanel.add(clearBtn);
        bottomPanel.add(modeBtn);
        bottomPanel.add(vizBtn);

        frame.setLayout(new BorderLayout());
        frame.add(mainContent, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        return canvasPanel;
    }

    private static void styleButton(JButton btn) {
        btn.setBackground(new Color(45, 48, 52));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 73, 75)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
    }

    public static void main(String[] args) {
        boolean letterMode = args.length > 0 && args[0].equalsIgnoreCase("letters");
        String modelFile = letterMode ? "letters.nn" : "mnist.nn";

        Network net = Network.load(modelFile);
        if (net == null) {
            System.out.println("Model file '" + modelFile + "' not found.");
            System.out.println("Run Train" + (letterMode ? "Letters" : "Mnist") + " first.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            Mnist app = Mnist.open(net);
            if (letterMode) {
                app.setLetterMode(true);
            }
        });
    }
}
