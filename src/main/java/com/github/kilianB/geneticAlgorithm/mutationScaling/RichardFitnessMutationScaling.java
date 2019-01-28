package com.github.kilianB.geneticAlgorithm.mutationScaling;

/**
 * @author Kilian
 *
 */
public class RichardFitnessMutationScaling implements MutationScalingStrategy {

	
	private double firstFitness = 0;
	private MutationScalingStrategy internal = MutationScalingStrategy.RICHARD;
	
	@Override
	public double computeScaleFactor(int currentGeneration, int maxGeneration, double currentlyBestFitness,
			double targetFitness, int currentStallGeneration) {

		if (firstFitness == 0) {
			firstFitness = currentlyBestFitness - targetFitness;
		}

		double fraction =  100-(((currentlyBestFitness-targetFitness) / firstFitness)*100);
		//Pass to richard
		return internal.computeScaleFactor((int)fraction,100,0,0,0);
	}

	@Override
	public void reset() {
		firstFitness = 0;
		internal.reset();
		
	}
	
}
