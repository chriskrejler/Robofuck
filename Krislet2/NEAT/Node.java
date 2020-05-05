package NEAT;

import java.util.ArrayList;

import static java.lang.Math.pow;

public class Node {
    private int number;
    private double inputSum = 0;
    private double outputValue = 0;
    ArrayList<ConnectionGene> outputConnections;
    private int layer = 0;

    public Node(int number) {
        this.number = number;
    }

    public void engage() {
        if (this.layer != 0) {
            this.outputValue = this.sigmoid(this.inputSum);
        }

        for (int i = 0; i < this.outputConnections.size(); i++) {
            if (this.outputConnections.get(i).isEnabled()) {
                //add the weighted output to the sum of the inputs of whatever node this node is connected to
                this.outputConnections.get(i).getToNode().inputSum += this.outputConnections.get(i).getWeight() * this.outputValue;
            }
        }
    }

    public boolean isConnectedTo (Node node) {
        if(node.layer == this.layer) { //nodes in the same this.layer cannot be connected
            return false;
        }

        //you get it
        if(node.layer < this.layer) {
            for(var i = 0; i < node.outputConnections.size(); i++) {
                if(node.outputConnections.get(i).getToNode() == this) {
                    return true;
                }
            }
        } else {
            for(var i = 0; i < this.outputConnections.size(); i++) {
                if(this.outputConnections.get(i).getToNode() == node) {
                    return true;
                }
            }
        }

        return false;
    }

    //sigmoid activation function
    public double sigmoid(double x) {
        return 1.0 / (1.0 + pow(Math.E, -4.9 * x)); //todo check pow
    }

    public Node clone () {
        Node node = new Node(this.number);
        node.setLayer(this.layer);

        return node;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setInputSum(double inputSum) {
        this.inputSum = inputSum;
    }

    public void setOutputValue(double outputValue) {
        this.outputValue = outputValue;
    }

    public void setOutputConnections(ArrayList<ConnectionGene> outputConnections) {
        this.outputConnections = outputConnections;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getNumber() {
        return number;
    }

    public double getInputSum() {
        return inputSum;
    }

    public double getOutputValue() {
        return outputValue;
    }

    public ArrayList<ConnectionGene> getOutputConnections() {
        return outputConnections;
    }

    public int getLayer() {
        return layer;
    }
}
