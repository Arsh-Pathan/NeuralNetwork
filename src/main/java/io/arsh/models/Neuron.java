package io.arsh.models;

import java.io.Serial;
import java.io.Serializable;

public class Neuron implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private double[] weights;
    private double bias;
    private double value;
    private double z;   // weighted sum BEFORE activation
    private double gradient;

    public Neuron(double[] weights, double bias, double value, double gradient) {
        this.weights = weights;
        this.bias = bias;
        this.value = value;
        this.gradient = gradient;
    }

    public double[] getWeights() {return weights;}
    public void setWeights(double[] weights) {this.weights = weights;}
    public double getGradient() {return gradient;}
    public void setGradient(double gradient) {this.gradient = gradient;}
    public double getValue() {return value;}
    public void setValue(double value) {this.value = value;}

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getBias() {return bias;}
    public void setBias(double bias) {this.bias = bias;}
}
