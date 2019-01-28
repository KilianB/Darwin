package com.github.kilianB.geneticAlgorithm.crossover;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * A discrete crossover strategy defines which variable of which parent is used
 * during a crossover operations.
 * <p>
 * Discrete variables can not be subdivided in a meaningful way, therefore
 * discrete strategies will provide a int[] vector pointing to a single parent
 * which should be used as source for the trait.
 * 
 * @author Kilian
 *
 */
public abstract class CrossoverStrategyDiscrete extends CrossoverStrategy {

	/**
	 * Create a discrete crossover strategy.
	 * 
	 * @param numParents  the number of parents taking place in a crossover
	 *                    operation
	 * @param checkClones if the resulting children should check for clones. A clone
	 *                    is defined as an offspring which will be identical to one
	 *                    parent due to all variables being selected from the same
	 *                    individual.
	 *                    <p>
	 *                    Implementing strategies are encouraged to create a new
	 *                    crossover vector if a likely clone is detected
	 */
	public CrossoverStrategyDiscrete(int numParents, boolean checkClones) {
		super(numParents, checkClones);
	}

	/**
	 * Create a discrete crossover vector used to assign which gene should be taken
	 * from which parent during a crossover operation.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * [0,1,1,0,2,0]
	 * Take the first gene from parent 0,
	 * Take the second gene from parent 1
	 * Take the third gene from parent 1 
	 * ...
	 * </pre>
	 * 
	 * @param parents The parents used for this crossover operation
	 * @return the crossover vector
	 */
	public abstract int[] getCrossoverVector(Individual[] parents);

}
