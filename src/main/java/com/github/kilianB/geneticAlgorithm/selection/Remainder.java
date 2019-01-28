package com.github.kilianB.geneticAlgorithm.selection;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

/**
 * Remainder selection assigns parents deterministically from the integer part
 * of each individual's scaled value and then uses roulette selection on the
 * remaining fractional part. For example, if the scaled value of an individual
 * is 2.3, that individual is listed twice as a parent because the integer part
 * is 2. After parents have been assigned according to the integer parts of the
 * scaled values, the rest of the parents are chosen stochastically. The
 * probability that a parent is chosen in this step is proportional to the
 * fractional part of its scaled value.
 * 
 * @author Kilian
 * @see https://se.mathworks.com/help/gads/genetic-algorithm-options.html#f6593
 */
public class Remainder implements SelectionStrategy {

	@Override
	public Individual[] selectParents(ScaledFitness[] scaledFitness, int count) {
		
		
		Individual[] generatedParents = new Individual[count];

		//Remainder 
		int curIndex = 0;
		
		//Calculate remaining roulette area
		double rouletteArea = 0;
		
		for(var item : scaledFitness) {
			
			int intPart = (int)item.getScaledFitness();
			
			for(int i = 0; i < intPart; i++) {
				generatedParents[curIndex++] = item.getIndividual();
			}
			
			//Sum up the fractional parts
			rouletteArea += (item.getScaledFitness() - intPart);
			
		}
		
		int parentsNeeded = count - curIndex;
		
	
		//Roulette 
		
		for (int i = 0; i < parentsNeeded; i++) {
			// The total area is = the count of parents to generate
			double currentCount = 0;
			double roulette = RNG.nextDouble() * rouletteArea;
			for (int j = 0; j < scaledFitness.length; j++) {
				
				//only consider the fractional part
				currentCount += (scaledFitness[j].getScaledFitness()) - (int)scaledFitness[j].getScaledFitness();
				
				if (roulette <= currentCount) {
					// The section of the wheel the ball landed on
					generatedParents[curIndex++] = scaledFitness[j].getIndividual();
					break;
				}
			}
		}
		return generatedParents;
	}

	@Override
	public String toString() {
		return "Remainder";
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
