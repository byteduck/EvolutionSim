package net.codepixl.EvolutionSim;

import java.util.ArrayList;
import java.util.Collections;

public class InfoPoint{
	double best,worst,average;
	ArrayList<Creature> creatures;
	public InfoPoint(ArrayList<Creature> creatures){
		creatures = (ArrayList<Creature>) creatures.clone();
		this.creatures = creatures;
		Collections.sort(creatures);
		this.best = creatures.get(0).maxDistance;
		this.worst = creatures.get(creatures.size()-1).maxDistance;
		double total = 0;
		for(Creature c : creatures)
			total+=c.maxDistance;
		this.average = total/creatures.size();
	}
}
