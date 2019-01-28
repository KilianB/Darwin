package com.github.kilianB.geneticAlgorithm.selection;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

/**
 * Stochastic uniform, lays out a line in which each parent corresponds to a
 * section of the line of length proportional to its scaled value. The algorithm
 * moves along the line in steps of equal size. At each step, the algorithm
 * allocates a parent from the section it lands on. The first step is a uniform
 * random number less than the step size.
 * 
 * @author Kilian
 *
 */
public class StochasticUniform implements SelectionStrategy{

	@Override
	public Individual[] selectParents(ScaledFitness[] scaledFitness, int count) {
		
		Individual[] selectedParents = new Individual[count];
				
		int curIndex = 0;
		double currentMaxLocationOfIndividual = 0;
		
		
		for(int i = 0; i < count; i++) {
			
			//Since values are scaled we simply can use i as step size
			//The line can be divided into n*count many parts
			double location = i + RNG.nextDouble();
			//since we are only increasing we can simply move forward
			while(currentMaxLocationOfIndividual < location) {
				currentMaxLocationOfIndividual += scaledFitness[curIndex++].getScaledFitness();
			};
			//This is the parent we want to work with
			selectedParents[i] = scaledFitness[curIndex-1].getIndividual();
		}
		
		return selectedParents;
	}

	@Override
	public String toString() {
		return "StochasticUniform";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getClass().getName().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

}
