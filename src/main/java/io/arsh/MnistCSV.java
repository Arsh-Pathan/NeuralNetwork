package io.arsh;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MnistCSV {

    public static List<double[]> images = new ArrayList<>();
    public static List<Integer> labels = new ArrayList<>();

    public static void load(String path, int limit, boolean transpose) throws Exception {

        images.clear();
        labels.clear();

        BufferedReader br = new BufferedReader(new FileReader(path));

        String line;
        int count = 0;

        while ((line = br.readLine()) != null && count < limit) {

            String[] parts = line.split(",");

            int label = Integer.parseInt(parts[0]);
            double[] img = new double[784];

            // normalize pixels 0-255 -> 0-1
            for (int i = 1; i < parts.length; i++)
                img[i - 1] = Integer.parseInt(parts[i]) / 255.0;

            if (transpose) {
                double[] transposed = new double[784];
                for (int y = 0; y < 28; y++) {
                    for (int x = 0; x < 28; x++) {
                        transposed[x * 28 + y] = img[y * 28 + x];
                    }
                }
                img = transposed;
            }

            labels.add(label);
            images.add(img);
            count++;
        }

        br.close();

        System.out.println("Loaded " + count + " images from " + path);
    }

    public static double[] oneHot(int digit, int size) {
        double[] t = new double[size];
        if (digit >= 0 && digit < size) {
            t[digit] = 1.0;
        }
        return t;
    }

    public static double[] oneHotLetter(int label) {
        return oneHot(label - 1, 26);
    }
}
