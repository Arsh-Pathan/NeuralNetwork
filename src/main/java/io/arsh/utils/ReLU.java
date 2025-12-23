package io.arsh.utils;

public class ReLU {
    private static final double ALPHA = 0.01;

    public static double calculate(double x) {
        return Math.max(x * ALPHA, x);
    }

    public static double calculateDerivative(double x) {
        return x > 0 ? 1.0 : ALPHA;
    }
}