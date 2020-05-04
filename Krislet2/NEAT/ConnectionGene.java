package NEAT;

import java.util.Random;

public class ConnectionGene {
    private Node fromNode;
    private Node toNode;
    private double weight;
    private boolean enabled = true;
    private double innovationNo;
    private Random rand = new Random();

    public ConnectionGene(Node fromNode, Node toNode, double weight, double innovationNo) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.weight = weight;
        this.innovationNo = innovationNo;
    }

    public void mutateWeight () {
        double random = randomDouble(0,0); // 0-1
        if (random < 0.1) {
            this.weight = randomDouble(-1,0);
        } else {
            // TODO: Implement gaussian method?
        }

        // Keep weights between bounds
        if (this.weight > 1) {
            this.weight = 1;
        }

        if (this.weight < -1) {
            this.weight = -1;
        }
    }

    public ConnectionGene clone(Node from, Node to){
        return new ConnectionGene(from, to, this.weight, this.innovationNo);
    }

    public double randomDouble(int min, int max) {
        return Math.random() * (max - min + 1) + min;
    }


    // GETTERS AND SETTERS

    public void setFromNode(Node fromNode) {
        this.fromNode = fromNode;
    }

    public void setToNode(Node toNode) {
        this.toNode = toNode;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setInnovationNo(double innovationNo) {
        this.innovationNo = innovationNo;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }

    public Node getFromNode() {
        return fromNode;
    }

    public Node getToNode() {
        return toNode;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getInnovationNo() {
        return innovationNo;
    }

    public Random getRand() {
        return rand;
    }
}
