package GeneticAlgo;

import Krislet2.Pair;

import java.util.Random;

class Genotype { //the encoded form of each players behaviour
    Pair shot;//each player can be represented by a vector array
    //using Vec2s because thats what box2d uses

    //-----------------------------------------------------------------------------------------------------------------------------
    //constructor
    Genotype() {
        Random rand = new Random();
        shot = new Pair(0.0, 0.0);//TODO randomize the power
    }
    //------------------------------------------------------------------------------------------------------------------------------
    //Mutation function for genetic algorithm
    void mutate() {
        Random rand = new Random();

        double mutationRate =0.8; //mutate 80% of the players

        double temp = rand.nextDouble();
        int temp1;

        if (temp<mutationRate) {//80% of the time mutate the player
            if (temp<mutationRate/5) {
                //20% of the time change the vector to a random vector

                if(rand.nextBoolean()){
                    shot = new Pair(rand.nextInt(25), rand.nextInt(101));
                }else{
                    shot = new Pair(-rand.nextInt(25), rand.nextInt(101));
                }
            } else {
                //80% of the time rotate the vector a small amount
                shot.first = shot.first ; //Randomize a little
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