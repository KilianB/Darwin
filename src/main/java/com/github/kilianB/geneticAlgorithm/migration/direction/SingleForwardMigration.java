package com.github.kilianB.geneticAlgorithm.migration.direction;

import java.util.ArrayList;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.migration.strategy.MigrationStrategy;

/**
 * Individuals will migrate to the next higher populations.
 *
 * <pre>
 *  For example.
 *  Sub population 3 will receive individuals from 2
 *  Sub population 2 will receive individuals from 1
 *  Sub population 1 will receive individuals from 0
 *  Sub population 0 will not receive any individuals
 * </pre>
 * 
 * @author Kilian
 */
public class SingleForwardMigration implements MigrationProcess {

	@Override
	public Individual[] migratedIndividuals(ArrayList<Individual[]> population, int indexTargetPopulation,
			int migrationCount, MigrationStrategy strategy) {

		if (indexTargetPopulation == 0) {
			return new Individual[0];
		}

		return strategy.getMigrationCandidates(population.get(indexTargetPopulation - 1), migrationCount);
	}
}
