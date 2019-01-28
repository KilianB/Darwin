package com.github.kilianB.geneticAlgorithm.fitnessScaling;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * Scale the fitness values of the individual to include only the top 
 * x percent. The selected individuals are treated equally while every other
 * individual is discarded (scaled fitness = 0).
 *  
 * @author Kilian
 */
public class TopScaling implements FitnessScalingStrategy {
	/**
	 * How many percent of the top population shall survive?
	 */
	final double topPercentage;

	/**
	 * @param topPercentage
	 *            What percentage of the population are considered good enough for
	 *            reproduction. [0 - 1]. Reasonable 0.4
	 */
	public TopScaling(double topPercentage) {
		if (topPercentage > 1 || topPercentage < 0) {
			throw new IllegalArgumentException("Top percentage may only be in the range of [0-1]");
		}

		this.topPercentage = topPercentage;
	}

	/**
	 * Creates a top scaling approach with a default of 40 percent of the  population.
	 */
	public TopScaling() {
		this(0.4);
	}

	@Override
	public ScaledFitness[] scaleFitness(Individual[] population,int parentsNeeded) {

		int topIndividualCount = Math.max(1,(int) Math.round(population.length * topPercentage));
		double scaleFactor = parentsNeeded / (double) topIndividualCount;
		
		ScaledFitness[] scaledFitness = new ScaledFitness[population.length];

		//TODO check if we really get the correct output for wired top scaling percentages
		
		for (int i = 0; i < population.length; i++) {

			if (i < topIndividualCount) {
				scaledFitness[i] = new ScaledFitness(scaleFactor, population[i]);
			} else {
				scaledFitness[i] = new ScaledFitness(0d, population[i]);
			}
		}
		return scaledFitness;
	}

	@Override
	public String toString() {
		return "TopScaling [topPercentage=" + topPercentage + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(topPercentage);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		TopScaling other = (TopScaling) obj;
		if (Double.doubleToLongBits(topPercentage) != Double.doubleToLongBits(other.topPercentage))
			return false;
		return true;
	}
	
	
}
