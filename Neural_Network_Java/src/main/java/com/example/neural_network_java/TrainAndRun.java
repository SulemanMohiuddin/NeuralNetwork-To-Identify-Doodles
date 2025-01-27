package com.example.neural_network_java;

import com.example.neural_network_java.DataHandling.DataPoint;
import com.example.neural_network_java.DataHandling.DataProcessing;
import com.example.neural_network_java.NeuralNetwork.HyperParameters;
import com.example.neural_network_java.NeuralNetwork.NeuralNetwork;
import com.example.neural_network_java.Training.DataSetHelper;
import com.example.neural_network_java.Training.NetworkTrainer;

import java.util.List;

public class TrainAndRun {
    public static void main(String[] args) {
        try {
            // Step 1: Initialize HyperParameters
            HyperParameters hyperParameters = new HyperParameters();
            hyperParameters.setLayerSizes(new int[]{784, 128, 64, 340}); // Example: 784 input, 128 hidden, 64 hidden, 340 output
            hyperParameters.setInitialLearningRate(0.01);
            hyperParameters.setMomentum(0.9);
            hyperParameters.setRegularization(0.001);

            // Step 2: Load the Dataset
            DataProcessing dataProcessing = new DataProcessing();
            String csvPath = "path/to/master_doodle_dataframe.csv"; // Update with your actual path
            List<DataPoint> dataset = dataProcessing.loadDataset(csvPath, 28, 340); // Image size = 28x28, 340 classes

            // Step 3: Split the Data into Training and Validation Sets
            double trainingSplit = 0.8; // 80% training, 20% validation
            var splitData = DataSetHelper.splitData(dataset, trainingSplit, true);
            List<DataPoint> trainingData = splitData.getKey(); // Training set
            List<DataPoint> validationData = splitData.getValue(); // Validation set

            // Step 4: Train the Neural Network
            NetworkTrainer trainer = new NetworkTrainer(hyperParameters);
            int epochs = 10; // Number of epochs
            int batchSize = 32; // Mini-batch size
            trainer.train(trainingData, epochs, batchSize);

            // Step 5: Save the Trained Model
            String modelPath = "path/to/trained_model.json"; // Update with your desired save location
            trainer.getNeuralNetwork().save(modelPath);
            System.out.println("Model saved to: " + modelPath);

            // Step 6: Evaluate on Validation Set
            double accuracy = trainer.getNeuralNetwork().evaluate(validationData);
            System.out.println("Validation Accuracy: " + accuracy);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
