package GeneticAlgo;

import Krislet2.Brain;

import java.util.Random;

class Population {
    Player[] players;// all the players
    int generation = 1;
    int fitnessSum;
    int bestPlayerNo;//the array position of the best player
    int ballsSunk = 0; //number of balls sunk in the previous shot, used for calculating fitness
    Brain brain;

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    Population(int size, Brain braino) {
        brain = braino;
        players = new Player[size];
        for (int i =0; i< players.length; i++) {//create the players
            players[i] = new Player(brain);
        }
        bestPlayerNo = 0;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //calculate the fitness of all the players
    void calculateFitness() {
        for (int i =0; i< players.length; i++) {
            players[i].calculateFitness();
        }
        setFitnessSum();//add up the fitnesses
        setBestPlayer();//its all in the name
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //calculates the sum of the fitnesses
    void setFitnessSum() {
        fitnessSum = 0;
        for (int i =0; i< players.length; i++) {
            fitnessSum +=  players[i].fitness;
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //finds the greatest fitness and sets that player as the best
    void setBestPlayer() {
        float max =0;
        int maxIndex = 0;
        for (int i =0; i<players.length; i++) {
            if (players[i].fitness > max) {
                max = players[i].fitness;
                maxIndex = i;
            }
        }

        bestPlayerNo = maxIndex;

        if (players[maxIndex].won) {//if a player won

            //reset the population
            Player[] newPlayers = new Player[players.length];//Create new players array for the next generation
            ballsSunk = players[bestPlayerNo].shots;
            for (int i =0; i< players.length; i++) {//for each player
                newPlayers[i] = players[bestPlayerNo].clone(brain);//set it as the clone of the best player
            }

            players = newPlayers.clone();
        }
    }


    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //randomly chooses player from the population to return (considering fitness)
    Player selectPlayer() {
        //this function works by randomly choosing a value between 0 and the sum of all the fitnesses
        //then go through all the players and add their fitness to a running sum and if that sum is greater than the random value generated that player is chosen
        //since players with a higher fitness function add more to the running sum then they have a higher chance of being chosen

        Random rand = new Random();

        float rando = rand.nextInt(fitnessSum);
        float runningSum = 0;
        for (int i = 0; i< players.length; i++) {

            runningSum += players[i].fitness;
            if (runningSum > rando) {
                return players[i];
            }
        }
        //unreachable code to make the parser happy
        return null;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //mutates all the players
    void mutate() {
        for (int i =1; i< players.length; i++) {
            players[i].DNA.mutate();
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //if all the games are finished then return true
    boolean done() {
        for (int i =1; i< players.length; i++) {
            if (!players[i].gameOver|| !players[i].done) {
                return false;
            }
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //sets all the players as clones of the best with a random additional shot added to the end
    void resetPopulation() {
        Player[] newPlayers = new Player[players.length];//Create new players array for the next generation
        setBestPlayer();//get the best player
        ballsSunk = players[bestPlayerNo].shots;
        for (int i =0; i< players.length; i++) {//for each player
            newPlayers[i] = players[bestPlayerNo].clone();//set the player as a clone of the best
        }

        players = newPlayers.clone();
        generation +=1;
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //natural selection for the genetic algorithm
    void naturalSelection() {
        //Reset world


        Player[] newPlayers = new Player[players.length];//Create new players array for the next generation

        setBestPlayer();//set which player is the best

        newPlayers[0] = players[bestPlayerNo].clone();//add the best player of this generation to the next generation without mutation
        for (int i = 1; i<players.length; i++) {
            //for each remaining spot in the next generation
            newPlayers[i] = selectPlayer().clone();//select a random player(based on fitness) and clone it
            newPlayers[i].DNA.mutate(); //mutate it
        }

        players = newPlayers.clone();
        generation+=1;
    }
}
