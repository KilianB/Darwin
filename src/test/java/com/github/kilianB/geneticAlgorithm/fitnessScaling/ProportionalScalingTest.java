package com.github.kilianB.geneticAlgorithm.fitnessScaling;

import static com.github.kilianB.geneticAlgorithm.IndividualTestSets.SMALL_TEST_SET_DISTINCT;
import static com.github.kilianB.geneticAlgorithm.IndividualTestSets.TEST_SET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

class ProportionalScalingTest {

	private static final FitnessScalingStrategy scaler = new ProportionalScaling();
	
	@Test
	@DisplayName("All individuals scaled")
	void testConsistentCount() {
		assertEquals(TEST_SET.length,scaler.scaleFitness(TEST_SET,1).length);
	}

	@DisplayName("Sum of scaled fitness")
	@ParameterizedTest
	@ValueSource(ints = {1,2,3,20})
	void testScaledValueSum(int count) {
		double summedFitness = Arrays.stream(scaler.scaleFitness(TEST_SET,count))
			.mapToDouble(item -> item.getScaledFitness()).sum();
		assertEquals(count,summedFitness,1e-5);
	}
	
	@Test
	@DisplayName("Sorted")
	void testDecreasinglySorted() {
		
		int parentsNeeded = 4;
		ScaledFitness[] result = scaler.scaleFitness(TEST_SET,parentsNeeded);
		
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
	void testCorrectlyScaledValues() {
		int parentsNeeded = 4;
		ScaledFitness[] result = scaler.scaleFitness(SMALL_TEST_SET_DISTINCT,parentsNeeded);

		
//		double scale = parentsNeeded/Arrays.stream(SMALL_TEST_SET_DISTINCT).mapToDouble(
//				individual -> 1/individual.getFitness()).sum();
//		
//		double expectedValue[] = new double[SMALL_TEST_SET_DISTINCT.length];
//		
		double scale = parentsNeeded / (1/0.1+ 1/0.2+ 1d/4);
		
		double expectedValues[] = {
				1/0.1 * scale,
				1/0.2 * scale,
				1d/4 * scale
		};
		
		for(int i = 0; i < result.length; i++) {
			assertEquals(expectedValues[i],result[i].getScaledFitness(),1e-6);
		}
		
	}
}
