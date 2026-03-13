package io.arsh;

public class TrainNumbers {

    public static void main(String[] args) throws Exception {

        Network net = new Network(784, 128, 64, 10);
        io.arsh.visualizer.Visualizer viz = net.show();
        net.setLearningRate(0.01);

        MnistCSV.load("mnist_numbers.csv", 10000, false);

        int epochs = 10;
        for (int epoch = 0; epoch < epochs; epoch++) {

            for (int i = 0; i < MnistCSV.images.size(); i++) {
                double[] input = MnistCSV.images.get(i);
                int label = MnistCSV.labels.get(i);

                net.train(input, MnistCSV.oneHot(label, 10));

                // Update visualizer
                viz.updateTraining(input, label, epoch, epochs, i, MnistCSV.images.size());

                // Slow down for visualization (controlled by slider)
                int delay = viz.getTrainingDelay();
                if (delay > 0) {
                    Thread.sleep(delay);
                }

                if (i % 1000 == 0)
                    System.out.println("Epoch " + epoch + " : " + i + "/" + MnistCSV.images.size());
            }

            System.out.println("Epoch " + epoch + " complete");
        }

        net.save("mnist.nn");
        System.out.println("MODEL SAVED!");
    }
}