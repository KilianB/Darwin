package com.github.kilianB.geneticAlgorithm.crossover;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import com.github.kilianB.geneticAlgorithm.Individual;


/**
 * Assign the child the variable of a random parent
 * 
 * <pre>
 * Variables :	{x1, x2, x3, x4, x5, x6}
 * Parents:
 *         P1:	{A , B , C , D , E , F}
 *         P2:	{1 , 2 , 3 , 4 , 5 , 6}
 *         P3:	{a , b , c , d , e , f}
 *         ------------------------------
 *         new:	{1 , B , C , 4 , e , f}
 * </pre>
 * 
 * 
 * @author Kilian
 * 
 */
public class ScatteredDiscrete extends CrossoverStrategyDiscrete {

	/**
	 * A scattered discrete instance with clone checking enabled as default
	 * @param numParents The number of parents used for each crossover
	 */
	public ScatteredDiscrete(int numParents) {
		this(numParents,true);
	}
	
	/**
	 * @param numParents  The number of parents used for each crossover
	 * @param checkClones Prevent vectors from being created which only 
	 * 	contain a single parent's genes
	 */
	public ScatteredDiscrete(int numParents, boolean checkClones) {
		super(numParents,checkClones);
	}

	@Override
	public int[] getCrossoverVector(Individual[] parents) {
		
		int numParents = parents.length;
		int variableCount = parents[0].getVariableCount();
		
		int[]  matrix = new int[variableCount];
		
		for(int i = 0; i < variableCount; i++) {
			matrix[i] = RNG.nextInt(numParents);
		}
		
		/*
		 *  Make sure that we don't have a clone of one of the parents
		 */
		if(checkClones && isClone(matrix)) {
			/* 
			 * If we only have a very small set of variables go ahead and modify it manually
			 */
			if(variableCount < 4) {
				
				//At this point we have a vector with just the same parent
				//Pick a random entry and modify it
				int index = RNG.nextInt(variableCount);
				int value = matrix[index];
				if(value == 0) {
					value = RNG.nextInt(numParents-1)+1;
				}else if(value == numParents -1) {
					value = RNG.nextInt(numParents-1);
				}else {
					value += RNG.nextBoolean() ? 1 : -1;
				}
				matrix[index] = value;
			}else {
				//Brute force
				return getCrossoverVector(parents);
			}
		}
		return matrix;
	}
	
	

	@Override
	public String toString() {
		return "ScatteredDiscrete [checkClones=" + checkClones + "]";
	}
	
	

	

}
