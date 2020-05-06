package GeneticAlgo;

import Krislet2.Pair;

class Genotype { //the encoded form of each players behaviour
    Pair shot;//each player can be represented by a vector array
    //using Vec2s because thats what box2d uses

    //-----------------------------------------------------------------------------------------------------------------------------
    //constructor
    Genotype() {
        shot = new Pair(0.0, 0.0);//only starts with a single shot
        randomize();//set all the shots as random vectors
    }
    //------------------------------------------------------------------------------------------------------------------------------
    //Mutation function for genetic algorithm
    void mutate() {
        double mutationRate =0.8; //mutate 80% of the players

        float rand = random(1);
        if (rand<mutationRate) {//80% of the time mutate the player
            if (rand<mutationRate/5) {
                //20% of the time change the vector to a random vector
                shot = new Pair(random(-1, 1), random(-1, 1));
            } else {
                //80% of the time rotate the vector a small amount
                shot.first = shot.first; //Randomize a little
                shot.second = shot.second; //Randomize a little
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------
//returns a clone
    Genotype clone() {
        Genotype clone = new Genotype();
        clone.shot = shot.clone();
        return clone;
    }
}