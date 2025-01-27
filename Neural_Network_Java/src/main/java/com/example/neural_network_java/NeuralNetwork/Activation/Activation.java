package com.example.neural_network_java.NeuralNetwork.Activation;

public class Activation {

    public enum ActivationType {
        SIGMOID, TANH, RELU, SILU, SOFTMAX
    }

    public static IActivation getActivationFromType(ActivationType type) {
        switch (type) {
            case SIGMOID: return new Sigmoid();
            case TANH: return new TanH();
            case RELU: return new ReLU();
            case SILU: return new SiLU();
            case SOFTMAX: return new Softmax();
            default: throw new IllegalArgumentException("Unhandled activation type: " + type);
        }
    }

    public static class Sigmoid implements IActivation {
        @Override
        public double activate(double[] inputs, int index) {
            return 1.0 / (1.0 + Math.exp(-inputs[index]));
        }

        @Override
        public double derivative(double[] inputs, int index) {
            double a = activate(inputs, index);
            return a * (1 - a);
        }
    }

    public static class TanH implements IActivation {
        @Override
        public double activate(double[] inputs, int index) {
            double e2 = Math.exp(2 * inputs[index]);
            return (e2 - 1) / (e2 + 1);
        }

        @Override
        public double derivative(double[] inputs, int index) {
            double t = activate(inputs, index);
            return 1 - t * t;
        }
    }

    public static class ReLU implements IActivation {
        @Override
        public double activate(double[] inputs, int index) {
            return Math.max(0, inputs[index]);
        }

        @Override
        public double derivative(double[] inputs, int index) {
            return inputs[index] > 0 ? 1 : 0;
        }
    }

    public static class SiLU implements IActivation {
        @Override
        public double activate(double[] inputs, int index) {
            return inputs[index] / (1.0 + Math.exp(-inputs[index]));
        }

        @Override
        public double derivative(double[] inputs, int index) {
            double sig = 1.0 / (1.0 + Math.exp(-inputs[index]));
            return inputs[index] * sig * (1 - sig) + sig;
        }
    }

    public static class Softmax implements IActivation {
        @Override
        public double activate(double[] inputs, int index) {
            double expSum = 0;
            for (double input : inputs) {
                expSum += Math.exp(input);
            }
            return Math.exp(inputs[index]) / expSum;
        }

        @Override
        public double derivative(double[] inputs, int index) {
            double expSum = 0;
            for (double input : inputs) {
                expSum += Math.exp(input);
            }
            double ex = Math.exp(inputs[index]);
            return (ex * expSum - ex * ex) / (expSum * expSum);
        }
    }
}