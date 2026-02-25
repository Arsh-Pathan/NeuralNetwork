package io.arsh.tests;

import io.arsh.Network;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;

public class PongTest {

    private static final int GAME_WIDTH = 800;
    private static final int GAME_HEIGHT = 600;
    private static final int BALL_SIZE = 15;
    private static final int PADDLE_HEIGHT = 80;
    private static final int PADDLE_WIDTH = 10;
    private static final double PADDLE_MAX_VELOCITY = 30.0;
    private static final double TIMESTEP = 0.001;
    private static final Random random = new Random();

    private static final int DELAY_STEPS = 80;
    private double ballX = GAME_WIDTH / 2.0;
    private double ballY = GAME_HEIGHT / 2.0;
    private double ballVelX = 3.0;
    private double ballVelY = 3.0;
    private double playerPaddleY = GAME_HEIGHT / 2.0 - PADDLE_HEIGHT / 2.0;
    private double opponentPaddleY = GAME_HEIGHT / 2.0 - PADDLE_HEIGHT / 2.0;

    private final Network playerNetwork;
    private final Network opponentNetwork;
    private double playerError = 0.0;
    private double opponentError = 0.0;
    private int playerScore = 0;
    private int opponentScore = 0;
    private int gameCount = 0;

    private int playerDelayTimer = 0;
    private int opponentDelayTimer = 0;


    public PongTest() {
        this.playerNetwork = new Network(4, 20, 10, 5, 1).show();
        this.playerNetwork.setLearningRate(0.005);

        this.opponentNetwork = new Network(4, 20, 10, 5, 1).show();
        this.opponentNetwork.setLearningRate(0.005);

        setupGUI();
    }

    private double[] updateSide(Network network, double currentPaddleY, double targetPaddleX, boolean isPlayer, int delayTimer) {
        if (delayTimer > 0) {
            return new double[]{0.0, 0.0};
        }

        double[] input = getDoubles(currentPaddleY);

        double paddleCenter = currentPaddleY + PADDLE_HEIGHT / 2.0;
        double yDiff = ballY - paddleCenter;

        double idealPaddleVel = Math.max(-1.0, Math.min(1.0, yDiff / (PADDLE_HEIGHT / 2.0)));
        double[] target = new double[]{(idealPaddleVel + 1.0) / 2.0};

        double[] output = network.forward(input);

        network.train(input, target);
        double error = Math.pow(target[0] - output[0], 2);

        double normalizedNetworkVel = output[0] * 2.0 - 1.0;
        double actualPaddleVel = normalizedNetworkVel * PADDLE_MAX_VELOCITY * TIMESTEP * 60.0;

        return new double[]{actualPaddleVel, error};
    }

    private double[] getDoubles(double currentPaddleY) {
        double normalizedBallY = ballY / GAME_HEIGHT;
        double normalizedBallVelX = (ballVelX ) / 10.0;
        double normalizedBallVelY = (ballVelY ) / 10.0;
        double normalizedPaddleY = currentPaddleY / (GAME_HEIGHT - PADDLE_HEIGHT);

        return new double[]{
                normalizedBallY,
                normalizedBallVelX,
                normalizedBallVelY,
                normalizedPaddleY
        };
    }

