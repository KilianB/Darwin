package com.github.kilianB.geneticAlgorithm.fitnessScaling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.BiFunction;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * 
 * @author Kilian
 *
 */
public class AgeScaling implements FitnessScalingStrategy {

	FitnessScalingStrategy scaler;
	BiFunction<Integer, Double, Double> customAgeScaler;

	public AgeScaling() {
		this.scaler = new RankScaling();
	}

	public AgeScaling(BiFunction<Integer, Double, Double> scalingFunction) {
		customAgeScaler = scalingFunction;
	}

	public AgeScaling(FitnessScalingStrategy initialScaler) {
		this.scaler = initialScaler;
	}

	@Override
	public ScaledFitness[] scaleFitness(Individual[] population, int parentsNeeded) {

		ScaledFitness[] scaledFitness = scaler.scaleFitness(population, parentsNeeded);

		double newScaledFitness[] = new double[scaledFitness.length];

		double sumOfFitness = 0;
		
		if (customAgeScaler != null) {
			// Do some user stuff
			for (int i = 0; i < scaledFitness.length; i++) {
				newScaledFitness[i] = customAgeScaler.apply(scaledFitness[i].getIndividual().getBirth(),
						scaledFitness[i].getScaledFitness());
				sumOfFitness += newScaledFitness[i];
			}
		}else {
			
			//Find the age range of all individuals
//			int minAge = Integer.MAX_VALUE;
//			int maxAge = Integer.MIN_VALUE;
//			
//			for(var s : scaledFitness) {
//				int age = s.getIndividual().getAge();
//				if(minAge < age) {
//					minAge = age;
//				}
//				if(maxAge > age) {
//					maxAge = age;
//				}
			
			//Lets simply use the sqrt. due to normalization at the end initial scaling
			//might not even matter
			
			//Scale the individuals age 
			
			
			//We can't simply scale age like this or old individuals will never ever be considered. Thats
			//Why a simple sqrt will lead to elite parent's not contributing to repopulation at all.
			//Go for the analog rank scaling
		
			//TODO SLOW!
			ArrayList<Individual> sortedByAge =  new ArrayList<>(Arrays.asList(population));
		
			sortedByAge.sort((ind0, ind1)->{
				return Integer.compare(ind0.getBirth(), ind1.getBirth());
			});
			
			
			
			
			for (int i = 0; i < scaledFitness.length; i++) {
//				newScaledFitness[i] = (Math.sqrt(scaledFitness[i].getIndividual().getAge())) 
//						* scaledFitness[i].getScaledFitness();
//				
				Individual ind = scaledFitness[i].getIndividual();
				//0 index is the oldest
				
				//Give the newest one a higher weight
				int ageRank = (sortedByAge.size() - Collections.binarySearch(sortedByAge, ind)+1);
				
				
				
				newScaledFitness[i] =  scaledFitness[i].getScaledFitness()* 1/Math.sqrt(ageRank);
				
				sumOfFitness += newScaledFitness[i];
			}
		}
		
		//Normalize back to the the sum of parentsNeeded
		double scaleFactor = parentsNeeded / sumOfFitness;
		
		var result = new ScaledFitness[scaledFitness.length];
		for (int i = 0; i < scaledFitness.length; i++) {
			result[i] = new ScaledFitness(newScaledFitness[i] * scaleFactor,scaledFitness[i].getIndividual());
		}
		
		return result;
	}

	//TODO bi function doesn't redeclare hashcode or equals
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((customAgeScaler == null) ? 0 : customAgeScaler.hashCode());
		result = prime * result + ((scaler == null) ? 0 : scaler.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgeScaling other = (AgeScaling) obj;
		if (customAgeScaler == null) {
			if (other.customAgeScaler != null)
				return false;
		} else if (!customAgeScaler.equals(other.customAgeScaler))
			return false;
		if (scaler == null) {
			if (other.scaler != null)
				return false;
		} else if (!scaler.equals(other.scaler))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AgeScaling [scaler=" + scaler + ", customAgeScaler=" + customAgeScaler + "]";
	}
	
	

}
