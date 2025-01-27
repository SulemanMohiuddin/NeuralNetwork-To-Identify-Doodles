package com.example.neural_network_java.NeuralNetwork;

import com.example.neural_network_java.NeuralNetwork.Activation.IActivation;
import com.example.neural_network_java.NeuralNetwork.Cost.ICost;
import com.example.neural_network_java.Training.LayerLearnData;

import java.util.Random;

public class Layer {
    private final int numNodesIn;
    private final int numNodesOut;
    private final double[] weights;
    private final double[] biases;

    private final double[] costGradientW;
    private final double[] costGradientB;

    private final double[] weightVelocities;
    private final double[] biasVelocities;

    private IActivation activation;

    public Layer(int numNodesIn, int numNodesOut, Random rng) {
        this.numNodesIn = numNodesIn;
        this.numNodesOut = numNodesOut;

        this.weights = new double[numNodesIn * numNodesOut];
        this.biases = new double[numNodesOut];

        this.costGradientW = new double[weights.length];
        this.costGradientB = new double[biases.length];

        this.weightVelocities = new double[weights.length];
        this.biasVelocities = new double[biases.length];

        initializeRandomWeights(rng);
    }

    public double[] calculateOutputs(double[] inputs) {
        double[] weightedInputs = new double[numNodesOut];
        for (int nodeOut = 0; nodeOut < numNodesOut; nodeOut++) {
            double sum = biases[nodeOut];
            for (int nodeIn = 0; nodeIn < numNodesIn; nodeIn++) {
                sum += inputs[nodeIn] * weights[nodeOut * numNodesIn + nodeIn];
            }
            weightedInputs[nodeOut] = sum;
        }

        double[] activations = new double[numNodesOut];
        for (int i = 0; i < numNodesOut; i++) {
            activations[i] = activation.activate(weightedInputs, i);
        }
        return activations;
    }

    public double[] calculateOutputs(double[] inputs, LayerLearnData learnData) {
        learnData.inputs = inputs;
        learnData.weightedInputs = calculateOutputs(inputs);
        for (int i = 0; i < numNodesOut; i++) {
            learnData.activations[i] = activation.activate(learnData.weightedInputs, i);
        }
        return learnData.activations;
    }

    public void calculateOutputLayerNodeValues(LayerLearnData learnData, double[] expectedOutputs, ICost cost) {
        for (int i = 0; i < numNodesOut; i++) {
            double costDerivative = cost.costDerivative(learnData.activations[i], expectedOutputs[i]);
            double activationDerivative = activation.derivative(learnData.weightedInputs, i);
            learnData.nodeValues[i] = costDerivative * activationDerivative;
        }
    }

    public void calculateHiddenLayerNodeValues(LayerLearnData learnData, Layer nextLayer, double[] nextNodeValues) {
        for (int i = 0; i < numNodesOut; i++) {
            double sum = 0;
            for (int j = 0; j < nextNodeValues.length; j++) {
                sum += nextLayer.getWeight(i, j) * nextNodeValues[j];
            }
            learnData.nodeValues[i] = sum * activation.derivative(learnData.weightedInputs, i);
        }
    }

    public void updateGradients(LayerLearnData learnData) {
        for (int i = 0; i < numNodesOut; i++) {
            costGradientB[i] += learnData.nodeValues[i];
            for (int j = 0; j < numNodesIn; j++) {
                costGradientW[i * numNodesIn + j] += learnData.inputs[j] * learnData.nodeValues[i];
            }
        }
    }

    public void applyGradients(double learnRate, double regularization, double momentum) {
        double weightDecay = 1 - regularization * learnRate;

        for (int i = 0; i < weights.length; i++) {
            weightVelocities[i] = weightVelocities[i] * momentum - costGradientW[i] * learnRate;
            weights[i] = weights[i] * weightDecay + weightVelocities[i];
            costGradientW[i] = 0;
        }

        for (int i = 0; i < biases.length; i++) {
            biasVelocities[i] = biasVelocities[i] * momentum - costGradientB[i] * learnRate;
            biases[i] += biasVelocities[i];
            costGradientB[i] = 0;
        }
    }

    public double getWeight(int inputIndex, int outputIndex) {
        return weights[outputIndex * numNodesIn + inputIndex];
    }

    private void initializeRandomWeights(Random rng) {
        for (int i = 0; i < weights.length; i++) {
            weights[i] = rng.nextGaussian() / Math.sqrt(numNodesIn);
        }
    }

    public void setActivationFunction(IActivation activation) {
        this.activation = activation;
    }

    public int getNumNodesOut() {
        return numNodesOut;
    }


    public void setWeights(double[] weights) {
        System.arraycopy(weights, 0, this.weights, 0, weights.length);
    }

    public void setBiases(double[] biases) {
        System.arraycopy(biases, 0, this.biases, 0, biases.length);
    }

    public double[] getWeights() {
        return weights;
    }

    public double[] getBiases() {
        return biases;
    }

}
