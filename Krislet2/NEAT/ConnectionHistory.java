package NEAT;

import java.util.ArrayList;

public class ConnectionHistory {
    private double fromNode;
    private double toNode;
    private double inovationNumber;
    private ArrayList<Double> innovationNumbers;

    public ConnectionHistory(double fromNode, double toNode, double inovationNumber, ArrayList<Double> innovationNumbers) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.inovationNumber = inovationNumber;
        this.innovationNumbers = innovationNumbers; // TODO: Check hvis det her er det samme som arrayCopy();
    }

    //----------------------------------------------------------------------------------------------------------------
    //returns whether the genome matches the original genome and the connection is between the same nodes
    public boolean matches(Genome genome, Node from, Node to) {
        if (genome.genes.size() == this.innovationNumbers.size()) { //if the number of connections are different then the genoemes aren't the same
            if (from.getNumber() == this.fromNode && to.getNumber() == this.toNode) {
                //next check if all the innovation numbers match from the genome
                for (var i = 0; i < genome.genes.size(); i++) {
                    if (!this.innovationNumbers.contains(genome.genes.get(i).getInnovationNo())) {
                        return false;
                    }
                }
                //if reached this far then the innovationNumbers match the genes innovation numbers and the connection is between the same nodes
                //so it does match
                return true;
            }
        }
        return false;
    }

    public void setFromNode(double fromNode) {
        this.fromNode = fromNode;
    }

    public void setToNode(double toNode) {
        this.toNode = toNode;
    }

    public void setInovationNumber(double inovationNumber) {
        this.inovationNumber = inovationNumber;
    }

    public void setInnovationNumbers(ArrayList<Double> innovationNumbers) {
        this.innovationNumbers = innovationNumbers;
    }

    public double getFromNode() {
        return fromNode;
    }

    public double getToNode() {
        return toNode;
    }

    public double getInovationNumber() {
        return inovationNumber;
    }

    public ArrayList<Double> getInnovationNumbers() {
        return innovationNumbers;
    }
}
