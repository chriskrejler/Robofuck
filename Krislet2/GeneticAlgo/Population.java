package GeneticAlgo;

class Population {
    Player[] players;// all the players
    int generation = 1;
    float fitnessSum;
    int bestPlayerNo;//the array position of the best player
    boolean foundWinner = false; // true if a player has won
    int ballsSunk = 0; //number of balls sunk in the previous shot, used for calculating fitness

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    Population(int size) {
        players = new Player[size];
        for (int i =0; i< players.length; i++) {//create the players
            players[i] = new Player(box2d[i]);
        }
        bestPlayerNo = 0;
    }

    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //update all players
    void update() {
        for (int i =0; i< players.length; i++) {
            players[i].update();
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //calculate the fitness of all the players
    void calculateFitness() {
        for (int i =0; i< players.length; i++) {
            players[i].calculateFitness(ballsSunk);
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
            foundWinner = true;

            //reset the population
            Player[] newPlayers = new Player[players.length];//Create new players array for the next generation
            ballsSunk = players[bestPlayerNo].ballsSunk();
            resetWorlds();//remove all bodies from the worlds
            for (int i =0; i< players.length; i++) {//for each player
                newPlayers[i] = players[bestPlayerNo].clone(box2d[i]);//set it as the clone of the best player
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

        float rand = random(fitnessSum);
        float runningSum = 0;
        for (int i = 0; i< players.length; i++) {

            runningSum += players[i].fitness;
            if (runningSum > rand) {
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
            if (!players[i].gameOver|| !players[i].ballsStopped()) {
                return false;
            }
        }
        return true;
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------
    //increase the amount of shots each player does
    void increaseShots() {
        for (int i =0; i< players.length; i++) {
            players[i].DNA.increaseShotLength();
        }
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //sets all the players as clones of the best with a random additional shot added to the end
    void resetPopulation() {
        Player[] newPlayers = new Player[players.length];//Create new players array for the next generation
        setBestPlayer();//get the best player
        ballsSunk = players[bestPlayerNo].ballsSunk();
        resetWorlds();//remove all bodies from the worlds
        for (int i =0; i< players.length; i++) {//for each player
            newPlayers[i] = players[bestPlayerNo].clone(box2d[i]);//set the player as a clone of the best
        }

        players = newPlayers.clone();
        increaseShots();//increments to amount of shots by 1
        generation +=1;
    }
    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //natural selection for the genetic algorithm
    void naturalSelection() {

        resetWorlds();//remove all bodies from their worlds
        Player[] newPlayers = new Player[players.length];//Create new players array for the next generation

        setBestPlayer();//set which player is the best

        newPlayers[0] = players[bestPlayerNo].clone(box2d[0]);//add the best player of this generation to the next generation without mutation
        for (int i = 1; i<players.length; i++) {
            //for each remaining spot in the next generation
            newPlayers[i] = selectPlayer().clone(box2d[i]);//select a random player(based on fitness) and clone it
            newPlayers[i].DNA.mutate(); //mutate it
        }

        players = newPlayers.clone();
        generation+=1;
    }
}
