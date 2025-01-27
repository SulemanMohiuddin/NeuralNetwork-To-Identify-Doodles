package com.example.neural_network_java.NeuralNetwork.Activation;

public interface IActivation {
    double activate(double[] inputs, int index); // Activation function
    double derivative(double[] inputs, int index); // Derivative of the activation function
}