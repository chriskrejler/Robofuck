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
        int child = 1;

        for (Genome gene: population) {
            System.out.println("Generation : " + generationPublic + " Child: " + child);
            gene.setFitness(brain.run(gene));
            child++;
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

            float points = topGenome.getPoints();
            System.out.println("Points: " + points);
            if(points>1500){
                break;
            }
//            System.out.println("Population : " + pool.getCurrentPopulation() );
            System.out.println("Generation : " + generation );

            pool.breedNewGeneration();
            generation++;
            generationPublic = generation;
        }
        //System.out.println(topGenome.evaluateNetwork(new float[]{1,0})[0]);
    }

    int generationPublic;
    private Brain brain;
}
