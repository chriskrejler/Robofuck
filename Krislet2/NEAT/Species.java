package NEAT;

import java.util.ArrayList;

public class Species {
    ArrayList<Player> players = new ArrayList<>();
    double bestFitness = 0;
    Player champ;
    double averageFitness = 0;
    int staleness = 0; //how many generations the species has gone without an improvement
    Genome rep;

    //--------------------------------------------
    //coefficients for testing compatibility
    int excessCoeff = 1;
    double weightDiffCoeff = 0.5;
    int compatibilityThreshold = 3;

    Species (Player p) {
        if (p != null) {
            this.players.add(p);
            //since it is the only one in the species it is by default the best
            this.bestFitness = p.fitness;
            this.rep = p.brain.clone();
            this.champ = p.cloneForReplay();
        }
    }

}
