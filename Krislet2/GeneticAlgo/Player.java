package GeneticAlgo;

import Krislet2.Brain;
import Krislet2.Krislet;
import Krislet2.Memory;

class Player {
    Genotype DNA;//the behaviour of the player
    float fitness;// the quality of the player used for natural selection
    boolean gameOver = false;
    boolean done = false;
    Memory World;//the box2d world that the player is playing in
    Krislet Sender;
    int shots = 0;


    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    Player(Brain brain) {
        World = brain.m_memory;
        Sender = brain.m_krislet;
        DNA = new Genotype();
        fitness =0;
    }


    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    void shoot() {
        //apply the force
        Sender.kick((Double) DNA.shot.first, (Double) DNA.shot.second);
    }


    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //sets the fitness based on where the balls ended up
    void calculateFitness() { //ballsSunkPreviously is the number of balls sunk before this shot

        fitness = shots;
        /*
        fitness = 0;
        if ( gameFinished()) {
            fitness = 1000;
        }
         */
    }


    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns a clone with the same DNA as this player
    Player clone(Brain brain) {
        Player clone = new Player(brain);
        clone.DNA = DNA.clone();
        return clone;
    }


    //-----------------------------------------------------------------------------------------------------------------------------------------------
    //true if the game is won
    boolean gameFinished() {

        //Tilføj en bool i memory der bliver sat når player hører coach skifte game mode

        if(shots < 100){
            return false;
        }
        done = true;
        return true;
    }
}