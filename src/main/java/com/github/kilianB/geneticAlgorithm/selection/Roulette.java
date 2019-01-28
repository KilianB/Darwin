package com.github.kilianB.geneticAlgorithm.selection;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

/**
 * Roulette selection chooses parents by simulating a roulette wheel, in which
 * the area of the section of the wheel corresponding to an individual is
 * proportional to the individual's expectation. The algorithm uses a random
 * number to select one of the sections with a probability equal to its area.
 * 
 * @author Kilian
 * @see https://se.mathworks.com/help/gads/genetic-algorithm-options.html#f6593
 */
public class Roulette implements SelectionStrategy {

	@Override
	public Individual[] selectParents(ScaledFitness[] scaledFitness, int count) {

		Individual[] generatedParents = new Individual[count];

		for (int i = 0; i < count; i++) {
			// The total area is = the count of parents to generate
			double currentCount = 0;
			double roulette = RNG.nextDouble() * count;

			for (int j = 0; j < scaledFitness.length; j++) {

				currentCount += scaledFitness[j].getScaledFitness();
				if (roulette <= currentCount) {
					// The section of the wheel the ball landed on
					generatedParents[i] = scaledFitness[j].getIndividual();
					break;
				}
			}
		}
		return generatedParents;
	}

	@Override
	public String toString() {
		return "Roulette";
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
