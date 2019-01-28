package com.github.kilianB.geneticAlgorithm.crossover;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * Assign the child a random share of each parent's variable
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
public class ScatteredFuzzy extends CrossoverStrategyFuzzy {

final boolean checkClones;
	
	/**
	 * A scattered fuzzy instance with clone checking enabled as default
	 * @param numParents The number of parents used for each crossover
	 */
	public ScatteredFuzzy(int numParents) {
		this(numParents,true);
	}

	/**
	 * @param numParents  The number of parents used for each crossover
	 * @param checkClones Prevent vectors from being created which only 
	 * 	contain a single parent's genes
	 */
	public ScatteredFuzzy(int numParents,boolean checkClones) {
		super(numParents,checkClones);
		this.checkClones= checkClones;
	}

	@Override
	public double[][] getCrossoverMatrix(Individual[] parents) {
		
		int numParents = parents.length;
		int variableCount = parents[0].getVariableCount();
				
		double[][] matrix = new double[numParents][variableCount];
		
		double[] avg = new double[variableCount];
	

		//Instead maybe wen can normalize each row and mulitply by parens neeeded?
		for(int i = 0; i < numParents;i++) {
			for(int j = 0; j < variableCount;j++) {
				//27% of the entire ga algorithm spends at this line
				double rDouble = RNG.nextDouble();
				matrix[i][j] = rDouble;
				avg[j] += rDouble;
			}
		}
		
		
		//Calculate average
		for(int i = 0; i < numParents;i++) {
			for(int j = 0; j < variableCount;j++) {
				matrix[i][j] /= avg[j];
			}
		}
		
		//Cheap check to see that we didn't just get a matrix returning one parent
		if(checkClones && isClone(matrix)) {
			/* 
			 * Make sure that we don't have a clone of one of the parents
			 * Instead of brute forcing we could just pick 
			 * one element and alter it. But this will only guarantee minimal diversity
			 */
			//TODO implement manual modification
			return getCrossoverMatrix(parents);
		}
	
		return matrix;
	}

	@Override
	public String toString() {
		return "ScatteredFuzzy [checkClones=" + checkClones + "]";
	}
	
}
