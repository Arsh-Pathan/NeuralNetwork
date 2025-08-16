package io.arsh.utils;

public class Sigmoid {
    public static double calculate(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    public static double calculateDerivative(double y) {return y * (1.0 - y);}
}
