package io.arsh.utils;

public class ReLU {
    private static final double ALPHA = 0.01;

    public static double calculate(double x) {
        return Math.max(x * ALPHA, x);
    }

    public static double calculateDerivative(double x) {
        return x > 0 ? 1.0 : ALPHA;
    }

    public static double[] applyArray(double[] z) {
        double[] out = new double[z.length];
        for (int i = 0; i < z.length; i++)
            out[i] = calculate(z[i]);
        return out;
    }
}