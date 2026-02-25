package io.arsh.tests;

import io.arsh.Network;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

public class NeuralRacerTest {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 700;
    private static final int CAR_COUNT = 10;
    private static final double CAR_RADIUS = 3;

    private final List<Car> cars = new ArrayList<>();
    private final List<Line2D> walls = new ArrayList<>();

    public NeuralRacerTest() {
        createTrack();
        createCars();
        setupGUI();
    }

    private class Car {
        double x, y, angle;
        double speed = 3;
        double[] sensors = new double[5];
        double lastError = 0;
        Color color;
        Network brain;

        Car(double x, double y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.angle = Math.random() * Math.PI * 2;

            brain = new Network(5, 3, 2, 1);
            brain.setLearningRate(0.015);
        }

        void reset() {
            x = 150 + Math.random() * 50;
            y = 150 + Math.random() * 50;
            angle = Math.random() * Math.PI * 2;
        }
    }
    /* ========================= TRACK ========================= */
    private void createTrack() {
        walls.clear();
        walls.add(new Line2D.Double(40, 40, 860, 40));
        walls.add(new Line2D.Double(860, 40, 860, 660));
        walls.add(new Line2D.Double(860, 660, 40, 660));
        walls.add(new Line2D.Double(40, 660, 40, 40));
        walls.add(new Line2D.Double(160, 180, 360, 180));
        walls.add(new Line2D.Double(360, 180, 360, 460));
        walls.add(new Line2D.Double(260, 460, 260, 660));
        walls.add(new Line2D.Double(520, 180, 520, 460));
        walls.add(new Line2D.Double(560, 360, 700, 360));
    }


    private void createCars() {
        Color[] colors = {
                Color.BLUE, Color.RED, Color.GREEN,
                Color.ORANGE, Color.MAGENTA, Color.CYAN
        };

        for (int i = 0; i < CAR_COUNT; i++) {
            cars.add(new Car(
                    150 + i * 25,
                    150 + i * 15,
                    colors[i % colors.length]
            ));
        }
    }

    /* ========================= UPDATE ========================= */

    private void update() {
        for (Car car : cars) {

            double[] sensorAngles = {-0.8, -0.4, 0, 0.4, 0.8};
            for (int i = 0; i < 5; i++) {
                car.sensors[i] = castRay(car, car.angle + sensorAngles[i]);
            }

            double[] output = car.brain.forward(car.sensors);
            double steering = (output[0] * 2 - 1) * 0.15;

            // 3. Teacher
            double target;
            if (car.sensors[0] < car.sensors[4]) target = 0.8;
            else if (car.sensors[4] < car.sensors[0]) target = 0.2;
            else target = 0.5;

            car.brain.train(car.sensors, new double[]{target});
            car.lastError = Math.pow(target - output[0], 2);

            // 4. Move
            car.angle += steering;
            car.x += Math.cos(car.angle) * car.speed;
            car.y += Math.sin(car.angle) * car.speed;

            // 5. Wall collision
            for (Line2D wall : walls) {
                if (wall.ptSegDist(car.x, car.y) < CAR_RADIUS) {
                    car.reset();
                    break;
                }
            }
        }

        // 6. Car–Car collisions
        for (int i = 0; i < cars.size(); i++) {
            for (int j = i + 1; j < cars.size(); j++) {
                Car a = cars.get(i);
                Car b = cars.get(j);

                double dx = a.x - b.x;
                double dy = a.y - b.y;
                if (Math.hypot(dx, dy) < CAR_RADIUS * 2) {
                    a.reset();
                    b.reset();
                }
            }
        }
    }

    /* ========================= RAY CAST ========================= */

    private double castRay(Car car, double angle) {
        double rayX = Math.cos(angle);
        double rayY = Math.sin(angle);
        double minDist = 1.0;

        for (Line2D wall : walls) {
            double x1 = wall.getX1(), y1 = wall.getY1();
            double x2 = wall.getX2(), y2 = wall.getY2();
            double x3 = car.x, y3 = car.y;
            double x4 = car.x + rayX * 200, y4 = car.y + rayY * 200;

            double den = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
            if (den == 0) continue;

            double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / den;
            double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / den;

            if (t > 0 && t < 1 && u > 0) {
                double d = (u * 200) / 200.0;
                if (d < minDist) minDist = d;
            }
        }
        return minDist;
    }

    /* ========================= GUI ========================= */

    private void setupGUI() {
        JFrame frame = new JFrame("Neural Racer — Multi-Agent");
        JPanel canvas = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Walls
                g2.setColor(Color.black);
                g2.setStroke(new BasicStroke(3));
                walls.forEach(g2::draw);

                // Cars
                for (Car car : cars) {

                    // Sensors
                    double[] sensorAngles = {-0.8, -0.4, 0, 0.4, 0.8};
                    for (int i = 0; i < 5; i++) {
                        double d = car.sensors[i] * 200;
                        double ex = car.x + Math.cos(car.angle + sensorAngles[i]) * d;
                        double ey = car.y + Math.sin(car.angle + sensorAngles[i]) * d;
                        g2.setColor(new Color(
                                car.color.getRed(),
                                car.color.getGreen(),
                                car.color.getBlue(),
                                20
                        ));
                        g2.draw(new Line2D.Double(car.x, car.y, ex, ey));
                    }

                    // Car body
                    AffineTransform old = g2.getTransform();
                    g2.translate(car.x, car.y);
                    g2.rotate(car.angle);
                    g2.setColor(car.color);
                    g2.fillRect(-10, -5, 20, 10);
                    g2.setTransform(old);
                }
            }
        };

        canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.add(canvas);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        new Timer(16, e -> {
            update();
            canvas.repaint();
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NeuralRacerTest::new);
    }
}
