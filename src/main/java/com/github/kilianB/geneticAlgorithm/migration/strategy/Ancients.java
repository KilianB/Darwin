package com.github.kilianB.geneticAlgorithm.migration.strategy;

import java.util.PriorityQueue;

import com.github.kilianB.geneticAlgorithm.Individual;

public class Ancients extends MigrationStrategyBase{

	public Ancients(int migrationCount) {
		super(migrationCount);
	}

	@Override
	public Individual[] getMigrationCandidates(Individual[] currentPopulation, int migrationCount) {
		
		migrationCount = Math.min(migrationCount, currentPopulation.length);
		
		Individual[] result = new Individual[migrationCount];
		
		PriorityQueue<Individual> prio = new PriorityQueue<>(migrationCount,(i1,i2)-> {
			//Invert. older is better
			return -Integer.compare(i1.getBirth(), i2.getBirth());
		});
		
		for(Individual i : currentPopulation) {
			prio.add(i);
		}
		
		
		for(int i = 0; i < migrationCount; i++) {
			result[i] = prio.remove();
		}
		
		//TODO test
		
		return currentPopulation;
		
	}


}
