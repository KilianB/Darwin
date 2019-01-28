package com.github.kilianB.geneticAlgorithm;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy;

/**
 * An individual base implementation getting rid of all optional methods wich
 * are not absolutely necessary for the genetic algorithm to work.
 * 
 * <p>
 * Some methods like
 * {@link com.github.kilianB.geneticAlgorithm.result.Result#toFile(java.io.File, java.io.File)
 * Result#toFile} as well as
 * {@link com.github.kilianB.geneticAlgorithm.charting.ChartHelper#createVariableInspectionPane
 * ChartHelper#createVariableInspectionPane} will not work.
 * 
 * <p>
 * Each individual represents a solution to the fitness function and is one of
 * the core concepts employed in genetic algorithms. Individuals are able to
 * pass on <i>parts</i> of their genes to offsprings in form of reproduction and
 * mutation.
 * 
 * <p>
 * In order to guarantee thread safety and allowing one individual to be the
 * parent of multiple children individuals are immutable and must not change
 * state once it was created.
 * 
 * @apiNote Starting with version 0.0.4 generic type parameters were remove to
 *          allow for easy array creation in {@link GeneticAlgorithm}. This
 *          leaves the ga open to "accidentally" mix different individual types
 *          which usually does not yield useful results. Generics shall be re
 *          implemented at a later stage
 * 
 * @author Kilian
 */
public abstract class SimpleIndividual extends Individual {

	protected boolean factorScaleIntoMutationProbability = true;
	protected Object values;

	public SimpleIndividual(Object currentGenes) {
		this.values = currentGenes;
	}

	@Override
	public Individual crossover(CrossoverStrategyDiscrete crossoverStrategy, Individual... crossoverParent) {
		Object[] newValues = new Object[this.getVariableCount()];
		int crossoverVector[] = crossoverStrategy.getCrossoverVector(crossoverParent);

		for (int i = 0; i < newValues.length; i++) {
			newValues[i] = crossoverParent[crossoverVector[i]].getValue(i);
		}
		return createIndividual(values);
	}

	// Reflection is probably to slow?
	protected abstract Individual createIndividual(Object newGenes);

	@Override
	public Individual crossover(CrossoverStrategyFuzzy crossoverStrategy, Individual... crossoverParent) {
		throw new UnsupportedOperationException("Individual does not support fuzzy crossover");
	}

	@Override
	public Individual mutate(double probability, double scaleFactor) {
		Object[] newValues = new Object[this.getVariableCount()];

		Random rng = RNG.getUnderlayingRNG();

		RNG.nextBoolean();
		
		if (factorScaleIntoMutationProbability) {
			probability *= scaleFactor;
		}

		for (int i = 0; i < newValues.length; i++) {

			if (rng.nextDouble() < probability) {
				newValues[i] = mutateValue(i, getValue(i), scaleFactor);
			} else {
				// Simply copy it
				newValues[i] = getValue(i);
			}
		}
		return createIndividual(newValues);
	}

	/**
	 * @param i
	 * @param object
	 * @param scaleFactor
	 * @return
	 */
	protected abstract Object mutateValue(int index, Object oldGene, double scaleFactor);

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(int index) {
		return (T) java.lang.reflect.Array.get(values,index);
	}
	

	@Override
	public String toString() {
		int length = Array.getLength(values);
		StringBuilder sb = new StringBuilder("SimpleIndividual[ ");
		sb.append("fitness=").append(this.getFitness()).append(", age=").append(this.getBirth()).append(", genes=[");
		for(int i = 0; i < length; i++) {
			sb.append(getValue(i).toString());
			if(i != length-1) {
				sb.append(",");
			}
		}
		sb.append("]]");
		return sb.toString();
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (factorScaleIntoMutationProbability ? 1231 : 1237);
		result = prime * result + Arrays.deepHashCode(new Object[] {values});
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
		SimpleIndividual other = (SimpleIndividual) obj;
		if (factorScaleIntoMutationProbability != other.factorScaleIntoMutationProbability)
			return false;
		if (values == null) {
			return false;
		} else if (!Arrays.deepEquals(new Object[] {values},new Object[] {other.values}))
			return false;
		return true;
	}


	

}
