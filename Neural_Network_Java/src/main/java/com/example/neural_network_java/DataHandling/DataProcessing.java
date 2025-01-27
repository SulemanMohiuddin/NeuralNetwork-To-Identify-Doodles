package com.example.neural_network_java.DataHandling;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

public class DataProcessing {
    private final Map<String, Integer> labelToIndex = new HashMap<>(); // Maps labels to indices
    private int currentIndex = 0;

    /**
     * Dynamically assigns or retrieves an index for the given label.
     */
    private int getLabelIndex(String label) {
        return labelToIndex.computeIfAbsent(label, key -> currentIndex++);
    }

    /**
     * Loads and processes the dataset, returning a list of DataPoints.
     */
    public List<DataPoint> loadDataset(String csvPath, int imageSize, int numLabels) throws IOException {
        List<DataPoint> dataPoints = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] columns = line.split("\t");
                String imagePath = columns[5]; // Image path
                String word = columns[4];     // Label (e.g., "traffic light")

                int label = getLabelIndex(word); // Assign or retrieve label index
                double[] inputs = processImage(new File(imagePath), imageSize);

                dataPoints.add(new DataPoint(inputs, label, numLabels));
            }
        }

        System.out.println("Unique labels: " + labelToIndex);
        return dataPoints;
    }

    /**
     * Processes an image: loads it, converts to grayscale, resizes, and normalizes.
     */
    private double[] processImage(File file, int imageSize) throws IOException {
        BufferedImage img = ImageIO.read(file);
        BufferedImage resizedImg = resizeImage(img, imageSize, imageSize);

        double[] pixelValues = new double[imageSize * imageSize];
        for (int y = 0; y < imageSize; y++) {
            for (int x = 0; x < imageSize; x++) {
                int rgb = resizedImg.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // Extract grayscale value
                pixelValues[y * imageSize + x] = gray / 255.0; // Normalize to [0, 1]
            }
        }
        return pixelValues;
    }

    /**
     * Resizes an image to the target dimensions using Java's Graphics2D.
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    /**
     * Returns the label-to-index mapping for debugging or reference.
     */
    public Map<String, Integer> getLabelMapping() {
        return labelToIndex;
    }
}
