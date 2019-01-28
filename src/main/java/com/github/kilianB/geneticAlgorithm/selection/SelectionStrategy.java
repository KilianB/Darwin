package com.github.kilianB.geneticAlgorithm.selection;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;


/**
 * The selection strategy defines which parents are nominated as parents for future generations
 * based on their scaled fitness values. The same individual may be picked as parent multiple
 * times. 
 * @author Kilian
 *
 */
public interface SelectionStrategy {

	/**
	 * 
	 * Nominate individuals to be parents for the next generation based on their scaled fitness value.
	 * 
	 * @param scaledFitness A sorted array with individuals associated with a scaled fitness value
	 * @param count The count of parents that shall be selected
	 * @return
	 * 	The ordering of the array is unspecified and may or may not be sorted by the individuals
	 * 	fitness value. Be aware that when selecting parents for reproduction the individuals 
	 * 	yshould be randomly picked from the array!
	 */
	public Individual[] selectParents(ScaledFitness[] scaledFitness, int count);
}
