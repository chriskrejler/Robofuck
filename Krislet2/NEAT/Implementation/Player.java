package NEAT.Implementation;

import Krislet.Brain;
import NEAT.Environment;
import NEAT.Genome;
import NEAT.Pool;

import java.util.ArrayList;

/**
 * Created by vishnughosh on 05/03/17.
 */
public class Player implements Environment {
    @Override
    public void evaluateFitness(ArrayList<Genome> population) {

        for (Genome gene: population) {
            float fitness = 0;
            gene.setFitness(0);

            //TODO Sæt fitness til hvor mange succesful spilninger den lavede


            gene.setFitness(fitness);
        }
    }

    public void Player(Brain brain){
        Player player = new Player();

        Pool pool = new Pool();
        pool.initializePool();

        float inputs[] = {brain.teammate.first.floatValue(), brain.teammate.second.floatValue(), brain.enemy.first.floatValue(), brain.enemy.second.floatValue()};

        Genome topGenome = new Genome();
        int generation = 0;
        while(true){
            //pool.evaluateFitness();

            // Hver gang der skal skydes, skal "gene.evaluateNetwork(inputs);" kaldes
            pool.evaluateFitness(player);
            topGenome = pool.getTopGenome();
            System.out.println("TopFitness : " + topGenome.getPoints());

            if(topGenome.getPoints()>15){
                break;
            }
//            System.out.println("Population : " + pool.getCurrentPopulation() );
            System.out.println("Generation : " + generation );
            //           System.out.println("Total number of matches played : "+TicTacToe.matches);
            //           pool.calculateGenomeAdjustedFitness();

            pool.breedNewGeneration();
            generation++;
        }
        //System.out.println(topGenome.evaluateNetwork(new float[]{1,0})[0]);
    }

    private void decision(int action){
        //TODO create switch that translates and integer between 1-16 to a shot
    }
}
