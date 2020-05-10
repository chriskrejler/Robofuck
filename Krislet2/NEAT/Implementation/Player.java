package NEAT.Implementation;

import Krislet.Brain;
import NEAT.Environment;
import NEAT.Genome;
import NEAT.Pool;
import Krislet.Krislet;

import java.util.ArrayList;

/**
 * Created by vishnughosh on 05/03/17.
 */
public class Player implements Environment {
    @Override
    public void evaluateFitness(ArrayList<Genome> population) {

        for (Genome gene: population) {
            gene.setFitness(brain.run(gene));
        }
    }

    public Player(Brain braino){
        brain = braino;

        Pool pool = new Pool();
        pool.initializePool();
        Genome topGenome;
        int generation = 0;


        while(true){
            //pool.evaluateFitness();

            pool.evaluateFitness(this);
            topGenome = pool.getTopGenome();
            //System.out.println("TopFitness : " + topGenome.getPoints());

            if(topGenome.getPoints()>95){
                break;
            }
//            System.out.println("Population : " + pool.getCurrentPopulation() );
            System.out.println("Generation : " + generation );

            pool.breedNewGeneration();
            generation++;
        }
        //System.out.println(topGenome.evaluateNetwork(new float[]{1,0})[0]);
    }

    private Brain brain;
}
