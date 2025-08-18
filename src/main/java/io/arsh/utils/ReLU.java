package io.arsh.utils;

public class ReLU {
    public static double calculate(double x) { return Math.max(0, x); }
    public static double calculateDerivative(double x) { return x > 0 ? 1 : 0; }
}
