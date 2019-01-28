package com.github.kilianB.geneticAlgorithm.mutationScaling;

/**
 * 
 * 
 * @author Kilian
 *
 */
public final class LinearFitnessMutationScaling implements MutationScalingStrategy {
	
	private double firstFitness = 0;

	@Override
	public double computeScaleFactor(int currentGeneration, int maxGeneration, double currentlyBestFitness,
			double targetFitness, int currentStallGeneration) {

		if (firstFitness == 0) {
			firstFitness = currentlyBestFitness - targetFitness;
		}

		return ((currentlyBestFitness-targetFitness) / firstFitness);
	}
	
	@Override
	public void reset() {
		firstFitness = 0;
	}
}
