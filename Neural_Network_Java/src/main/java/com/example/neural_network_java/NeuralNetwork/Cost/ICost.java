package com.example.neural_network_java.NeuralNetwork.Cost;

public interface ICost {
    double costFunction(double[] predictedOutputs, double[] expectedOutputs);
    double costDerivative(double predictedOutput, double expectedOutput);
}
