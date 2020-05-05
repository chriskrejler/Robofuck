package NEAT;

import java.util.ArrayList;

public class Player {
    double fitness = 0;
    ArrayList<Double> vision; //the input array fed into the neuralNet
    ArrayList<Double> decision;//the out put of the NN
    double unadjustedFitness;
    int lifespan = 0; //how long the player lived for this.fitness
    int bestScore = 0; //stores the this.score achieved used for replay
    boolean dead = false;
    int score = 0;
    int gen = 0;
    int genomeInputs;
    double genomeOutputs;
    Genome brain;


    public Player() {
        int genomeInputs = 5;
        int genomeOutputs = 2;
        // TODO: Har bare sat den til false, ved ikke hvorfor han ikke skrvier noget
        brain = new Genome(this.genomeInputs, this.genomeOutputs,false);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    public void show() {
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<replace
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    public void move() {
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<replace
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    public void update() {
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<replace
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------------------

    public void look() {
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<replace
    }


    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //gets the output of the this.brain then converts them to actions
    public void think() {
        double max = 0;
        int maxIndex = 0;
        //get the output of the neural network
        this.decision = this.brain.feedForward(this.vision);

        for (var i = 0; i < this.decision.size(); i++) {
            if (this.decision.get(i) > max) {
                max = this.decision.get(i);
                maxIndex = i;
            }
        }

        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<replace
    }

    //returns a clone of this player with the same brian
    public Player clone() {
        Player clone = new Player();
        clone.brain = this.brain.clone();
        clone.fitness = this.fitness;
        clone.brain.generateNetwork();
        clone.gen = this.gen;
        clone.bestScore = this.score;
        return clone;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //since there is some randomness in games sometimes when we want to replay the game we need to remove that randomness
    //this fuction does that

    public Player cloneForReplay() {
        Player clone = new Player();
        clone.brain = this.brain.clone();
        clone.fitness = this.fitness;
        clone.brain.generateNetwork();
        clone.gen = this.gen;
        clone.bestScore = this.score;

        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<replace
        return clone;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    //fot Genetic algorithm
    public void calculateFitness() {
        this.fitness = randomDouble(0,9);
        //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<replace
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------
    public Player crossover(Player parent2) {

        var child = new Player();
        child.brain = this.brain.crossover(parent2.brain);
        child.brain.generateNetwork();
        return child;
    }

    public double randomDouble(int min, int max) {
        return Math.random() * (max - min + 1) + min;
    }

}
