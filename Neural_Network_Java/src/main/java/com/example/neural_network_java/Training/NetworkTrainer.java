package com.example.neural_network_java.Training;

import com.example.neural_network_java.DataHandling.DataPoint;
import com.example.neural_network_java.NeuralNetwork.HyperParameters;
import com.example.neural_network_java.NeuralNetwork.NeuralNetwork;

import java.util.List;

public class NetworkTrainer {
    private final NeuralNetwork neuralNetwork; // Trained neural network
    private final HyperParameters hyperParameters;

    public NetworkTrainer(HyperParameters hyperParameters) {
        this.hyperParameters = hyperParameters;
        this.neuralNetwork = new NeuralNetwork(hyperParameters.getLayerSizes());
    }

    /**
     * Train the neural network using the provided data.
     *
     * @param data      The dataset to train on.
     * @param epochs    Number of epochs to train.
     * @param batchSize Size of mini-batches.
     */
    public void train(List<DataPoint> data, int epochs, int batchSize) {
        for (int epoch = 0; epoch < epochs; epoch++) {
            List<List<DataPoint>> batches = DataSetHelper.createMiniBatches(data, batchSize, true);
            for (List<DataPoint> batch : batches) {
                neuralNetwork.learn(
                        batch.toArray(new DataPoint[0]),
                        hyperParameters.getInitialLearningRate(),
                        hyperParameters.getRegularization(),
                        hyperParameters.getMomentum()
                );
            }
            System.out.println("Epoch " + (epoch + 1) + " completed.");
        }
    }

    /**
     * Save the trained neural network to a file.
     *
     * @param filePath The file path where the model should be saved.
     */
    public void saveModel(String filePath) {
        neuralNetwork.save(filePath);
        System.out.println("Model saved to: " + filePath);
    }

    /**
     * Evaluate the neural network on a validation dataset.
     *
     * @param validationData The validation dataset.
     * @return The accuracy of the model on the validation dataset.
     */
    public double evaluate(List<DataPoint> validationData) {
        int correct = 0;

        for (DataPoint point : validationData) {
            int predicted = neuralNetwork.classify(point.getInputs());
            if (predicted == point.getLabel()) {
                correct++;
            }
        }

        double accuracy = (double) correct / validationData.size();
        System.out.printf("Validation Accuracy: %.2f%%%n", accuracy * 100);
        return accuracy;
    }

    /**
     * Get the trained neural network instance.
     *
     * @return The trained neural network.
     */
    public NeuralNetwork getNeuralNetwork() {
        return neuralNetwork;
    }
}
