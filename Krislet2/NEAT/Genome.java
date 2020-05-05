package NEAT;

import java.util.ArrayList;

public class Genome {
    int nextConnectionNo = 1000;
    ArrayList<ConnectionGene> genes;
    ArrayList<Node> nodes;
    int inputs;
    double outputs;
    int layers = 2;
    int nextNode = 0; // TODO: Han sætter den til 0 her?
    int biasNode;
    ArrayList<Node> network; //a list of the this.nodes in the order that they need to be considered in the NN

    public Genome(int inputs, double outputs, boolean crossover) {
        this.inputs = inputs;
        this.outputs = outputs;

        // TODO: Det her giver jo ingen mening
        if (crossover){
            return;
        }

        for (var i = 0; i < this.inputs; i++) {
            this.nodes.add(new Node(i));
            this.nextNode++;
            this.nodes.get(i).setLayer(0);
        }

        //create output this.nodes
        for (var i = 0; i < this.outputs; i++) {
            this.nodes.add(new Node(i + this.inputs));
            this.nodes.get(i+this.inputs).setLayer(1);
            this.nextNode++;
        }

        this.nodes.add(new Node(this.nextNode)); //bias node
        this.biasNode = this.nextNode;
        this.nextNode++;
        this.nodes.get(this.biasNode).setLayer(0);
    }

    public void fullyConnect (ConnectionHistory innovationHistory) {
        //this will be a new number if no identical genome has mutated in the same

        for (var i = 0; i < this.inputs; i++) {
            for (var j = 0; j < this.outputs; j++) {
                int connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.nodes[i], this.nodes[this.nodes.length - j - 2]);
                this.genes.push(new connectionGene(this.nodes[i], this.nodes[this.nodes.length - j - 2], random(-1, 1), connectionInnovationNumber));
            }
        }

        var connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.nodes[this.biasNode], this.nodes[this.nodes.length - 2]);
        this.genes.push(new connectionGene(this.nodes[this.biasNode], this.nodes[this.nodes.length - 2], random(-1, 1), connectionInnovationNumber));

        connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.nodes[this.biasNode], this.nodes[this.nodes.length - 3]);
        this.genes.push(new connectionGene(this.nodes[this.biasNode], this.nodes[this.nodes.length - 3], random(-1, 1), connectionInnovationNumber));
        //add the connection with a random array


        //changed this so if error here
        this.connectNodes();
    }

    //adds the conenctions going out of a node to that node so that it can acess the next node during feeding forward
    public void connectNodes() {

        for (var i = 0; i < this.nodes.length; i++) { //clear the connections
            this.nodes[i].outputConnections = [];
        }

        for (var i = 0; i < this.genes.length; i++) { //for each connectionGene
            this.genes[i].fromNode.outputConnections.push(this.genes[i]); //add it to node
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
    //returns the innovation number for the new mutation
    //if this mutation has never been seen before then it will be given a new unique innovation number
    //if this mutation matches a previous mutation then it will be given the same innovation number as the previous one
    public int getInnovationNumber(ArrayList<ConnectionHistory> innovationHistory, Node fromNode, Node toNode) {
        boolean isNew = true;
        int connectionInnovationNumber = nextConnectionNo; // TODO: Global variabel i hans
        for (var i = 0; i < innovationHistory.size(); i++) { //for each previous mutation
            if (innovationHistory.get(i).matches(this, fromNode, toNode)) { //if match found
                isNew = false; //its not a new mutation
                connectionInnovationNumber = innovationHistory[i].innovationNumber; //set the innovation number as the innovation number of the match
                break;
            }
        }

        if (isNew) { //if the mutation is new then create an arrayList of varegers representing the current state of the genome
            var innoNumbers = [];
            for (var i = 0; i < this.genes.length; i++) { //set the innovation numbers
                innoNumbers.push(this.genes[i].innovationNo);
            }

            //then add this mutation to the innovationHistory
            innovationHistory.push(new connectionHistory(from.number, to.number, connectionInnovationNumber, innoNumbers));
            nextConnectionNo++;
        }
        return connectionInnovationNumber;
    }
}
