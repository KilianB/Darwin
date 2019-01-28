package com.github.kilianB.geneticAlgorithm.fitnessScaling;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * Rank scaling assumes that the population size and the amount of parents
 * needed stays consistent throughout the algorithm.
 * 
 * The fitness values are scaled by rank.
 * 
 * 1/Math.sqrt(rank)
 * 
 * @author Kilian
 *
 */
public class RankScaling implements FitnessScalingStrategy {

	@Override
	public ScaledFitness[] scaleFitness(Individual[] population, int parentsNeeded) {
		
		double scaleFactor = 0;
		 
		// total fitness scaling
		
		//Fix if 2 individuals have the exact same fitness value give them the same rank
		int curRank = 0;
		double lastFitness = Double.MAX_VALUE;
		double[] rawRank = new double[population.length];
		for (int i = 0; i < population.length; i++) {
			//No need for epsilon as the value is only calculated once!
			double fitness = population[i].getFitness();
			if(fitness != lastFitness) {
				curRank = i+1;
			}
			rawRank[i] = 1 / Math.sqrt(curRank);
			scaleFactor += rawRank[i];
			lastFitness = fitness;
		}

		scaleFactor = parentsNeeded / scaleFactor;

		ScaledFitness[] scaledFitness = new ScaledFitness[population.length];
		
		for (int i = 0; i < population.length; i++) {
			scaledFitness[i] = new ScaledFitness(rawRank[i] * scaleFactor, population[i]);
		}

		return scaledFitness;
	}

	@Override
	public String toString() {
		return "RankScaling ";
	}
	
	@Override
	public int hashCode() {
		return getClass().getName().hashCode();
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
