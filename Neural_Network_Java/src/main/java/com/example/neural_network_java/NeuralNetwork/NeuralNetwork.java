package com.example.neural_network_java.NeuralNetwork;

import com.example.neural_network_java.DataHandling.DataPoint;
import com.example.neural_network_java.NeuralNetwork.Activation.IActivation;
import com.example.neural_network_java.NeuralNetwork.Cost.Cost;
import com.example.neural_network_java.NeuralNetwork.Cost.ICost;
import com.example.neural_network_java.Training.LayerLearnData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Random;

public class NeuralNetwork {
    private final Layer[] layers;
    private final int[] layerSizes;
    private ICost cost;
    private final Random rng;

    public NeuralNetwork(int... layerSizes) {
        this.layerSizes = layerSizes;
        this.rng = new Random();

        this.layers = new Layer[layerSizes.length - 1];
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new Layer(layerSizes[i], layerSizes[i + 1], rng);
        }

        this.cost = new Cost.MeanSquaredError();
    }

    public void setActivationFunction(IActivation hiddenActivation, IActivation outputActivation) {
        for (int i = 0; i < layers.length - 1; i++) {
            layers[i].setActivationFunction(hiddenActivation);
        }
        layers[layers.length - 1].setActivationFunction(outputActivation);
    }

    public void setCostFunction(ICost costFunction) {
        this.cost = costFunction;
    }

    public int classify(double[] inputs) {
        double[] outputs = calculateOutputs(inputs);
        return maxValueIndex(outputs);
    }

    public double[] calculateOutputs(double[] inputs) {
        for (Layer layer : layers) {
            inputs = layer.calculateOutputs(inputs);
        }
        return inputs;
    }

    public void learn(DataPoint[] batch, double learnRate, double regularization, double momentum) {
        LayerLearnData[] learnData = new LayerLearnData[layers.length];
        for (int i = 0; i < layers.length; i++) {
            learnData[i] = new LayerLearnData(layers[i].getNumNodesOut());
        }

        for (DataPoint data : batch) {
            double[] inputs = data.getInputs();
            for (int i = 0; i < layers.length; i++) {
                inputs = layers[i].calculateOutputs(inputs, learnData[i]);
            }

            Layer outputLayer = layers[layers.length - 1];
            outputLayer.calculateOutputLayerNodeValues(learnData[layers.length - 1], data.getExpectedOutputs(), cost);

            for (int i = layers.length - 2; i >= 0; i--) {
                layers[i].calculateHiddenLayerNodeValues(learnData[i], layers[i + 1], learnData[i + 1].nodeValues);
            }

            for (int i = 0; i < layers.length; i++) {
                layers[i].updateGradients(learnData[i]);
            }
        }

        for (Layer layer : layers) {
            layer.applyGradients(learnRate / batch.length, regularization, momentum);
        }
    }

    private int maxValueIndex(double[] values) {
        double maxValue = Double.NEGATIVE_INFINITY;
        int index = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] > maxValue) {
                maxValue = values[i];
                index = i;
            }
        }
        return index;
    }

    public Layer getLayer(int index) {
        return layers[index];
    }


    public static NeuralNetwork load(String filePath) {
        try (Reader reader = new FileReader(filePath)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            // Read layer sizes
            JsonArray layerSizesJson = json.getAsJsonArray("layerSizes");
            int[] layerSizes = new int[layerSizesJson.size()];
            for (int i = 0; i < layerSizesJson.size(); i++) {
                layerSizes[i] = layerSizesJson.get(i).getAsInt();
            }

            NeuralNetwork neuralNetwork = new NeuralNetwork(layerSizes);

            // Load weights and biases
            JsonArray layersJson = json.getAsJsonArray("layers");
            for (int i = 0; i < layersJson.size(); i++) {
                JsonObject layerJson = layersJson.get(i).getAsJsonObject();

                // Load weights
                JsonArray weightsJson = layerJson.getAsJsonArray("weights");
                double[] weights = new double[weightsJson.size()];
                for (int j = 0; j < weightsJson.size(); j++) {
                    weights[j] = weightsJson.get(j).getAsDouble();
                }

                // Load biases
                JsonArray biasesJson = layerJson.getAsJsonArray("biases");
                double[] biases = new double[biasesJson.size()];
                for (int j = 0; j < biasesJson.size(); j++) {
                    biases[j] = biasesJson.get(j).getAsDouble();
                }

                neuralNetwork.getLayer(i).setWeights(weights);
                neuralNetwork.getLayer(i).setBiases(biases);
            }

            return neuralNetwork;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load Neural Network model from " + filePath);
        }
    }

    public void save(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            JsonObject json = new JsonObject();

            // Save layer sizes
            JsonArray layerSizesJson = new JsonArray();
            for (int size : layerSizes) {
                layerSizesJson.add(size);
            }
            json.add("layerSizes", layerSizesJson);

            // Save weights and biases for each layer
            JsonArray layersJson = new JsonArray();
            for (Layer layer : layers) {
                JsonObject layerJson = new JsonObject();

                // Weights
                JsonArray weightsJson = new JsonArray();
                for (double weight : layer.getWeights()) {
                    weightsJson.add(weight);
                }
                layerJson.add("weights", weightsJson);

                // Biases
                JsonArray biasesJson = new JsonArray();
                for (double bias : layer.getBiases()) {
                    biasesJson.add(bias);
                }
                layerJson.add("biases", biasesJson);

                layersJson.add(layerJson);
            }
            json.add("layers", layersJson);

            // Write JSON to file
            writer.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public double evaluate(List<DataPoint> validationData) {
        int correct = 0;

        for (DataPoint data : validationData) {
            int predicted = classify(data.getInputs());
            if (predicted == data.getLabel()) {
                correct++;
            }
        }

        return (double) correct / validationData.size();
    }



}
