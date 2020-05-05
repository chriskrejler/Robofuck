package NEAT;

import java.util.ArrayList;

public class Population {
    ArrayList<Player> players; //new ArrayList<Player>();
    Player bestPlayer; //the best ever player
    int bestScore = 0; //the score of the best ever player
    int globalBestScore = 0;
    int gen = 1;
    ArrayList<ConnectionHistory> innovationHistory = new ArrayList<>(); // new ArrayList<connectionHistory>();
    ArrayList<Player> genPlayers; //new ArrayList<Player>();
    ArrayList<Species> species; //new ArrayList<Species>();

    boolean massExtinctionEvent = false;
    boolean newStage = false;

    public Population(int size) {
        for(int i = 0; i < size; i++) {
            this.players.add(new Player());
            this.players.get(this.players.size() - 1).brain.mutate(this.innovationHistory);
            this.players.get(this.players.size() - 1).brain.generateNetwork();
        }
    }

    public void updateAlive() {
        for (var i = 0; i < this.players.size(); i++) {
            if (!this.players.get(i).dead) {
                this.players.get(i).look(); //get inputs for brain
                this.players.get(i).think(); //use outputs from neural network
                this.players.get(i).update(); //move the player according to the outputs from the neural network

                // TODO: Det her er en del af hans Sketching
/*                if (!showNothing && (!showBest || i == 0)) {
                    this.players.get(i).show();
                }*/
                if (this.players.get(i).score > this.globalBestScore) {
                    this.globalBestScore = this.players.get(i).score;
                }
            }
        }

    }
    //------------------------------------------------------------------------------------------------------------------------------------------
    //returns true if all the players are dead      sad
    public boolean done() {
        for (var i = 0; i < this.players.size(); i++) {
            if (!this.players.get(i).dead) {
                return false;
            }
        }
        return true;
    }
    //------------------------------------------------------------------------------------------------------------------------------------------
    //sets the best player globally and for thisthis.gen
    public void setBestPlayer() {
        Player tempBest = this.species.get(0).players.get(0);
        tempBest.gen = this.gen;


        //if best thisthis.gen is better than the global best score then set the global best as the best thisthis.gen

        if (tempBest.score >= this.bestScore) {
            this.genPlayers.add(tempBest.cloneForReplay());
            System.out.println("old best: " + this.bestScore);
            System.out.println("new best: " + tempBest.score);
            this.bestScore = tempBest.score;
            this.bestPlayer = tempBest.cloneForReplay();
        }
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------
    //this function is called when all the players in the this.players are dead and a newthis.generation needs to be made
    public void naturalSelection() {

        // this.batchNo = 0;
        Player previousBest = this.players.get(0);
        this.speciate(); //seperate the this.players varo this.species
        this.calculateFitness(); //calculate the fitness of each player
        this.sortSpecies(); //sort the this.species to be ranked in fitness order, best first
        if (this.massExtinctionEvent) {
            this.massExtinction();
            this.massExtinctionEvent = false;
        }
        this.cullSpecies(); //kill off the bottom half of each this.species
        this.setBestPlayer(); //save the best player of thisthis.gen
        this.killStaleSpecies(); //remove this.species which haven't improved in the last 15(ish)this.generations
        this.killBadSpecies(); //kill this.species which are so bad that they cant reproduce

        // if (this.gensSinceNewWorld >= 0 || this.bestScore > (grounds[0].distance - 350) / 10) {
        //   this.gensSinceNewWorld = 0;
        //   console.log(this.gensSinceNewWorld);
        //   console.log(this.bestScore);
        //   console.log(grounds[0].distance);
        //   newWorlds();
        // }

        System.out.println("generation  " + this.gen + "  Number of mutations  " + this.innovationHistory.size() + "  species:   " + this.species.size() + "  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");


        double averageSum = this.getAvgFitnessSum();
        ArrayList<Player> children = new ArrayList<>();
        for (var j = 0; j < this.species.size(); j++) { //for each this.species
            children.add(this.species.get(j).champ.clone()); //add champion without any mutation
            var NoOfChildren = floor(this.species[j].averageFitness / averageSum * this.players.length) - 1; //the number of children this this.species is allowed, note -1 is because the champ is already added
            for (var i = 0; i < NoOfChildren; i++) { //get the calculated amount of children from this this.species
                children.push(this.species[j].giveMeBaby(this.innovationHistory));
            }
        }
        if (children.length < this.players.length) {
            children.push(previousBest.clone());
        }
        while (children.length < this.players.length) { //if not enough babies (due to flooring the number of children to get a whole var)
            children.push(this.species[0].giveMeBaby(this.innovationHistory)); //get babies from the best this.species
        }

        this.players = [];
        arrayCopy(children, this.players); //set the children as the current this.playersulation
        this.gen += 1;
        for (var i = 0; i < this.players.length; i++) { //generate networks for each of the children
            this.players[i].brain.generateNetwork();
        }
    }
}
