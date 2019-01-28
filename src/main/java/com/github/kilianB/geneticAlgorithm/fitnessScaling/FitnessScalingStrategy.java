package com.github.kilianB.geneticAlgorithm.fitnessScaling;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * Fitness scaling transforms the fitness values of individuals to a common
 * range of (0 - parentsNeeded). Contrary to the fitness a higher scaled fitness
 * indicates a better individual.
 * <p>
 * 
 * The scaled fitness is used as imput for the
 * {@link com.github.kilianB.geneticAlgorithm.selection.SelectionStrategy
 * SelectionStrategy} to choose the parents for the next generation.
 * 
 * <p>
 * The sum of all adjusted fitness values = the the amount of individuals
 * selected per generation.
 * 
 * @author Kilian
 */
@FunctionalInterface
public interface FitnessScalingStrategy {

	/**
	 * Calculate the scaled fitness of the supplied population. Contrary to the
	 * fitness value a higher scaled fitness value indicates a more fit individual.
	 * The values are scaled to a common range to allow crossover and mutation
	 * algorithms to pick suitable candidates.
	 * 
	 * @param population    A population sorted by it's fitness value
	 * @param parentsNeeded the number of parents needed for the next generation.
	 *                      The sum over the entire scaled fitness vector is exactly
	 *                      this value.
	 * @return Sorted scaled fitness of the population.
	 */
	public ScaledFitness[] scaleFitness(Individual[] population, int parentsNeeded);

	/**
	 * Data class bundling a scaled fitness to it's individual
	 * 
	 * @author Kilian
	 *
	 */
	public class ScaledFitness implements Comparable<ScaledFitness> {

		private double scaledFitness;
		private Individual individual;

		public ScaledFitness(double scaledFitness, Individual individual) {
			this.scaledFitness = scaledFitness;
			this.individual = individual;
		}

		@Override
		public int compareTo(ScaledFitness o) {
			return Double.compare(scaledFitness, o.scaledFitness);
		}

		public double getScaledFitness() {
			return scaledFitness;
		}

		public Individual getIndividual() {
			return individual;
		}

		@Override
		public String toString() {
			return "ScaledFitness [scaledFitness=" + scaledFitness + ", individual=" + individual + "]";
		}
	}

}
