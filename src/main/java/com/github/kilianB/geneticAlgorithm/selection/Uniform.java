package com.github.kilianB.geneticAlgorithm.selection;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

/**
 * The uniform selection strategy discards fitness values and randomly picks one of the individuals.
 * This approach is not useful for production but more or less a debug option
 * @author Kilian
 *
 */
@Deprecated
public class Uniform implements SelectionStrategy{

	@Override
	public Individual[] selectParents(ScaledFitness[] scaledFitness, int count) {
		Individual[] returnValues = new Individual[count];
		
		for(int i = 0; i < count; i++) {
			returnValues[i] = scaledFitness[RNG.nextInt(scaledFitness.length)].getIndividual();
		}
		return returnValues;
	}

	@Override
	public String toString() {
		return "Uniform(DEPRECATED!)";
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
