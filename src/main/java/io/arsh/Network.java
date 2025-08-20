package io.arsh;

import io.arsh.utils.ReLU;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Network implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final List<Layer> layers = new ArrayList<>();
    private double learningRate = 0.1;

    private double[][] lastActivations;

    public Network(int... layerSizes) {
        for (int i = 0; i < layerSizes.length; i++) {
            int neuronCount = layerSizes[i];
            int inputCount = (i == 0) ? 0 : layerSizes[i - 1];
            layers.add(new Layer(neuronCount, inputCount));
        }
    }

    public void setLearningRate(double rate) {
        this.learningRate = rate;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public double[][] getLastActivations() {
        return lastActivations;
    }

    public double[] forward(double[] input) {
        layers.getFirst().setValues(input);

        lastActivations = new double[layers.size()][];
        lastActivations[0] = input.clone();

        for (int i = 1; i < layers.size(); i++) {
            Layer prevLayer = layers.get(i - 1);
            Layer currentLayer = layers.get(i);

            double[] prevValues = prevLayer.getValues();
            double[] currentValues = new double[currentLayer.getNeurons().size()];

            for (int n = 0; n < currentLayer.getNeurons().size(); n++) {
                Neuron neuron = currentLayer.getNeurons().get(n);
                double sum = 0.0;
                double[] weights = neuron.getWeights();
                for (int w = 0; w < weights.length; w++) {
                    sum += prevValues[w] * weights[w];
                }
                sum += neuron.getBias();
                double activated = ReLU.calculate(-sum);
                neuron.setValue(activated);
                currentValues[n] = activated;
            }

            lastActivations[i] = currentValues;
        }

        return layers.getLast().getValues();
    }

    public void train(double[] input, double[] target) {
        double[] output = forward(input);

        Layer outputLayer = layers.getLast();
        for (int i = 0; i < outputLayer.getNeurons().size(); i++) {
            Neuron neuron = outputLayer.getNeurons().get(i);
            double error = target[i] - neuron.getValue();
            neuron.setGradient(error * ReLU.calculateDerivative(neuron.getValue()));
        }

        for (int layerIndex = layers.size() - 2; layerIndex > 0; layerIndex--) {
            Layer currentLayer = layers.get(layerIndex);
            Layer nextLayer = layers.get(layerIndex + 1);

            for (int i = 0; i < currentLayer.getNeurons().size(); i++) {
                double sum = 0.0;
                for (Neuron nextNeuron : nextLayer.getNeurons()) {
                    sum += nextNeuron.getWeights()[i] * nextNeuron.getGradient();
                }
                currentLayer.getNeurons().get(i).setGradient(
                        sum * ReLU.calculateDerivative(currentLayer.getNeurons().get(i).getValue())
                );
            }
        }

        for (int layerIndex = 1; layerIndex < layers.size(); layerIndex++) {
            Layer prevLayer = layers.get(layerIndex - 1);
            Layer currentLayer = layers.get(layerIndex);

            for (Neuron neuron : currentLayer.getNeurons()) {
                for (int w = 0; w < neuron.getWeights().length; w++) {
                    double delta = learningRate * neuron.getGradient() * prevLayer.getValues()[w];
                    neuron.getWeights()[w] += delta;
                }
                neuron.setBias(neuron.getBias() + learningRate * neuron.getGradient());
            }
        }
    }

    public void save(String path) throws IOException {
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(this);
        }
    }

    public static Network load(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Network) in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}
