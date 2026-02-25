package io.arsh.tests;

import io.arsh.Network;

import java.util.Arrays;

public class XORTest {
    public static void main(String[] args) {
        Network network = new Network(2, 4, 1);
        network.setLearningRate(0.1);

        double[][] inputs = {
                {0.0, 0.0},
                {0.0, 1.0},
                {1.0, 0.0},
                {1.0, 1.0}
        };
        double[][] targets = {
                {0.0},
                {1.0},
                {1.0},
                {0.0}
        };

        System.out.println("Starting Training...");

        for (int epoch = 0; epoch < 50000; epoch++) {
            int index = (int) (Math.random() * inputs.length);
            network.train(inputs[index], targets[index]);
        }

        System.out.println("Training Complete. Testing results:");
        for (int i = 0; i < inputs.length; i++) {
            double[] output = network.forward(inputs[i]);
            double result = output[0];
            double expected = targets[i][0];

            System.out.printf("Input: %s | Target: %.1f | Prediction: %.4f | rounded: %d%n",
                    Arrays.toString(inputs[i]),
                    expected,
                    result,
                    Math.round(result)
            );
        }
    }
}