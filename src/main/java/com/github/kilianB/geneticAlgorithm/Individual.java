package com.github.kilianB.geneticAlgorithm;

import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategy;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy;
import com.github.kilianB.geneticAlgorithm.rng.RngPool;

/**
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
 *
 */
public abstract class Individual implements Comparable<Individual> {

	/**
	 * A random number generator as initialized in {@link GeneticAlgorithm#RNG}.
	 */
	protected RngPool RNG = GeneticAlgorithm.RNG;

	/**
	 * Fitness value of the individual. Lazily populated. The lower the fitness
	 * value the better solution the individual provides. Fitness is a read only
	 * variable and does not change once computed
	 */
	private double fitness = Double.MIN_VALUE;

	/**
	 * The generation the individual was created at
	 */
	private int age;

	/**
	 * How the individual was created
	 */
	private Origin origin;

	/**
	 * @return the number of variables the individual possesses which can be altered
	 *         by mutation and crossover. If arrays are to be modified each element
	 *         in the array shall be counted separately.
	 * 
	 */
	public abstract int getVariableCount();

	/**
	 * Create a new offspring using gene recombination from multiple individuals.
	 * The crossover operation either picks random genes from multiple parents
	 * (discrete) or picks parts of each gene and calculates a new values by
	 * averaging these (fuzzy) before passing on the new state to an offspring.
	 * 
	 * @param crossoverStrategy The strategy applied to select which genes from
	 *                          which parent are selected.
	 *                          <p>
	 *                          In some rare cases it crossover operations may
	 *                          choose to ignore the crossoverStrategy and apply
	 *                          domain knowledge to increase the quality of the
	 *                          children. If this is the case
	 *                          {@link #CrossoverStrategy} has no effect. If this is
	 *                          suitable the fact should be prominently documented.
	 * 
	 * @param crossoverParent   The parents participating in the crossover process.
	 *                          While no guarantee is made the parents shall be
	 *                          ordered randomly with the first instance being the
	 *                          object this method was invoked on (this).
	 * 
	 * @return a new Individual with genes composed from the parents.
	 * @throws UnsuppoertedOperationException if strategy is not fuzzy or discrete
	 *                                        and method is not overwritten
	 */
	public Individual crossover(CrossoverStrategy crossoverStrategy, Individual... crossoverParent) {
		// Not really the greatest OOP design...
		if (crossoverStrategy instanceof CrossoverStrategyFuzzy) {
			return crossover((CrossoverStrategyFuzzy) crossoverStrategy, crossoverParent);
		} else if (crossoverStrategy instanceof CrossoverStrategyDiscrete) {
			return crossover((CrossoverStrategyDiscrete) crossoverStrategy, crossoverParent);
		} else {
			throw new UnsupportedOperationException("Unknown crossover strategy now suppoerted by individual");
		}
	}

	/**
	 * Create a new offspring using gene recombination from multiple individuals.
	 * The crossover operation picks fractional parts of each gene from multiple
	 * parents and calculates a new values by aggregating these before passing on
	 * the new state to an offspring.
	 * <p>
	 * 
	 * Fuzzy crossover operations may or may not be supported by a given individual.
	 * If an individual is not able to perform a fuzzy crossover a
	 * {@link java.lang.UnsupportedOperationException} shall be thrown.
	 * 
	 * @apiNote A consideration to remove the abstract modifier of this method and
	 *          throw a {@link java.lang.UnsupportedOperationException} by default
	 *          was dismissed due to the important nature of implementing this
	 *          crossover type if applicable. Fuzzy crossover has proven to have
	 *          such a great impact that dismissing this feature should be a
	 *          conscious decision.
	 * 
	 * @param crossoverStrategy The strategy applied to map the source of each gene
	 *                          of the offspring to a parent.
	 *                          <p>
	 *                          In some rare cases the crossover operations may
	 *                          choose to ignore the crossoverStrategy and apply
	 *                          domain knowledge to increase the quality of the
	 *                          children. If this is the case the supplied
	 *                          {@link #CrossoverStrategy} has no effect. If this is
	 *                          suitable the fact should be prominently documented.
	 * 
	 * @param crossoverParent   The parents participating in the crossover process.
	 *                          While no guarantee is made the parents shall be
	 *                          ordered randomly with the first instance being the
	 *                          object this method was invoked on (this).
	 * 
	 * @return a new Individual with genes composed from the parents.
	 * @throws UnsupportedOperationException if operation is not supported
	 */
	public abstract Individual crossover(CrossoverStrategyFuzzy crossoverStrategy, Individual... crossoverParent);

	/**
	 * Create a new offspring using gene recombination from multiple individuals.
	 * The crossover operation either picks random genes from multiple parents
	 * (discrete) or picks parts of each gene and calculates a new values by
	 * averaging these (fuzzy) before passing on the new state to an offspring.
	 * 
	 * Discrete crossover operations may or may not be supported by a given
	 * individual. If an individual is not able to perform a fuzzy crossover a
	 * {@link java.lang.UnsupportedOperationException} shall be thrown.
	 * 
	 * @param crossoverStrategy The strategy applied to select which genes from
	 *                          which parent are selected.
	 *                          <p>
	 *                          In some rare cases it crossover operations may
	 *                          choose to ignore the crossoverStrategy and apply
	 *                          domain knowledge to increase the quality of the
	 *                          children. If this is the case
	 *                          {@link #CrossoverStrategy} has no effect. If this is
	 *                          suitable the fact should be prominently documented.
	 * 
	 * @param crossoverParent   The parents participating in the crossover process.
	 *                          While no guarantee is made the parents shall be
	 *                          ordered randomly with the first instance being the
	 *                          object this method was invoked on (this).
	 * 
	 * @return a new Individual with genes composed from the parents.
	 * @throws UnsuppoertedOperationException if method was not overwritten by child
	 */
	public abstract Individual crossover(CrossoverStrategyDiscrete crossoverStrategy, Individual... crossoverParent);

