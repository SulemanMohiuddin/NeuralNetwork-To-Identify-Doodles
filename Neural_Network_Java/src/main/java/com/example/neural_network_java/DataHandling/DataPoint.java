package com.example.neural_network_java.DataHandling;

public class DataPoint {
    private final double[] inputs;        // Flattened pixel values
    private final double[] expectedOutputs; // One-hot encoded label
    private final int label;             // Integer label

    public DataPoint(double[] inputs, int label, int numLabels) {
        this.inputs = inputs;
        this.label = label;
        this.expectedOutputs = createOneHot(label, numLabels);
    }

    private double[] createOneHot(int index, int numLabels) {
        double[] oneHot = new double[numLabels];
        oneHot[index] = 1.0; // Set 1 for the correct class
        return oneHot;
    }

    public double[] getInputs() {
        return inputs;
    }

    public double[] getExpectedOutputs() {
        return expectedOutputs;
    }

    public int getLabel() {
        return label;
    }
}
