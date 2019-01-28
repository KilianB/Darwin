package com.github.kilianB.geneticAlgorithm.mutationScaling;

/**
 * Sin scaling maps a sine curve to 1 and 0 bounds marking x full revolution
 * from 0 - max generations.
 * 
 * 
 * @author Kilian
 *
 */
public class SinMutationScaling implements MutationScalingStrategy {

	double rev;

	public SinMutationScaling(double rev) {
		this.rev = rev;
	}

	@Override
	public double computeScaleFactor(int currentGeneration, int maxGeneration, double currentlyBestFitness,
			double targetFitness, int currentStallGeneration) {
		double rad = 2 * Math.PI;
		return Math.sin((currentGeneration / (double) maxGeneration) * rad * rev + (Math.PI / 2)) / 2 + 0.5;
	}

}