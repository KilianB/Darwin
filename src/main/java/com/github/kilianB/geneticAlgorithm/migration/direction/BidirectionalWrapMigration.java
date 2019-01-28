package com.github.kilianB.geneticAlgorithm.migration.direction;

import java.util.ArrayList;
import java.util.Arrays;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.migration.strategy.MigrationStrategy;

/**
 * Individuals will migrate into the next higher and lower population
 * 
 * <pre>
 *  For example given 4 sub populations
 *  Sub population 3 will receive individuals from 2 and 0.
 *  Sub population 2 will receive individuals from 3 and 1
 *  Sub population 1 will receive individuals from 2 and 0
 *  Sub population 0 will receive individuals from 3 and 1
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
public class BidirectionalWrapMigration implements MigrationProcess {

	@Override
	public Individual[] migratedIndividuals(ArrayList<Individual[]> population, int indexTargetPopulation,
			int migrationCount, MigrationStrategy strategy) {

		if (indexTargetPopulation == population.size() - 1) {
			return new Individual[0];
		}

		ArrayList<Individual> migrationIndividuals = new ArrayList<Individual>();

		int leftIndex = indexTargetPopulation - 1;
		int rightIndex = indexTargetPopulation + 1;

		if (leftIndex < 0) {
			leftIndex = population.size() - 1;
		}

		if (rightIndex > population.size() - 1) {
			leftIndex = 0;
		}

		migrationIndividuals
				.addAll(Arrays.asList(strategy.getMigrationCandidates(population.get(leftIndex), migrationCount)));
		migrationIndividuals
				.addAll(Arrays.asList(strategy.getMigrationCandidates(population.get(rightIndex), migrationCount)));

		// Pseudo population perform the same operation on the collected
		Individual[] result = migrationIndividuals.toArray(new Individual[migrationIndividuals.size()]);
		Arrays.sort(result);
		return strategy.getMigrationCandidates(result, migrationCount);

	}
}
