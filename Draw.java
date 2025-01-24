package com.example.paint;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Draw extends Application {

    private final int pixelCount = 128; // Number of pixels per row/column
    private final int canvasSize = 800; // Canvas dimensions (800x800)
    private final int pixelSize = canvasSize / pixelCount; // Size of each pixel

    @Override
    public void start(Stage primaryStage) {
        // Set up the canvas
        Canvas canvas = new Canvas(canvasSize, canvasSize);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        clearCanvas(gc);

        // Clear button
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearCanvas(gc));

        // Drawing actions
        canvas.setOnMouseDragged(e -> {
            int x = (int) (e.getX() / pixelSize) * pixelSize;
            int y = (int) (e.getY() / pixelSize) * pixelSize;
            gc.setFill(Color.WHITE);
            gc.fillRect(x, y, pixelSize * 3, pixelSize * 3); // Brush size of 3 pixels
        });

        // Layout
        BorderPane root = new BorderPane();
        root.setTop(clearButton);
        root.setCenter(canvas);

        // Scene and stage
        Scene scene = new Scene(root, canvasSize, canvasSize);
        primaryStage.setTitle("Canvas");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void clearCanvas(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
