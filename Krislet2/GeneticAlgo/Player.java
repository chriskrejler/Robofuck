package GeneticAlgo;

import Krislet2.Krislet;
import Krislet2.Memory;

class Player {
    Genotype DNA;//the behaviour of the player
    int upToShot = 0;//which point in the shots array the player is up to
    float fitness;// the quality of the player used for natural selection
    Ball whiteBall;
    Ball[] balls;
    boolean gameOver = false;
    boolean won = false;//whether the player has sunk all the balls with the black ball last
    Memory World;//the box2d world that the player is playing in
    Krislet Sender;

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //constructor
    Player(Memory world, Krislet sender) {
        World = world;
        Sender = sender;
        DNA = new Genotype();
        fitness =0;
    }


    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //apply a force to the ball in the direction of the next vector in the DNA
    void shoot() {
        //apply the force
        Sender.kick((Double) DNA.shots.first, (Double) DNA.shots.second);
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //sets the fitness based on where the balls ended up
    void calculateFitness(int ballsSunkPreviously) { //ballsSunkPreviously is the number of balls sunk before this shot

        fitness = 0;
        if (whiteBall.sunk) {
            return;//if white ball sunk then finish with the fitness =0
        }
        float totalDistance = 0;//the sum of all the distances of the balls
        int ballsSunk = 0;//number of balls in pockets
        for (int i =0; i<balls.length; i++) {
            if (!balls[i].sunk) { //if the ball isnt sunk calculate the distance to the closest hole
                float min = 1000;
                Vec2 ballPos = balls[i].world.getBodyPixelCoord(balls[i].body);
                for (int j= 0; j<6; j++) {
                    if (dist(tables[0].holes[j].pos.x, tables[0].holes[j].pos.y, ballPos.x, ballPos.y) < min) {
                        min = dist(tables[0].holes[j].pos.x, tables[0].holes[j].pos.y, ballPos.x, ballPos.y);
                    }
                }
                totalDistance += min;//add the smallest distance to a whole to the total
            } else {//if the ball is sunk
                ballsSunk ++;
                if (i == 4 && !blackBallIsLast()) {//if the black ball is sunk and it isnt the last ball
                    fitness = 0;//game over
                    gameOver = true;
                    return;
                }
            }
        }

        if ( totalDistance==0) { //if all balls sunk
            fitness = 1000;
        } else {
            fitness = ((1 +(ballsSunk - ballsSunkPreviously))*(1+(ballsSunk - ballsSunkPreviously)))/(totalDistance);//fitness function
        }
    }


    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //returns a clone with the same DNA as this player
    Player clone(Memory world, Krislet sender) {
        Player clone = new Player(world, sender);
        clone.DNA = DNA.clone();
        return clone;
    }


    //-----------------------------------------------------------------------------------------------------------------------------------------------
    //true if the game is won
    boolean gameFinished() {

        //Tilføj en bool i memory der bliver sat når player hører coach skifte game mode
        won = true;

        return true;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    //true if the black ball is/was the last on the table
    boolean blackBallIsLast() {

        for (int i =0; i<balls.length; i++) {
            if (i != 4 && !balls[i].isInHole()) {
                return false;
            }
        }
        return true;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    //returns the number of balls sunk
    int ballsSunk() {
        int ballsSunk = 0;
        for (int i =0; i<balls.length; i++) {
            if (balls[i].sunk) {
                ballsSunk+=1;
            }
        }

        return ballsSunk;
    }
}