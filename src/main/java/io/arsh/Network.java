package io.arsh;

import io.arsh.models.Layer;
import io.arsh.models.Neuron;
import io.arsh.utils.ReLU;
import io.arsh.utils.Softmax;
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

    public io.arsh.visualizer.Visualizer show() {
        return io.arsh.visualizer.Visualizer.show(this);
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
            double[] zValues = new double[currentLayer.getNeurons().size()];

            for (int n = 0; n < currentLayer.getNeurons().size(); n++) {
                Neuron neuron = currentLayer.getNeurons().get(n);

                double sum = 0.0;
                double[] weights = neuron.getWeights();

                for (int w = 0; w < weights.length; w++)
                    sum += prevValues[w] * weights[w];

                sum += neuron.getBias();

                neuron.setZ(sum);
                zValues[n] = sum;
            }

            double[] activated;

            if (i == layers.size() - 1)
                activated = Softmax.apply(zValues);
            else
                activated = ReLU.applyArray(zValues);

            currentLayer.setValues(activated);
            lastActivations[i] = activated;
        }

        return layers.getLast().getValues();
    }

    private static double getActivated(Layer currentLayer, int n, double[] prevValues) {
        Neuron neuron = currentLayer.getNeurons().get(n);
        double sum = 0.0;
        double[] weights = neuron.getWeights();

        for (int w = 0; w < weights.length; w++) {
            sum += prevValues[w] * weights[w];
        }
        sum += neuron.getBias();

        neuron.setZ(sum);
        double activated = ReLU.calculate(sum);
        neuron.setValue(activated);

        return activated;
    }

    public void backward(double[] input, double[] target) {
        train(input, target);
    }

    public void train(double[] input, double[] target) {
        forward(input);

        Layer outputLayer = layers.getLast();
        for (int i = 0; i < outputLayer.getNeurons().size(); i++) {
            Neuron neuron = outputLayer.getNeurons().get(i);

            double predicted = neuron.getValue();
            double error = predicted - target[i];

            neuron.setGradient(error);
        }

        for (int layerIndex = layers.size() - 2; layerIndex > 0; layerIndex--) {
            Layer currentLayer = layers.get(layerIndex);
            Layer nextLayer = layers.get(layerIndex + 1);

            for (int i = 0; i < currentLayer.getNeurons().size(); i++) {
                Neuron neuron = currentLayer.getNeurons().get(i);

                double errorSum = 0.0;

                for (Neuron nextNeuron : nextLayer.getNeurons()) {
                    errorSum += nextNeuron.getWeights()[i] * nextNeuron.getGradient();
                }

                neuron.setGradient(
                        errorSum * ReLU.calculateDerivative(neuron.getZ()));
            }
        }

        for (int layerIndex = 1; layerIndex < layers.size(); layerIndex++) {
            Layer prevLayer = layers.get(layerIndex - 1);
            Layer currentLayer = layers.get(layerIndex);

            double[] prevValues = prevLayer.getValues();

            for (Neuron neuron : currentLayer.getNeurons()) {
                double[] weights = neuron.getWeights();

                for (int w = 0; w < weights.length; w++) {
                    weights[w] -= learningRate * neuron.getGradient() * prevValues[w];
                }

                neuron.setBias(neuron.getBias() - learningRate * neuron.getGradient());
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
