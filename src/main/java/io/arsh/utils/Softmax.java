package io.arsh.utils;

public class Softmax {

    public static double[] apply(double[] z) {
        double max = Double.NEGATIVE_INFINITY;
        for (double v : z)
            if (v > max) max = v;

        double sum = 0;
        double[] out = new double[z.length];

        for (int i = 0; i < z.length; i++) {
            out[i] = Math.exp(z[i] - max);
            sum += out[i];
        }

        for (int i = 0; i < z.length; i++)
            out[i] /= sum;

        return out;
    }

}
