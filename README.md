
# 🧠 Neural Network Visualizer — Java Library

A lightweight, self-contained **neural network library written in Java**, designed for **learning, experimentation, and visualization**.  
It provides a clean API for building and training fully connected neural networks, along with a **real-time Swing-based visualizer** for inspecting layers, neurons, activations, and weights.

The project is distributed as a **reusable JAR library**, making it easy to integrate into other Java applications.

## ✨ Key Features

- Fully connected feedforward neural network
- Forward propagation
- Backpropagation training
- ReLU activation function
- Adjustable learning rate
- Real-time network visualization
- Interactive zoom
- Visual representation of:
  - Neuron activations
  - Layer structure
  - Weight magnitude and sign
- Model saving and loading via serialization
- No external machine learning libraries required

## 📦 Using This Project as a Library

### 1. Download the JAR
- Go to the **Releases** section of this repository
- Download the latest `.jar` file

### 2. Add the JAR to Your Project

#### IntelliJ IDEA
1. Open **Project Structure**
2. Navigate to **Libraries**
3. Click **+ → Java**
4. Select the downloaded JAR
5. Apply and close

#### Command Line
```bash
javac -cp neural-network.jar YourProject.java
java -cp neural-network.jar;. YourProject
````

## 🚀 Quick Start

### Create a Network

```java
import io.arsh.Network;

Network network = new Network(3, 5, 2);
network.setLearningRate(0.1);
```

### Forward Propagation

```java
double[] output = network.forward(
    new double[]{1.0, 0.5, 0.2}
);
```

### Training (Backpropagation)

```java
network.train(
    new double[]{1.0, 0.5, 0.2},
    new double[]{0.0, 1.0}
);
```

---

## 👁️ Visualizing the Network

The library includes a built-in visualizer for inspecting the network in real time:

```java
network.show();
```

### Visualizer Details

* Each **circle** represents a neuron
* **Color intensity** reflects activation value
* **Connection thickness** represents weight magnitude
* **Color differences** indicate positive vs negative weights
* Mouse wheel → zoom
* Click & drag → move around

## 💾 Saving and Loading Models

Networks can be serialized and reused:

```java
network.save("model.nn");

Network loaded = Network.load("model.nn");
```

## 🧩 Architecture Overview

* `Network` — core neural network logic
* `Layer` — manages neurons per layer
* `Neuron` — stores weights, bias, value, and gradient
* `utils` — activation functions (ReLU, Sigmoid)
* `visualizer` — Swing-based real-time renderer

The design prioritizes **clarity**, **modularity**, and **educational value**.

## 🎯 Intended Use Cases

This library is well-suited for:

* Educational projects
* Learning neural network fundamentals
* Visual demonstrations
* Java-based simulations
* Custom tools requiring lightweight neural networks

> This project is **not** a production-level framework and very inefficient but great for learning how neural network works.

## 🔮 Planned Improvements

* Better optimization options (GPU Integration)
* Training performance graphs
* Loss function visualization
* More control

## 📜 License

This project is open-source and free to use for learning, experimentation, and personal projects.
