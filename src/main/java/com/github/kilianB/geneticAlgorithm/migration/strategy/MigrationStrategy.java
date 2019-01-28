package com.github.kilianB.geneticAlgorithm.migration.strategy;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * A migration strategy defines which individuals of each sub population are
 * considered to migrate to the next population.
 *
 * A migration allows individuals from one sub population to move to the next
 * sub population during defined intervals.
 * 
 * 
 * @author Kilian
 *
 */
public interface MigrationStrategy {

	/**
	 * Create an array containing possible migration candidates for this population.
	 * Migration candidates from (multiple) sub populations will be compared based
	 * on their fitness and replace the worst n candidates of the target population.
	 * 
	 * @param currentPopulation the current population sorted by fitness
	 * @param migrationCount    the number of candidates to create
	 * @return the potential migration candidates
	 */
	public Individual[] getMigrationCandidates(Individual[] currentPopulation, int migrationCount);

	/**
	 * Return the number of individuals that will migrate from a population to
	 * another.
	 * 
	 * @return the number of individuals selected from this population.
	 */
	public int getMigrationCount();

}
