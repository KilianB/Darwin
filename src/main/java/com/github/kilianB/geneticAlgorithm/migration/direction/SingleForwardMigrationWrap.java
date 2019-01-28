package com.github.kilianB.geneticAlgorithm.migration.direction;

import java.util.ArrayList;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.migration.strategy.MigrationStrategy;

/**
 * Individuals will migrate to the next higher populations. Wraps around at the end
 *
 * <pre>
 *  For example.
 *  Sub population 3 will receive individuals from 2
 *  Sub population 2 will receive individuals from 1
 *  Sub population 1 will receive individuals from 0
 *  Sub population 0 will receive individuals from 3
 * 
 * </pre>
 * 
 * @author Kilian
 */
public class SingleForwardMigrationWrap implements MigrationProcess {

	@Override
	public Individual[] migratedIndividuals(ArrayList<Individual[]> population, int indexTargetPopulation,
			int migrationCount, MigrationStrategy strategy) {

		if (indexTargetPopulation == 0) {
			indexTargetPopulation = population.size();
		}

		return strategy.getMigrationCandidates(population.get(indexTargetPopulation - 1), migrationCount);
	}
}
