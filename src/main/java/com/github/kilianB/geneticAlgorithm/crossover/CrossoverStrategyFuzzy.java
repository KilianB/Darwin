package com.github.kilianB.geneticAlgorithm.crossover;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * A fuzzy crossover strategy defines which variable of which parent is used
 * during a crossover operations.
 * <p>
 * Fuzzy variables can be subdivided in a meaningful way (numeric), therefore
 * fuzzy strategies will assign percentages for each variable for each parent
 * indicating how much of the variable from each parent should be used to
 * compute the gene of the offspring.
 * 
 * @author Kilian
 *
 */
public abstract class CrossoverStrategyFuzzy extends CrossoverStrategy {

	public CrossoverStrategyFuzzy(int numParents, boolean checkClones) {
		super(numParents, checkClones);
	}

	/**
	 * Create a fuzzy crossover matrix used to assign which gene should be taken
	 * from which parent during a crossover operation.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * Parent 0 [0.3,0.0]
	 * Parent 1 [0.4,0.5]
	 * Parent 2 [0.2,0.5]
	 * 
	 * The offspring's first gene should consists of 30 % from parent 0 40% from parent 1 and 20% from parent 2
	 * The offspring's second gene should consists of 50% from parent 1 and 50% from parent 2
	 * 
	 * matrix[parentIndex][geneIndex]
	 * </pre>
	 * 
	 * @param parents The parents used for this crossover operation
	 * @return the crossover matrix
	 */
	public abstract double[][] getCrossoverMatrix(Individual[] parents);

	/**
	 * Converts a fuzzy matrix to a discrete crossover vector by determining the
	 * highest value found in the matrix.
	 * 
	 * 
	 * P1 [0,1,3,4,5] P2 [1,4,2,1,0] -------------- V: [0 1 0 0 0]
	 * 
	 * 
	 * @param fuzzy
	 * @return
	 */
	public static int[] fuzzyToDiscrete(double[][] fuzzy) {

		int[] vector = new int[fuzzy[0].length];

		// Slow due to cache misses
		for (int variable = 0; variable < vector.length; variable++) {

			int maxIndex = -1;
			double maxValue = 0;
			for (int parentCount = 0; parentCount < fuzzy.length; parentCount++) {
				double value = fuzzy[parentCount][variable];
				if (value > maxValue) {
					maxValue = value;
					maxIndex = parentCount;
				}
				vector[variable] = maxIndex;
			}
		}
		return vector;
	}

}
