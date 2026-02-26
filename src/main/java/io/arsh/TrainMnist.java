package io.arsh;


public class TrainMnist {

    public static void main(String[] args) throws Exception {

        Network net = new Network(784,128,64,10);
        net.setLearningRate(0.001);

        MnistCSV.load("mnist_train.csv", 10000);

        for(int epoch=0; epoch<50; epoch++) {

            for(int i=0;i<MnistCSV.images.size();i++) {
                net.train(
                        MnistCSV.images.get(i),
                        MnistCSV.oneHot(MnistCSV.labels.get(i))
                );

                if(i%1000==0)
                    System.out.println("Epoch "+epoch+" : "+i+"/"+MnistCSV.images.size());
            }

            System.out.println("Epoch "+epoch+" complete");
        }

        net.save("mnist.nn");
        System.out.println("MODEL SAVED!");
    }
}