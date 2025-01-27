package com.example.neural_network_java.NeuralNetwork;

public class HyperParameters {
    private int[] layerSizes;
    private double initialLearningRate;
    private double regularization;
    private double momentum;

    public int[] getLayerSizes() {
        return layerSizes;
    }

    public void setLayerSizes(int[] layerSizes) {
        this.layerSizes = layerSizes;
    }

    public double getInitialLearningRate() {
        return initialLearningRate;
    }

    public void setInitialLearningRate(double initialLearningRate) {
        this.initialLearningRate = initialLearningRate;
    }

    public double getRegularization() {
        return regularization;
    }

    public void setRegularization(double regularization) {
        this.regularization = regularization;
    }

    public double getMomentum() {
        return momentum;
    }

    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }
}
