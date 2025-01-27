package com.example.neural_network_java.Training;

import com.example.neural_network_java.DataHandling.DataPoint;
import javafx.util.Pair;

import java.util.*;

public class DataSetHelper {
    /**
     * Splits the dataset into training and validation sets.
     *
     * @param allData       The complete dataset.
     * @param trainingSplit The fraction of data to use for training.
     * @param shuffle       Whether to shuffle the dataset before splitting.
     * @return A pair of training and validation datasets.
     */
    public static Pair<List<DataPoint>, List<DataPoint>> splitData(List<DataPoint> allData, double trainingSplit, boolean shuffle) {
        if (shuffle) {
            Collections.shuffle(allData, new Random());
        }

        int trainSize = (int) (allData.size() * trainingSplit);
        List<DataPoint> trainingData = allData.subList(0, trainSize);
        List<DataPoint> validationData = allData.subList(trainSize, allData.size());

        return new Pair<>(trainingData, validationData);
    }

    /**
     * Creates mini-batches from the dataset.
     *
     * @param allData The dataset.
     * @param batchSize The size of each batch.
     * @param shuffle Whether to shuffle the dataset before batching.
     * @return A list of mini-batches.
     */
    public static List<List<DataPoint>> createMiniBatches(List<DataPoint> allData, int batchSize, boolean shuffle) {
        if (shuffle) {
            Collections.shuffle(allData, new Random());
        }

        List<List<DataPoint>> batches = new ArrayList<>();
        for (int i = 0; i < allData.size(); i += batchSize) {
            int end = Math.min(i + batchSize, allData.size());
            batches.add(new ArrayList<>(allData.subList(i, end)));
        }

        return batches;
    }

    /**
     * Shuffles a list of mini-batches.
     *
     * @param batches The list of mini-batches to shuffle.
     */
    public static void shuffleBatches(List<List<DataPoint>> batches) {
        Collections.shuffle(batches, new Random());
    }
}