	/**
	 * Mutate the individual by changing genes slightly.
	 * 
	 * @param probability The probability of a mutation supplied by the genetic
	 *                    algorithm. The probability is supplied during algorithm
	 *                    creation and may be increased during clone prevention to
	 *                    force a distinct population. In the range of [0 - 1]
	 * @param scaleFactor May be used to control the amount of change introduced by
	 *                    a mutation operation. By default the scale factor
	 *                    decreases throughout generations causing less and less
	 *                    drastic changes as time goes on.
	 *                    <p>
	 *                    Initially the scale factor starts at one and decreases per
	 *                    generation down to a value of 0 if max target generation
	 *                    is reached. Range[0 - 1].
	 *                    <p>
	 *                    Mathworks suggests to update a value equal to an initial
	 *                    range * gaus with std of scale factor.
	 * 
	 * @return a newly constructed Individual containing mutated genes of the parent
	 * 
	 * @see https://se.mathworks.com/help/gads/vary-mutation-and-crossover.html#f14372
	 */
	public abstract Individual mutate(double probability, double scaleFactor);

	/**
	 * Fitness function: The fitness function represents how good the current
	 * individual is based on the overall goal. The lower the fitness the better. A
	 * value of 0 represents the optimal solution.
	 * <p>
	 * 
	 * Due to the immutable nature of individuals this method will only be invoked
	 * exactly once and the value will be cached.
	 * 
	 * @return the fitness value of the current individual
	 */
	protected abstract double calculateFitness();

	/**
	 * Fitness function: The fitness function represents how good the current
	 * individual is based on the overall goal. The lower the fitness the better. A
	 * value of 0 represents the optimal solution.
	 * <p>
	 * 
	 * @return the fitness value of the current individual
	 */
	public double getFitness() {
		if (fitness == Double.MIN_VALUE) {
			fitness = calculateFitness();
		}
		return fitness;
	}

	/**
	 * Sets the birth generation of the individual.
	 * 
	 * @param age the birth generation
	 */
	public void setBirth(int age) {
		this.age = age;
	}

	/**
	 * Get the age of the individual. The age points to the generation this
	 * individual was created at.
	 * 
	 * @return the birth generation of the individual
	 */
	public int getBirth() {
		return (age);
	}

	/**
	 * Set the origin of the individual. The origin described due to which operation
	 * an individual was created.
	 * 
	 * @param origin The new origin of the individual
	 */
	public void setOrigin(Origin origin) {
		this.origin = origin;
	}

	/**
	 * Get the origin of the individual. The origin describes how the individual was
	 * created.
	 * 
	 * @return the origin of the individual.
	 */
	public Origin getOrigin() {
		return origin;
	}

	/**
	 * Return the internal state of the individual. The function may return fields
	 * which need to be altered during crossover or mutation and should provide an
	 * entry for as many variables as defined in {@link #getVariableCount()}
	 * 
	 * <p>
	 * <b>Partially optional:</p>
	 * <p>
	 * 
	 * The charting package requires a correct implementation of this method, but
	 * the genetic algorithm itself does not rely on this function, therefore it is
	 * up to the implementation to decide if this method shall be overwritten in a
	 * useful manner or not.
	 * 
	 * @param index of the returned value
	 * @return an internal value of the individual
	 */
	public abstract <T> T getValue(int index);

	/**
	 * Compares the fitness of two individuals.
	 * <p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(Individual i2) {
		double f2 = i2.getFitness();
		return getFitness() > f2 ? 1 : getFitness() == f2 ? 0 : -1;
	}

	/**
	 * <b>Individuals MUST override equals and hashCode to contain all fields used
	 * during fitness calculation</b>
	 * <p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public abstract int hashCode();

	/**
	 * <b>Individuals MUST override equals and hashCode to contain all fields used
	 * during fitness calculation</b>
	 * <p>
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public abstract boolean equals(Object obj);

	@Override
	public String toString() {
		return "Individual [fitness=" + getFitness() + ", age=" + getBirth() + " ,origin=" + origin + "]";
	}

	/**
	 * Prepare the individual to be saved to a csv file. Each index of the array
	 * will be saved as a new token in the file.
	 * 
	 * <code>str[0] Delimiter str[1] Delimiter ... line break </code>
	 * 
	 * @return Individual tokens which will be concatenated in a csv
	 */
	public String[] toCSV() {
		String[] csv = new String[getVariableCount()+2];
		csv[0] = Integer.toString(this.getBirth());
		csv[1] = Double.toString(this.getFitness());
		for(int i = 2; i < csv.length; i++) {
			csv[i] = this.getValue(i-2);
		}
		return csv;
	}

	/**
	 * The origin of this individual mainly used for statistical evaluation further
	 * down the road.
	 * 
	 * @author Kilian
	 *
	 */
	public enum Origin {
		/**
		 * Present in the initial population. Either user supplied or generated by the
		 * prototype function.
		 */
		INITIAL_POPULATION,
		/**
		 * Individual created by crossover
		 */
		CROSSOVER,
		/**
		 * Individual created by mutation
		 */
		MUTATION,
		/**
		 * NOT Implemented yet
		 */
		MIGRATION_INITIAL,
		/**
		 * NOT Implemented yet
		 */
		MIGRATION_CROSSOVER,
		/**
		 * NOT Implemented yet
		 */
		MIGRATION_MUTATION,
		/**
		 * Individual was created by force clone mutation
		 */
		FORCE_CLONE_MUTATION
	}
}
