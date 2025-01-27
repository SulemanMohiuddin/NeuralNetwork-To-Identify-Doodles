package com.example.neural_network_java.Training;

public class LayerLearnData {
    public double[] inputs;
    public double[] weightedInputs;
    public double[] activations;
    public double[] nodeValues;

    public LayerLearnData(int numNodesOut) {
        this.weightedInputs = new double[numNodesOut];
        this.activations = new double[numNodesOut];
        this.nodeValues = new double[numNodesOut];
    }
}


