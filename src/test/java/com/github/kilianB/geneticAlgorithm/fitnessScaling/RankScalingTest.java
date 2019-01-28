package com.github.kilianB.geneticAlgorithm.fitnessScaling;

import static com.github.kilianB.geneticAlgorithm.IndividualTestSets.SMALL_TEST_SET_DISTINCT;
import static com.github.kilianB.geneticAlgorithm.IndividualTestSets.SMALL_TEST_SET_DUPLICATES;
import static com.github.kilianB.geneticAlgorithm.IndividualTestSets.TEST_SET;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

class RankScalingTest {

	private static final FitnessScalingStrategy scaler = new RankScaling();
	
	
	@Test
	@DisplayName("Same Fitness -> Same Scaling")
	void sameFitnessSameScaling() {
		ScaledFitness[] scaledFitness = scaler.scaleFitness(SMALL_TEST_SET_DUPLICATES, 2);
		assertEquals(scaledFitness[0].getScaledFitness(),scaledFitness[1].getScaledFitness());
	}
	
	@Test
	@DisplayName("Different Fitness -> Different Scaling")
	void uneqalFitnessUneqalScaling() {
		ScaledFitness[] scaledFitness = scaler.scaleFitness(SMALL_TEST_SET_DUPLICATES, 2);
		assertNotEquals(scaledFitness[0].getScaledFitness(),scaledFitness[2].getScaledFitness());
	}
	
	
	@Test
	@DisplayName("All individuals scaled")
	void testConsistentCount() {
		assertEquals(TEST_SET.length,new RankScaling().scaleFitness(TEST_SET,1).length);
	}

	@DisplayName("Sum of scaled fitness")
	@ParameterizedTest
	@ValueSource(ints = {1,2,3,20})
	void testScaledValueSum(int count) {
		double summedFitness = Arrays.stream(new RankScaling().scaleFitness(TEST_SET,count))
			.mapToDouble(item -> item.getScaledFitness()).sum();
		assertEquals(count,summedFitness,1e-5);
	}
	
	@Test
	@DisplayName("Sorted")
	void testDecreasinglySorted() {
		
		int parentsNeeded = 4;
		ScaledFitness[] result = new RankScaling().scaleFitness(TEST_SET,parentsNeeded);
		
		double lastScaledFitness = Double.MAX_VALUE;
		double lastFitness = Double.MIN_VALUE;
		for(var item: result) {
			
			if(item.getIndividual().getFitness() < lastFitness) {
				fail("Not correctly sorted. Fitness of individual is better than fitness of last individual");
			}
			
			lastFitness = item.getIndividual().getFitness();
			
			if(item.getScaledFitness() > lastScaledFitness) {
				fail("Not correctly sorted. Scaled fitness  is better than fitness of last individual");
			}
			
			lastFitness = item.getIndividual().getFitness();
			lastScaledFitness = item.getScaledFitness();
		}
	}
	
	@Test
	void testCorrectlyScaledValuesDistinct() {
				
		int parentsNeeded = 2;
		
		double scale = (1/Math.sqrt(1) + 1/Math.sqrt(2) + 1/Math.sqrt(3))/parentsNeeded;
		
		double[] expectedScaledValue = {
				(1/Math.sqrt(1))/scale,
				(1/Math.sqrt(2))/scale,
				(1/Math.sqrt(3))/scale
		};
		
		ScaledFitness[] result = new RankScaling().scaleFitness(SMALL_TEST_SET_DISTINCT,parentsNeeded);
		double[] scaledFitness = Arrays.stream(result).mapToDouble(item -> item.getScaledFitness()).toArray();
		assertArrayEquals(expectedScaledValue, scaledFitness, 1e-6);
	}
	
	@Test
	void testCorrectlyScaledValuesDuplicates() {

		//the first 2 entries are duplicates
		int parentsNeeded = 2;
		
		double scale = (1/Math.sqrt(1) + 1/Math.sqrt(1) + 1/Math.sqrt(3))/parentsNeeded;
		
		double[] expectedScaledValue = {
				(1/Math.sqrt(1))/scale,
				(1/Math.sqrt(1))/scale,
				(1/Math.sqrt(3))/scale
		};
		
		ScaledFitness[] result = new RankScaling().scaleFitness(SMALL_TEST_SET_DUPLICATES,parentsNeeded);
		double[] scaledFitness = Arrays.stream(result).mapToDouble(item -> item.getScaledFitness()).toArray();
		assertArrayEquals(expectedScaledValue, scaledFitness, 1e-6);
	}
	
	
}
