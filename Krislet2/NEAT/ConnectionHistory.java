package NEAT;

import java.util.ArrayList;

public class ConnectionHistory {
    private Node fromNode;
    private Node toNode;
    private double inovationNumber;
    private ArrayList<Double> innovationNumbers;

    public ConnectionHistory(Node fromNode, Node toNode, double inovationNumber, ArrayList<Double> innovationNumbers) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.inovationNumber = inovationNumber;
        this.innovationNumbers = innovationNumbers; // TODO: Check hvis det her er det samme som arrayCopy();
    }

    public boolean matches(Genome genome, Node from, Node to){
        // LAV EFTER GENOME
    }
}
