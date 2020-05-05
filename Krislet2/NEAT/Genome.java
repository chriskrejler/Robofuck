package NEAT;

import java.lang.reflect.Array;
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

    public void fullyConnect (ArrayList<ConnectionHistory> innovationHistory) {
        //this will be a new number if no identical genome has mutated in the same

        for (var i = 0; i < this.inputs; i++) {
            for (var j = 0; j < this.outputs; j++) {
                double connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.nodes.get(i), this.nodes.get(this.nodes.size() - j - 2));
                this.genes.add(new ConnectionGene(this.nodes.get(i), this.nodes.get(this.nodes.size()- j - 2), randomDouble(-1, 0), connectionInnovationNumber));
            }
        }

        double connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.nodes.get(this.biasNode), this.nodes.get(this.nodes.size() - 2));
        this.genes.add(new ConnectionGene(this.nodes.get(this.biasNode), this.nodes.get(this.nodes.size() - 2), randomDouble(-1, 0), connectionInnovationNumber));

        connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.nodes.get(this.biasNode), this.nodes.get(this.nodes.size() - 3));
        this.genes.add(new ConnectionGene(this.nodes.get(this.biasNode), this.nodes.get(this.nodes.size() - 3), randomDouble(-1, 0), connectionInnovationNumber));
        //add the connection with a random array


        //changed this so if error here
        this.connectNodes();
    }

    //adds the conenctions going out of a node to that node so that it can acess the next node during feeding forward
    public void connectNodes() {

        for (var i = 0; i < this.nodes.size(); i++) { //clear the connections
            this.nodes.get(i).outputConnections.clear();
        }

        for (var i = 0; i < this.genes.size(); i++) { //for each connectionGene
            this.genes.get(i).getFromNode().outputConnections.add(this.genes.get(i)); //add it to node
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
    //returns the innovation number for the new mutation
    //if this mutation has never been seen before then it will be given a new unique innovation number
    //if this mutation matches a previous mutation then it will be given the same innovation number as the previous one
    public double getInnovationNumber(ArrayList<ConnectionHistory> innovationHistory, Node fromNode, Node toNode) {
        boolean isNew = true;
        double connectionInnovationNumber = nextConnectionNo; // TODO: Global variabel i hans
        for (var i = 0; i < innovationHistory.size(); i++) { //for each previous mutation
            if (innovationHistory.get(i).matches(this, fromNode, toNode)) { //if match found
                isNew = false; //its not a new mutation
                connectionInnovationNumber = innovationHistory.get(i).getInovationNumber(); //set the innovation number as the innovation number of the match
                break;
            }
        }

        if (isNew) { //if the mutation is new then create an arrayList of varegers representing the current state of the genome
            ArrayList<Double> innoNumbers = new ArrayList<>();
            for (var i = 0; i < this.genes.size(); i++) { //set the innovation numbers
                innoNumbers.add(this.genes.get(i).getInnovationNo());
            }

            //then add this mutation to the innovationHistory
            innovationHistory.add(new ConnectionHistory(fromNode.getNumber(), toNode.getNumber(), connectionInnovationNumber, innoNumbers));
            nextConnectionNo++;
        }
        return connectionInnovationNumber;
    }

    public double randomDouble(int min, int max) {
        return Math.random() * (max - min + 1) + min;
    }

    public double randomInteger(int min, int max) {
        return (int)(Math.random() * max - min + 1) + min;
    }

    //returns the node with a matching number
    //sometimes the this.nodes will not be in order
    public Node getNode(int nodeNumber) {
        for (var i = 0; i < this.nodes.size(); i++) {
            if (this.nodes.get(i).getNumber() == nodeNumber) {
                return this.nodes.get(i);
            }
        }
        return null;
    }

    //feeding in input values varo the NN and returning output array
    public ArrayList<Double> feedForward(ArrayList<Double> inputValues) {
        //set the outputs of the input this.nodes
        for (var i = 0; i < this.inputs; i++) {
            this.nodes.get(i).setOutputValue(inputValues.get(i));
        }
        this.nodes.get(this.biasNode).setOutputValue(1); //output of bias is 1

        for (var i = 0; i < this.network.size(); i++) { //for each node in the network engage it(see node class for what this does)
            this.network.get(i).engage();
        }

        //the outputs are this.nodes[inputs] to this.nodes [inputs+outputs-1]
        ArrayList<Double> outs = new ArrayList<>();
        for (var i = 0; i < this.outputs; i++) {
            outs.add(this.nodes.get(this.inputs + i).getOutputValue());
        }

        for (var i = 0; i < this.nodes.size(); i++) { //reset all the this.nodes for the next feed forward
            this.nodes.get(i).setInputSum(0);
        }

        return outs;
    }

    //sets up the NN as a list of this.nodes in order to be engaged

    public void generateNetwork() {
        this.connectNodes();
        this.network.clear();
        //for each layer add the node in that layer, since layers cannot connect to themselves there is no need to order the this.nodes within a layer

        for (var l = 0; l < this.layers; l++) { //for each layer
            for (var i = 0; i < this.nodes.size(); i++) { //for each node
                if (this.nodes.get(i).getLayer() == l) { //if that node is in that layer
                    this.network.add(this.nodes.get(i));
                }
            }
        }
    }


    //mutate the NN by adding a new node
    //it does this by picking a random connection and disabling it then 2 new connections are added
    //1 between the input node of the disabled connection and the new node
    //and the other between the new node and the output of the disabled connection
    public void addNode(ArrayList<ConnectionHistory> innovationHistory) {
        //pick a random connection to create a node between
        if (this.genes.size() == 0) {
            this.addConnection(innovationHistory);
            return;
        }
        int randomConnection = (int) Math.floor(randomInteger(0, this.genes.size()));

        while (this.genes.get(randomConnection).getFromNode() == this.nodes.get(this.biasNode) && this.genes.size() != 1) { //dont disconnect bias
            randomConnection = (int) Math.floor(randomInteger(0, this.genes.size()));
        }

        this.genes.get(randomConnection).setEnabled(false); //disable it

        var newNodeNo = this.nextNode;
        this.nodes.add(new Node(newNodeNo));
        this.nextNode++;
        //add a new connection to the new node with a weight of 1
        var connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.genes.get(randomConnection).getFromNode(), this.getNode(newNodeNo));
        this.genes.add(new ConnectionGene(this.genes.get(randomConnection).getFromNode(), this.getNode(newNodeNo), 1, connectionInnovationNumber));


        connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.getNode(newNodeNo), this.genes.get(randomConnection).getToNode());
        //add a new connection from the new node with a weight the same as the disabled connection
        this.genes.add(new ConnectionGene(this.getNode(newNodeNo), this.genes.get(randomConnection).getToNode(), this.genes.get(randomConnection).getWeight(), connectionInnovationNumber));
        this.getNode(newNodeNo).setLayer(this.genes.get(randomConnection).getFromNode().getLayer() + 1);


        connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.nodes.get(this.biasNode), this.getNode(newNodeNo));
        //connect the bias to the new node with a weight of 0
        this.genes.add(new ConnectionGene(this.nodes.get(this.biasNode), this.getNode(newNodeNo), 0, connectionInnovationNumber));

        //if the layer of the new node is equal to the layer of the output node of the old connection then a new layer needs to be created
        //more accurately the layer numbers of all layers equal to or greater than this new node need to be incrimented
        if (this.getNode(newNodeNo).getLayer() == this.genes.get(randomConnection).getToNode().getLayer()) {
            for (var i = 0; i < this.nodes.size() - 1; i++) { //dont include this newest node
                if (this.nodes.get(i).getLayer() >= this.getNode(newNodeNo).getLayer()) {
                    this.nodes.get(i).setLayer(this.nodes.get(i).getLayer() + 1);
                }
            }
            this.layers++;
        }
        this.connectNodes();
    }


    //------------------------------------------------------------------------------------------------------------------
    //adds a connection between 2 this.nodes which aren't currently connected
    public void addConnection(ArrayList<ConnectionHistory> innovationHistory) {
        //cannot add a connection to a fully connected network
        if (this.fullyConnected()) {
            System.out.println("Connection failed");
            return;
        }


        //get random this.nodes
        int randomNode1 = (int) Math.floor(randomInteger(0, this.nodes.size()));
        int randomNode2 = (int) Math.floor(randomInteger(0, this.nodes.size()));
        while (this.randomConnectionNodesAreShit(randomNode1, randomNode2)) { //while the random this.nodes are no good
            //get new ones
            randomNode1 = (int) Math.floor(randomInteger(0, this.nodes.size()));
            randomNode2 = (int) Math.floor(randomInteger(0, this.nodes.size()));
        }
        int temp;
        if (this.nodes.get(randomNode1).getLayer() > this.nodes.get(randomNode2).getLayer()) { //if the first random node is after the second then switch
            temp = randomNode2;
            randomNode2 = randomNode1;
            randomNode1 = temp;
        }

        //get the innovation number of the connection
        //this will be a new number if no identical genome has mutated in the same way
        var connectionInnovationNumber = this.getInnovationNumber(innovationHistory, this.nodes.get(randomNode1), this.nodes.get(randomNode2));
        //add the connection with a random array

        this.genes.add(new ConnectionGene(this.nodes.get(randomNode1), this.nodes.get(randomNode2), randomDouble(-1, 0), connectionInnovationNumber)); //changed this so if error here
        this.connectNodes();
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------
    public boolean randomConnectionNodesAreShit(int r1, int r2) {
        if (this.nodes.get(r1).getLayer() == this.nodes.get(r2).getLayer()) return true; // if the this.nodes are in the same layer
        if (this.nodes.get(r1).isConnectedTo(this.nodes.get(r2))) return true; //if the this.nodes are already connected



        return false;
    }


    //returns whether the network is fully connected or not
    public boolean fullyConnected() {
        int maxConnections = 0;
        ArrayList<Integer> nodesInLayers = new ArrayList<>(); //array which stored the amount of this.nodes in each layer

        for (var i = 0; i < this.layers; i++) {
            nodesInLayers.set(i, 0);
        }
        //populate array
        for (var i = 0; i < this.nodes.size(); i++) {
            nodesInLayers.set(this.nodes.get(i).getLayer(), this.nodes.get(i).getLayer()+1);
        }
        //for each layer the maximum amount of connections is the number in this layer * the number of this.nodes infront of it
        //so lets add the max for each layer together and then we will get the maximum amount of connections in the network
        for (var i = 0; i < this.layers - 1; i++) {
            var nodesInFront = 0;
            for (var j = i + 1; j < this.layers; j++) { //for each layer infront of this layer
                nodesInFront += nodesInLayers.get(j); //add up this.nodes
            }

            maxConnections += nodesInLayers.get(i) * nodesInFront;
        }

        if (maxConnections <= this.genes.size()) { //if the number of connections is equal to the max number of connections possible then it is full
            return true;
        }

        return false;
    }


    //-------------------------------------------------------------------------------------------------------------------------------
    //mutates the genome
    public void mutate(ArrayList<ConnectionHistory> innovationHistory) {
        if (this.genes.size() == 0) {
            this.addConnection(innovationHistory);
        }


        double rand1 = randomDouble(0,0);
        if (rand1 < 0.8) { // 80% of the time mutate weights

            for (var i = 0; i < this.genes.size(); i++) {
                this.genes.get(i).mutateWeight();
            }
        }

        //5% of the time add a new connection
        double rand2 = randomDouble(0,0);
        if (rand2 < 0.05) {

            this.addConnection(innovationHistory);
        }

        //1% of the time add a node
        double rand3 = randomDouble(0,0);
        if (rand3 < 0.01) {

            this.addNode(innovationHistory);
        }
    }


    //---------------------------------------------------------------------------------------------------------------------------------
    //called when this Genome is better that the other parent
    public Genome crossover(Genome parent2) {
        Genome child = new Genome(this.inputs, this.outputs, true);
        child.genes.clear();
        child.nodes.clear();
        child.layers = this.layers;
        child.nextNode = this.nextNode;
        child.biasNode = this.biasNode;
        ArrayList<ConnectionGene> childGenes = new ArrayList<>();
        ArrayList<Boolean> isEnabled = new ArrayList<>();
        // var childGenes = []; // new ArrayList<connectionGene>();//list of genes to be inherrited form the parents
        //var isEnabled = []; // new ArrayList<Boolean>();
        //all inherited genes
        for (var i = 0; i < this.genes.size(); i++) {
            boolean setEnabled = true; //is this node in the chlid going to be enabled

            int parent2gene = this.matchingGene(parent2, (int) this.genes.get(i).getInnovationNo());
            if (parent2gene != -1) { //if the genes match
                if (!this.genes.get(i).isEnabled() || !parent2.genes.get(parent2gene).isEnabled()) { //if either of the matching genes are disabled

                    if (randomDouble(0,0) < 0.75) { //75% of the time disabel the childs gene
                        setEnabled = false;
                    }
                }
                double rand = randomDouble(0,0);
                if (rand < 0.5) {
                    childGenes.add(this.genes.get(i));

                    //get gene from this fucker
                } else {
                    //get gene from parent2
                    childGenes.add(parent2.genes.get(parent2gene));
                }
            } else { //disjoint or excess gene
                childGenes.add(this.genes.get(i));
                setEnabled = this.genes.get(i).isEnabled();
            }
            isEnabled.add(setEnabled);
        }

        //since all excess and disjovar genes are inherrited from the more fit parent (this Genome) the childs structure is no different from this parent | with exception of dormant connections being enabled but this wont effect this.nodes
        //so all the this.nodes can be inherrited from this parent
        for (var i = 0; i < this.nodes.size(); i++) {
            child.nodes.add(this.nodes.get(i).clone());
        }

        //clone all the connections so that they connect the childs new this.nodes

        for (var i = 0; i < childGenes.size(); i++) {
            child.genes.add(childGenes.get(i).clone(child.getNode(childGenes.get(i).getFromNode().getNumber()), child.getNode(childGenes.get(i).getToNode().getNumber())));
            child.genes.get(i).setEnabled(isEnabled.get(i));
        }

        child.connectNodes();
        return child;
    }

        //----------------------------------------------------------------------------------------------------------------------------------------
        //returns whether or not there is a gene matching the input innovation number  in the input genome
        public int matchingGene(Genome parent2, int innovationNumber) {
            for (var i = 0; i < parent2.genes.size(); i++) {
                if (parent2.genes.get(i).getInnovationNo() == innovationNumber) {
                    return i;
                }
            }
            return -1; //no matching gene found
        }

    //----------------------------------------------------------------------------------------------------------------------------------------
    //prints out info about the genome to the console
    public void printGenome() {
        System.out.println("Prvar genome  layers:" + this.layers);
        System.out.println("bias node: " + this.biasNode);
        System.out.println("this.nodes");
        for (var i = 0; i < this.nodes.size(); i++) {
            System.out.println(this.nodes.get(i).getNumber() + ",");
        }
        System.out.println("Genes");
        for (var i = 0; i < this.genes.size(); i++) { //for each connectionGene
            System.out.println("gene " + this.genes.get(i).getInnovationNo() + "From node " + this.genes.get(i).getFromNode().getNumber() + "To node " + this.genes.get(i).getToNode().getNumber() +
                    "is enabled " + this.genes.get(i).isEnabled() + "from layer " + this.genes.get(i).getFromNode().getLayer() + "to layer " + this.genes.get(i).getToNode().getLayer() + "weight: " + this.genes.get(i).getWeight());
        }

        System.out.println("");
    }


    //----------------------------------------------------------------------------------------------------------------------------------------
    //returns a copy of this genome
    public Genome clone() {

        Genome clone = new Genome(this.inputs, this.outputs, true);

        for (var i = 0; i < this.nodes.size(); i++) { //copy this.nodes
            clone.nodes.add(this.nodes.get(i).clone());
        }

        //copy all the connections so that they connect the clone new this.nodes

        for (var i = 0; i < this.genes.size(); i++) { //copy genes
            clone.genes.add(this.genes.get(i).clone(clone.getNode(this.genes.get(i).getFromNode().getNumber()), clone.getNode(this.genes.get(i).getToNode().getNumber())));
        }

        clone.layers = this.layers;
        clone.nextNode = this.nextNode;
        clone.biasNode = this.biasNode;
        clone.connectNodes();

        return clone;
    }
}
