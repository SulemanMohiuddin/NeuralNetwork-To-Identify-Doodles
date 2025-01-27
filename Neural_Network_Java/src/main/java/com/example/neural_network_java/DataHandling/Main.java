package com.example.neural_network_java.DataHandling;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            String csvPath = "path/to/master_doodle_dataframe.csv";
            int imageSize = 28; // Resize all images to 28x28
            int numLabels = 340; // Total number of classes

            DataProcessing dataProcessing = new DataProcessing();
            List<DataPoint> dataPoints = dataProcessing.loadDataset(csvPath, imageSize, numLabels);

            System.out.println("Loaded " + dataPoints.size() + " data points.");
            System.out.println("Label mapping: " + dataProcessing.getLabelMapping());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
