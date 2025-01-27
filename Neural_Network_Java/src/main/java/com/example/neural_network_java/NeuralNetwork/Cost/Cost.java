package com.example.neural_network_java.NeuralNetwork.Cost;

public class Cost {
    public static class MeanSquaredError implements ICost {
        @Override
        public double costFunction(double[] predicted, double[] expected) {
            double cost = 0;
            for (int i = 0; i < predicted.length; i++) {
                double error = predicted[i] - expected[i];
                cost += error * error;
            }
            return 0.5 * cost;
        }

        @Override
        public double costDerivative(double predicted, double expected) {
            return predicted - expected;
        }
    }

    public static class CrossEntropy implements ICost {
        @Override
        public double costFunction(double[] predicted, double[] expected) {
            double cost = 0;
            for (int i = 0; i < predicted.length; i++) {
                cost -= expected[i] * Math.log(predicted[i] + 1e-9);
            }
            return cost;
        }

        @Override
        public double costDerivative(double predicted, double expected) {
            return (predicted - expected) / (predicted * (1 - predicted) + 1e-9);
        }
    }
}
