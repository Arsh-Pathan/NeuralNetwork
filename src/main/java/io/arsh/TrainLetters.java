package io.arsh;

import io.arsh.visualizer.Visualizer;

public class TrainLetters {

    public static void main(String[] args) throws Exception {

        // EMNIST Letters has 784 inputs and 26 outputs (A-Z)
        Network net = new Network(784, 256, 128, 26);
        Visualizer viz = net.show();
        viz.setLetterMode(true);
        net.setLearningRate(0.001);

        MnistCSV.load("mnist_letters.csv", 124800, true);

        int epochs = 20;
        for (int epoch = 0; epoch < epochs; epoch++) {

            for (int i = 0; i < MnistCSV.images.size(); i++) {
                double[] input = MnistCSV.images.get(i);
                int label = MnistCSV.labels.get(i); // 1-26

                // Train with 1-indexed to 0-indexed conversion
                net.train(input, MnistCSV.oneHotLetter(label));

                // Update visualizer
                viz.updateTraining(input, label, epoch, epochs, i, MnistCSV.images.size());

                // Use the speed slider delay
                int delay = viz.getTrainingDelay();
                if (delay > 0) {
                    Thread.sleep(delay);
                }

                if (i % 1000 == 0)
                    System.out.println("Epoch " + epoch + " : " + i + "/" + MnistCSV.images.size());
            }

            System.out.println("Epoch " + epoch + " complete");
        }

        net.save("letters.nn");
        System.out.println("LETTER MODEL SAVED!");
    }
}
