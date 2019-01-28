package com.github.kilianB.geneticAlgorithm.migration.strategy;

/**
 * Base implementation of the {@link MigrationStrategy} interface
 * 
 * @author Kilian
 *
 */
public abstract class MigrationStrategyBase implements MigrationStrategy {

	/**
	 * The number of individuals to select as candidates
	 */
	protected int migrationCount;

	public MigrationStrategyBase(int migrationCount) {
		this.migrationCount = migrationCount;
	}

	public int getMigrationCount() {
		return migrationCount;
	}

}
