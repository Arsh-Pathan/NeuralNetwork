package io.arsh.models;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Layer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Neuron> neurons = new ArrayList<>();

    public Layer(int neuronCount, int inputCount) {
        Random random = new Random();

        for (int i = 0; i < neuronCount; i++) {
            double[] weights = new double[inputCount];
            double stdDev = (inputCount > 0) ? Math.sqrt(2.0 / inputCount) : 0.1;

            for (int j = 0; j < inputCount; j++) {
                weights[j] = random.nextGaussian() * stdDev;
            }
            double bias = 0.1;
            neurons.add(new Neuron(weights, bias, 0.0, 0.0));
        }
    }

    public List<Neuron> getNeurons() { return neurons; }

    public void setValues(double[] values) {
        for (int i = 0; i < neurons.size(); i++) {
            neurons.get(i).setValue(values[i]);
        }
    }

    public double[] getValues() {
        double[] values = new double[neurons.size()];
        for (int i = 0; i < neurons.size(); i++) {
            values[i] = neurons.get(i).getValue();
        }
        return values;
    }
}