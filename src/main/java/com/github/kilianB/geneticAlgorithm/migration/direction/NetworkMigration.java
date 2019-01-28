package com.github.kilianB.geneticAlgorithm.migration.direction;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.migration.strategy.MigrationStrategy;

/**
 * Individuals migrate into every other population.
 * 
 * <pre>
 *  For example given 4 sub populations
 *  Sub population 3 will receive individuals from 2,1,0
 *  Sub population 2 will receive individuals from 3,1,0
 *  Sub population 1 will receive individuals from 3,2,0
 *  Sub population 0 will receive individuals from 3,2,1
 * </pre>
 * 
 * The Migration strategy is first applied to every sub population collecting
 * potential migration candidates. The resulting pseudo population again is
 * filtered by applying the same strategy to create the final population which
 * will migrate into the target population.
 * 
 * @author Kilian
 *
 */
public class NetworkMigration implements MigrationProcess {

	public Individual[] migratedIndividuals(ArrayList<Individual[]> population, int indexTargetPopulation,
			int migrationCount, MigrationStrategy strategy) {

		// migrationCount from every population

		ArrayList<Individual> migrationIndividuals = new ArrayList<Individual>();

		for (int i = 0; i < population.size(); i++) {
			if (i != indexTargetPopulation) {
				migrationIndividuals
						.addAll(Arrays.asList(strategy.getMigrationCandidates(population.get(i), migrationCount)));
			}
		}

		// Pseudo population perform the same operation on the collected
		Individual[] result = migrationIndividuals.toArray(new Individual[migrationIndividuals.size()]);
		Arrays.sort(result);
		return strategy.getMigrationCandidates(result, migrationCount);
	}

}
