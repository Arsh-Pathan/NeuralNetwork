package io.arsh;

public class Neuron {

    private double[] weights;
    private double bias;
    private double value;
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
    public double getBias() {return bias;}
    public void setBias(double bias) {this.bias = bias;}

}
