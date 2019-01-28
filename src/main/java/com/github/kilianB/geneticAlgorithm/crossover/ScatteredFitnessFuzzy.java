package com.github.kilianB.geneticAlgorithm.crossover;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.RankScaling;

/**
 * Assign the child a share of each parent's variable based on their fitness
 * 
 * <pre>
 * Variables :	{x1, x2, x3, x4, x5, x6}
 * Parents:
 *         P1:	{0.2,0.1,0.7,0.2,0.4,0.5}
 *         P2:	{0.4,0.9,0.1,0.3,0.3,0  }
 *         P3:	{0.4,  0,0.2,0.5,0.3,0.5}
 *         ------------------------------
 *         Sum:	{1,1,1,1,1,1}
 * </pre>
 * 
 * @author Kilian
 *
 */
public class ScatteredFitnessFuzzy extends CrossoverStrategyFuzzy {

	
	private FitnessScalingStrategy fitnessScaling;
	
	

	/**
	 * @param numParents
	 * @param checkClones
	 */
	public ScatteredFitnessFuzzy(int numParents) {
		super(numParents, false);
		this.fitnessScaling = new RankScaling();
	}
	
	/**
	 * @param numParents
	 * @param checkClones
	 */
	public ScatteredFitnessFuzzy(int numParents,FitnessScalingStrategy fitnessScaling) {
		super(numParents, false);
		this.fitnessScaling = fitnessScaling;
	}

	@Override
	public double[][] getCrossoverMatrix(Individual[] parents) {

		int numParents = parents.length;
		int variableCount = parents[0].getVariableCount();

		double[][] matrix = new double[numParents][variableCount];

		//Using a factor of 1 will normalize the scale values to 1 just as we need.
		ScaledFitness[] scaled = fitnessScaling.scaleFitness(parents,1);
		

		for (int parentIndex = 0; parentIndex < numParents; parentIndex++) {
			double fitness = scaled[parentIndex].getScaledFitness();
			for (int j = 0; j < variableCount; j++) {
				matrix[parentIndex][j] = fitness;
			}
		}
		

		// No clone checking required due to deterministic nature
		/*
		if(this.checkClones && 	this.isClone(matrix)) {
			return getCrossoverMatrix(parents);
		}*/
		
		return matrix;
	}

	@Override
	public String toString() {
		return "ScatteredFitnessFuzzy [fitnessScaling=" + fitnessScaling + "]";
	}
}
