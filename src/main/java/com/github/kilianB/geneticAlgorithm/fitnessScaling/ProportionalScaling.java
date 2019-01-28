package com.github.kilianB.geneticAlgorithm.fitnessScaling;

import java.util.HashSet;
import java.util.Set;

import com.github.kilianB.geneticAlgorithm.Individual;

public class ProportionalScaling implements FitnessScalingStrategy{
	
	@Override
	public ScaledFitness[] scaleFitness(Individual[] population, int parentsNeeded) {
		
		ScaledFitness[] scaledFitness = new ScaledFitness[population.length];
		
		//FIX 13.10 if the algorithm already has a fitness of 10 (e.g. during initial creation
		//It is not cought by the stop criteria and we have to prevent a division by 0 exception
		Set<Individual> zeroCandidates = new HashSet<>();
		
		//Iteration is faster than streams
		double fittnessSum = 0;
		for(var individual : population) {
		
			double fitness = individual.getFitness();
			
			if(fitness == 0.0) {
				zeroCandidates.add(individual);
			}
			fittnessSum += 1/fitness;
		}
		
		if(zeroCandidates.size() == 0 && Double.isInfinite(fittnessSum) || Double.isNaN(fittnessSum)) {
			throw new ArithmeticException("Fitness Sum Overflow. Proportional Scaling does not handle"
					+ "huge fitness values well. Try to go with annother approach");
		}
		
		
		if(zeroCandidates.isEmpty()) {
			double scaleFactor = parentsNeeded/fittnessSum;
			for(int i = 0; i < population.length; i++) {	
				scaledFitness[i] = new ScaledFitness(scaleFactor* 1/population[i].getFitness(),population[i]);
			}
		}else {
			int zeroCount = zeroCandidates.size();
			for(int i = 0; i < population.length; i++) {	
				if(zeroCandidates.contains(population[i])) {
					scaledFitness[i] = new ScaledFitness((1/zeroCount)*parentsNeeded,population[i]);
				}else {
					scaledFitness[i] = new ScaledFitness(0,population[i]);
				}
			}
		}
		
		
		return scaledFitness;
	}

	@Override
	public String toString() {
		return "ProportionalScaling ";
	}
	
	@Override
	public int hashCode() {
		return getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
	
}
