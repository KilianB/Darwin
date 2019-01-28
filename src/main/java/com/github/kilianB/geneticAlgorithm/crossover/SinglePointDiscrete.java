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
 * 
 *<pre>
 * Variables :	{x1, x2, x3, x4, x5, x6}
 * Parents:
 *         P1:	{A , B , C , D , E , F}
 *         P2:	{1 , 2 , 3 , 4 , 5 , 6}
 *         P3:	{a , b , c , d , e , f}
 *         ------------------------------
 *         new:	{1 , 2 , 3 , d , E , F}
 * </pre>
 *  
 * @author Kilian
 *
 */

public class SinglePointDiscrete extends CrossoverStrategyDiscrete{

	/**
	 * @param numParents  The number of parents used for each crossover
	 */
	public SinglePointDiscrete(int numParents) {
		this(numParents,true);
	}
	
	/**
	 * @param numParents  The number of parents used for each crossover
	 * @param checkClones Prevent vectors from being created which only 
	 * 	contain a single parent's genes
	 */
	public SinglePointDiscrete(int numParents,boolean checkClones) {
		super(numParents,checkClones);
		this.checkClones = checkClones;
	}
	
	@Override
	public int[] getCrossoverVector(Individual[] parents) {
		int numParents = parents.length;
		int variableCount = parents[0].getVariableCount();
		
		int[] matrix = new int[variableCount];
		
		//---------cut-------------cut--------cut-------------
		double share[] = new double[numParents];
		double sum = Double.MIN_VALUE;
		for(int i = 0; i < numParents; i++) {
			share[i] = RNG.nextDouble();
			sum += share[i];
		}
		
		int cutLocation[] = new  int[numParents];
		
		int sumVars = 0;
		for(int i = 0; i < numParents; i++){
			share[i] = sum / share[i];
			cutLocation[i] = (int) Math.round(variableCount/share[i]);
			sumVars += cutLocation[i];
		}
		
		//FIX 7.10.2018 due to rounding errors the last index might be too small to account for all variables
		//TODO this intorduces bias to the last object
		while(sumVars < variableCount) {
			cutLocation[numParents-1]++;
			sumVars++;
		}
		//Due to rounding the last parent's gene might be skipped even though it is entitled 
		//to it. since the individuals are randomized it does not matter.
		
		int curIndex = cutLocation[0]-1; 
		int curParent = 0;
		
		for(int i = 0; i < variableCount; i++) {
			
			while(curIndex < i) {
				curParent++;
				curIndex += cutLocation[curParent];
			}
			matrix[i] = curParent;
		}
		
		
		if(checkClones && isClone(matrix)) {
			/* 
			 * Make sure that we don't have a clone of one of the parents
			 * if we only have a very small set of variables go ahead and modify it manually
			 */
			if(variableCount <= 3) {
				//At this point we have a vector with just the same parent
				//Chose a parent we haven't used to far
				int parent = matrix[0];
				if(parent == 0) {
					parent = RNG.nextInt(numParents-1)+1;
				}else if(parent == numParents -1) {
					parent = RNG.nextInt(numParents-1);
				}else {
					parent += RNG.nextBoolean() ? -1: 1;
				}
				
				//We don't want 0 index to keep at least 1 entry!
				int forcedCut = RNG.nextInt(variableCount-1) +1;
				
				for(int i = forcedCut; i < variableCount; i++) {	
					matrix[i] = parent;
				}
			}else {
				return getCrossoverVector(parents);
			}
		}
		
		//System.out.println(Arrays.toString(matrix));
		
		return matrix;
	}

	@Override
	public String toString() {
		return "SinglePointDiscrete [checkClones=" + checkClones + "]";
	}
}
