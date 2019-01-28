package com.github.kilianB.geneticAlgorithm.migration.direction;

import java.util.ArrayList;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.migration.strategy.MigrationStrategy;

/**
 * The migration process determines the source and target population during a
 * migration procedure.
 * 
 * @author Kilian
 *
 */
public interface MigrationProcess {

	/**
	 * Create an array of individuals which will migrate into the given population
	 * 
	 * @param population            All individuals of the current generation
	 * @param indexTargetPopulation The sub population to compute the candidates for
	 * @param migrationCount        the number of individuals to create
	 * @param strategy              The migration strategy to use
	 * @return the selected migration individuals for this sub population.
	 */
	public Individual[] migratedIndividuals(ArrayList<Individual[]> population, int indexTargetPopulation,
			int migrationCount, MigrationStrategy strategy);

}