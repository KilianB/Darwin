package com.github.kilianB.geneticAlgorithm.crossover;

/**
 * Crossover strategies define which variable of which parent is used during a
 * crossover operations.
 * 
 * <p>
 * The give the individual a hint how a crossover operation should take place.
 * 
 * @author Kilian
 *
 */
public abstract class CrossoverStrategy {

	protected boolean checkClones;
	protected int numParents;

	/**
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
	public CrossoverStrategy(int numParents, boolean checkClones) {
		this.numParents = numParents;
		this.checkClones = checkClones;
	}

	/**
	 * @return the number of parents taking place in a crossover operation.
	 */
	public int getParentCount() {
		return numParents;
	}

	/**
	 * Check if the input vector will create a clone of one of the parents
	 * 
	 * @param input the crossover vector
	 * @return true if all variables are taken from a single individual, false
	 *         otherwise
	 */
	protected boolean isClone(int[] input) {
		if(input.length == 0) {
			return true;
		}
		
		int needle = input[0];
		int count = input.length;

		for (int i = 1; i < count; i++) {
			if (needle != input[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Check if the input matrix will create a clone of one of the parents.
	 * 
	 * @param input the crossover matrix
	 * @return true if all variables are taken from a single individual, false
	 *         otherwise
	 */
	protected boolean isClone(double[][] input) {

		if(input.length == 0) {
			return true;
		}
		
		int count = input[0].length;
		for (int i = 0; i < count; i++) {
			if (input[0][i] < 0.99 && input[0][i] > 0.01)
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (checkClones ? 1231 : 1237);
		result = prime * result + numParents;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CrossoverStrategy other = (CrossoverStrategy) obj;
		if (checkClones != other.checkClones)
			return false;
		if (numParents != other.numParents)
			return false;
		return true;
	}
}
