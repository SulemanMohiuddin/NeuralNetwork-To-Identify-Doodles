package com.example.neural_network_java.Training;

import com.example.neural_network_java.NeuralNetwork.Layer;

public class NetworkLearnData {
    public LayerLearnData[] layerData;

    public NetworkLearnData(Layer[] layers) {
        this.layerData = new LayerLearnData[layers.length];
        for (int i = 0; i < layers.length; i++) {
            this.layerData[i] = new LayerLearnData(layers[i].getNumNodesOut());
        }
    }
}