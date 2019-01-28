package com.github.kilianB.geneticAlgorithm.migration.strategy;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * Elitism is a migration strategy slecting the best n individuals based on
 * their fitness value.
 * 
 * @author Kilian
 *
 */
public class Elitism extends MigrationStrategyBase {

	public Elitism(int migrationCount) {
		super(migrationCount);
	}

	@Override
	public Individual[] getMigrationCandidates(Individual[] currentPopulation, int migrationCount) {
		migrationCount = Math.min(migrationCount, currentPopulation.length);
		Individual[] result = new Individual[migrationCount];
		System.arraycopy(currentPopulation, 0, result, 0, migrationCount);
		return result;
	}

}