    private void updateGameAndTrain() {
        if (playerDelayTimer > 0) playerDelayTimer--;
        if (opponentDelayTimer > 0) opponentDelayTimer--;

        double[] playerResults = updateSide(playerNetwork, playerPaddleY, GAME_WIDTH - 30, true, playerDelayTimer);
        double playerVel = playerResults[0];
        playerError = playerResults[1];
        playerPaddleY += playerVel;
        playerPaddleY = Math.max(0, Math.min(GAME_HEIGHT - PADDLE_HEIGHT, playerPaddleY));

        double[] opponentResults = updateSide(opponentNetwork, opponentPaddleY, 20, false, opponentDelayTimer);
        double opponentVel = opponentResults[0];
        opponentError = opponentResults[1];
        opponentPaddleY += opponentVel;
        opponentPaddleY = Math.max(0, Math.min(GAME_HEIGHT - PADDLE_HEIGHT, opponentPaddleY));

        ballX += ballVelX;
        ballY += ballVelY;

        if (ballY < 0 || ballY > GAME_HEIGHT - BALL_SIZE) {
            ballVelY *= -1;
        }

        if (ballX > GAME_WIDTH - 30 - BALL_SIZE && ballY > playerPaddleY && ballY < playerPaddleY + PADDLE_HEIGHT) {
            ballVelX *= -1.0;
            ballVelY += (random.nextDouble() * 2.0 - 1.0) * 0.5;
            ballVelX *= 1.05;
            playerScore++;

            opponentDelayTimer = DELAY_STEPS;
        }

        if (ballX < 20 + PADDLE_WIDTH && ballY > opponentPaddleY && ballY < opponentPaddleY + PADDLE_HEIGHT) {
            ballVelX *= -1.0;
            ballVelY += (random.nextDouble() * 2.0 - 1.0) * 0.5;
            ballVelX *= 1.05;
            opponentScore++;

            playerDelayTimer = DELAY_STEPS;
        }

        if (ballX > GAME_WIDTH) {
            opponentScore++;
            gameCount++;
            resetBall(true);
        }

        if (ballX < 0) {
            playerScore++;
            gameCount++;
            resetBall(false);
        }
    }

    private void resetBall(boolean towardsPlayer) {
        ballX = GAME_WIDTH / 2.0;
        ballY = random.nextDouble() * (GAME_HEIGHT - 2 * BALL_SIZE) + BALL_SIZE;
        ballVelX = (towardsPlayer ? 3.0 : -3.0);
        ballVelY = random.nextDouble() * 4.0 - 2.0;

        playerDelayTimer = 0;
        opponentDelayTimer = 0;
    }

    private class PongPainter extends JPanel {
        public PongPainter() {
            setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(Color.DARK_GRAY);
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            g2d.drawLine(GAME_WIDTH / 2, 0, GAME_WIDTH / 2, GAME_HEIGHT);

            g2d.setColor(Color.WHITE);
            g2d.fill(new Ellipse2D.Double(ballX, ballY, BALL_SIZE, BALL_SIZE));

            g2d.setColor(playerDelayTimer > 0 ? Color.YELLOW : Color.WHITE);
            g2d.fillRect(GAME_WIDTH - 30, (int) playerPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);

            g2d.setColor(opponentDelayTimer > 0 ? Color.YELLOW : Color.WHITE);
            g2d.fillRect(20, (int) opponentPaddleY, PADDLE_WIDTH, PADDLE_HEIGHT);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 18));

            g2d.drawString("RED (Opponent)", 10, 30);
            g2d.drawString("Score: " + opponentScore, 10, 50);
            g2d.drawString("MSE: " + String.format("%.6f", opponentError), 10, 70);
            g2d.drawString("Delay: " + opponentDelayTimer, 10, 90);


            g2d.drawString("GREEN (Player)", GAME_WIDTH - 200, 30);
            g2d.drawString("Score: " + playerScore, GAME_WIDTH - 200, 50);
            g2d.drawString("MSE: " + String.format("%.6f", playerError), GAME_WIDTH - 200, 70);
            g2d.drawString("Delay: " + playerDelayTimer, GAME_WIDTH - 200, 90);


            g2d.drawString("Game Count: " + gameCount, GAME_WIDTH / 2 - 60, 30);
        }
    }

    private void setupGUI() {
        JFrame frame = new JFrame("Neural Network Pong Player (Delayed Asymmetric Play)");
        PongPainter painter = new PongPainter();
        frame.add(painter, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Timer((int) (TIMESTEP * 1000), e -> {
            updateGameAndTrain();
            painter.repaint();
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PongTest::new);
    }
}