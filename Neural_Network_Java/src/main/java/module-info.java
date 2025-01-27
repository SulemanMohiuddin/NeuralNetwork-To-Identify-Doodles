module com.example.neural_network_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires com.opencsv;
    requires com.google.gson;
    requires org.json;


    opens com.example.neural_network_java to javafx.fxml;
    exports com.example.neural_network_java;
    exports com.example.neural_network_java.Drawing;
    opens com.example.neural_network_java.Drawing to javafx.fxml;
}