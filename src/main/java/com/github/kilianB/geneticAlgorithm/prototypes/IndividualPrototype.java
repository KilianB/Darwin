package com.github.kilianB.geneticAlgorithm.prototypes;

import com.github.kilianB.geneticAlgorithm.Individual;


/**
 * A protype object 
 * @author Kilian
 */
@FunctionalInterface
public interface IndividualPrototype {

	/**
	 * 
	 * @return
	 */
	public Individual createIndividual();

}
