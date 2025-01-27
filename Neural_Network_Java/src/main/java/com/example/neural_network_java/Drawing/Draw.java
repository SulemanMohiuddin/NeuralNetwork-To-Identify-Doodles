package com.example.neural_network_java.Drawing;

import com.example.neural_network_java.NeuralNetwork.NeuralNetwork;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Draw extends Application {

    private final int logicalResolution = 255; // Actual drawing grid (28x28)
    private final int canvasSize = 765; // Display canvas size in pixels (255x255)
    private final int pixelSize = canvasSize / logicalResolution; // Size of each visible "pixel"

    private double[][] grid; // 28x28 grid storing pixel data
    private Canvas canvas; // Display canvas
    private GraphicsContext gc; // Graphics context for rendering

    @Override
    public void start(Stage primaryStage) {
        // Initialize grid and canvas
        grid = new double[logicalResolution][logicalResolution];
        canvas = new Canvas(canvasSize, canvasSize);
        gc = canvas.getGraphicsContext2D();

        // Clear the grid and canvas
        clearCanvas();

        // Mouse event for drawing
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::drawOnGrid);

        // Button to convert the drawing to JSON
        Button saveButton = new Button("Save as JSON");
        saveButton.setLayoutX(10);
        saveButton.setLayoutY(canvasSize + 10);
        saveButton.setOnAction(event -> convertToJson());

        // Layout and scene
        Pane root = new Pane(canvas, saveButton);
        Scene scene = new Scene(root, canvasSize, canvasSize + 50);

        primaryStage.setTitle("Draw Application");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawOnGrid(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        // Map mouse position to logical grid (28x28)
        int logicalX = (int) (x / (canvasSize / logicalResolution));
        int logicalY = (int) (y / (canvasSize / logicalResolution));

        // Ensure coordinates are within bounds
        if (logicalX >= 0 && logicalX < logicalResolution && logicalY >= 0 && logicalY < logicalResolution) {
            // Update logical grid
            grid[logicalX][logicalY] = 1.0;

            // Redraw the canvas
            renderCanvas();
        }
    }

    private void renderCanvas() {
        // Clear the visual canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the logical grid (28x28)
        gc.setFill(Color.WHITE);
        for (int i = 0; i < logicalResolution; i++) {
            for (int j = 0; j < logicalResolution; j++) {
                if (grid[i][j] > 0) {
                    int visualX = i * pixelSize;
                    int visualY = j * pixelSize;
                    gc.fillRect(visualX, visualY, pixelSize, pixelSize);
                }
            }
        }
    }

    private void clearCanvas() {
        // Clear the logical grid
        for (int i = 0; i < logicalResolution; i++) {
            for (int j = 0; j < logicalResolution; j++) {
                grid[i][j] = 0.0;
            }
        }

        // Clear the visual canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void convertToJson() {
        JsonArray drawingData = new JsonArray();

        // Generate the data in the format specified
        for (int i = 0; i < logicalResolution; i++) {
            JsonArray xCoords = new JsonArray();
            JsonArray yCoords = new JsonArray();

            for (int j = 0; j < logicalResolution; j++) {
                if (grid[i][j] > 0) {
                    // If the cell is drawn, add its coordinates
                    xCoords.add(i);
                    yCoords.add(j);
                }
            }

            // Add both x and y coordinate arrays for the stroke
            if (xCoords.size() > 0 && yCoords.size() > 0) {
                JsonArray stroke = new JsonArray();
                stroke.add(xCoords);
                stroke.add(yCoords);
                drawingData.add(stroke);
            }
        }

        // Print the JSON in the desired format
        System.out.println(drawingData.toString());
    }
    public double[] getGridAsArray() {
        double[] array = new double[784];  // 28x28 = 784
        int index = 0;
        for (int i = 0; i < logicalResolution; i++) {
            for (int j = 0; j < logicalResolution; j++) {
                array[index++] = grid[i][j]; // Use the drawn grid's value
            }
        }
        return array;
    }

    private void convertAndClassify() {
        try {
            // Step 1: Convert grid to a 1D array
            double[] doodleData = getGridAsArray();

            // Step 2: Load the trained model
            String modelPath = "path/to/trained_model.json"; // Ensure this matches your saved model path
            NeuralNetwork neuralNetwork = NeuralNetwork.load(modelPath); // Implement load method in NeuralNetwork

            // Step 3: Classify the drawing
            int predictedClass = neuralNetwork.classify(doodleData);

            // Step 4: Display the predicted class
            System.out.println("Predicted class: " + predictedClass);
            // Optionally, map predictedClass to a human-readable label
            System.out.println("Predicted label: " + getLabelFromIndex(predictedClass));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLabelFromIndex(int index) {
        // Implement this mapping to return human-readable class labels
        switch (index) {
            case 0: return "traffic light";
            case 1: return "cat";
            case 2: return "dog";
            // Add more cases as needed for all 340 classes
            default: return "Unknown";
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}