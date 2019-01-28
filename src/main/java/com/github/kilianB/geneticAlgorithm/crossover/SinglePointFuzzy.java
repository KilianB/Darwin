package com.github.kilianB.geneticAlgorithm.crossover;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * Assign the value of each variables from one parent to the final solution
 * in linear fashion.<p>
 * 
 * A line is laid out and cut in n sections. Each section corresponds to 
 * a parent. The line section the variable falls will be used to determine
 * who passes on the value.
 * <pre>
 * Variables :	{x1, x2, x3, x4, x5, x6}
 * Parents:
 *         P1:	{A , B , C , D , E , F}
 *         P2:	{1 , 2 , 3 , 4 , 5 , 6}
 *         P3:	{a , b , c , d , e , f}
 *         ------------------------------
 *         new:	{1 , 2 , 3 , d , E , F}
 * </pre>
 * 
 * <b>CAUTION:</b> Single point does by definition use a discrete
 * 	approach. This implementation will distribute the elements
 * in the same way the discrete algorithm does but returns it's
 * result in a double[][] array with values clamped to 1 and 0.
 * 
 * @author Kilian
 *
 */
public class SinglePointFuzzy extends CrossoverStrategyFuzzy {

	private boolean checkClones;

	/**
	 * @param numParents  The number of parents used for each crossover
	 */
	public SinglePointFuzzy(int numParents) {
		this(numParents,true);
	}
	
	/**
	 * @param numParents  The number of parents used for each crossover
	 * @param checkClones Prevent vectors from being created which only 
	 * 	contain a single parent's genes
	 */
	public SinglePointFuzzy(int numParents, boolean checkClones) {
		super(numParents,checkClones);
	}

	@Override
	public double[][] getCrossoverMatrix(Individual[] parents) {

		int numParents = parents.length;
		int variableCount = parents[0].getVariableCount();

		double[][] matrix = new double[numParents][variableCount];

		// ---------cut-------------cut--------cut-------------
		double share[] = new double[numParents];
		double sum = Double.MIN_VALUE;
		for (int i = 0; i < numParents; i++) {
			share[i] = RNG.nextDouble();
			sum += share[i];
		}

		// 0 check? necessary?
		// + Double.MIN_VALUE;

		int cutLocation[] = new int[numParents];

		for (int i = 0; i < numParents; i++) {
			share[i] = sum / share[i];
			cutLocation[i] = (int) Math.round(variableCount / share[i]);
		}

		// Due to rounding the last parent's gene might be skipped even though it is
		// entitled
		// to it. since the individuals are randomized it does not matter.

		int curIndex = cutLocation[0] - 1;
		int curParent = 0;

		for (int i = 0; i < variableCount; i++) {

			while (curIndex < i) {
				curParent++;
				curIndex += cutLocation[curParent];
			}
			for (int j = 0; j < numParents; j++) {
				matrix[j][i] = j == curParent ? 1 : 0;
			}
		}

		if (checkClones && isClone(matrix)) {
			//TODO also implement the single point discrete optimization 
			//for little var counts for improved performance
			// if we only have 2 fields we can simply swap
			return getCrossoverMatrix(parents);
			
		}

		return matrix;

	}

	@Override
	public String toString() {
		return "SinglePointFuzzy [checkClones=" + checkClones + "]";
	}

}
