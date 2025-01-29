![79ba9e57-f1bf-470c-8b68-25faf29ff4b2](https://github.com/user-attachments/assets/621b7495-25bb-4907-86c7-285c4ec6da06)


# 🌟 Doodle Recognition Neural Network 🎨

This repository contains a **Doodle Recognition Neural Network** implemented entirely in Java, without using any external machine learning libraries. The focus is on building the core neural network concepts from scratch using mathematical computations. 🧠✨

## 📚 Table of Contents
- [🎯 Overview](#-overview)
- [🚀 Features](#-features)
- [🛠 Prerequisites](#-prerequisites)
- [📥 Installation](#-installation)
- [🖥️ Usage](#%EF%B8%8F-usage)
- [🔍 How It Works](#-how-it-works)
- [📂 Project Structure](#-project-structure)
- [🤝 Contributing](#-contributing)
- [📜 License](#-license)

## 🎯 Overview
The goal of this project is to recognize doodles (simple hand-drawn sketches) by training a neural network built entirely from mathematical principles. The project demonstrates:
- Fundamental concepts of neural networks 🧩
- Matrix operations for forward and backward propagation ➗✖️
- Optimization techniques like gradient descent 📉

## 🚀 Features
- **Custom Neural Network:** Built from scratch using only Java and mathematics. 🛠️
- **Adjustable Layers:** Configurable number of layers and neurons. 🎛️
- **Training and Testing:** Supports training on a dataset and testing with unseen doodles. 🖼️
- **No Dependencies:** No external machine learning libraries; only Java's standard libraries are used. ✅

## 🛠 Prerequisites
- **Java Development Kit (JDK):** Version 11 or later ☕
- **Dataset:** https://www.kaggle.com/datasets/ashishjangra27/doodle-dataset 📊

## 📥 Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/SulemanMohiuddin/doodle-recognition-nn.git
   cd doodle-recognition-nn
   ```
2. Compile the Java files:
   ```bash
   javac -d out src/**/*.java
   ```
3. Run the program:
   ```bash
   java -cp out Main
   ```

## 🖥️ Usage
1. **Prepare Dataset:** Ensure your doodle dataset is formatted correctly (e.g., pixel data as inputs and labels as outputs). 📝
2. **Training:** Train the neural network by running the program and specifying the training dataset. 🏋️‍♂️
3. **Testing:** Test the trained network with new doodles to evaluate performance. 🎯

### Example
```bash
java -cp out Main train dataset/train.csv
java -cp out Main test dataset/test.csv
```

## 🔍 How It Works
### Neural Network Architecture
- **Input Layer:** Takes pixel values as input. ✏️
- **Hidden Layers:** Configurable layers with adjustable neurons and activation functions. 🌀
- **Output Layer:** Outputs probabilities for each class (doodle type). 📊

### Key Concepts
- **Matrix Multiplication:** For forward propagation. ➗✖️
- **Activation Functions:** ReLU, Sigmoid, or custom functions. ⚡
- **Backpropagation:** Calculates gradients using the chain rule. 🔄
- **Gradient Descent:** Updates weights to minimize the loss function. 📉

### Example Workflow
1. **Initialization:** Randomly initialize weights and biases. 🎲
2. **Forward Propagation:** Calculate outputs layer by layer. 🧩
3. **Error Calculation:** Compute the loss (e.g., Mean Squared Error or Cross-Entropy Loss). ❌
4. **Backward Propagation:** Adjust weights using gradients. 🔧
5. **Repeat:** Iterate over multiple epochs until convergence. 🔁

## 📂 Project Structure
```
Neural_Network_Java
├── .idea
├── src
│   ├── main
│      ├── java
│         ├── com.example.neural_network_java
│            ├── DataHandling
│               ├── DataPoint.java
│               ├── DataProcessing.java
│               ├── Main.java
│            ├── Drawing
│               ├── Draw.java
│            ├── NeuralNetwork
│               ├── Activation
│               │   ├── Activation.java
│               │   ├── IActivation.java
│               ├── Cost
│               │   ├── Cost.java
│               │   ├── ICost.java
│               ├── HyperParameters.java
│               ├── Layer.java
│               ├── NeuralNetwork.java
│            |── Training
│               ├── DataSetHelper.java
│               ├── LayerLearnData.java
│               ├── NetworkLearnData.java
│               ├── NetworkTrainer.java
│               ├── HelloController.java
│               ├── TrainAndRun.java
│            ├── module-info.java
├── resources
├── target
├── Neural_Network_Java.iml
├── pom.xml
├── External Libraries
└── Scratches and Consoles

```

## 🤝 Contributing
Contributions are welcome! If you have suggestions or bug fixes, feel free to submit a pull request. 🛠️

### Steps to Contribute
1. Fork the repository. 🍴
2. Create a new branch for your feature or fix. 🌿
3. Commit your changes and push to your fork. 📤
4. Open a pull request with a detailed description. 📝

## 📜 Help
https://www.youtube.com/watch?v=hfMk-kjRv4c&t=2955s
