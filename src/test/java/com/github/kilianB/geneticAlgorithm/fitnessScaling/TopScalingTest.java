package com.github.kilianB.geneticAlgorithm.fitnessScaling;

import static com.github.kilianB.geneticAlgorithm.IndividualTestSets.SMALL_TEST_SET_DISTINCT;
import static com.github.kilianB.geneticAlgorithm.IndividualTestSets.TEST_SET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

class TopScalingTest {

	private static final FitnessScalingStrategy scaler = new TopScaling();
	
	@Nested
	class Constructor{
		
		@Test
		@DisplayName("Valid constructor")
		void testValidConstructor() {
			new TopScaling(Double.MIN_VALUE);
			new TopScaling(0.5d);
			new TopScaling(1);
		}
		
		
		@Test
		@DisplayName("Illegal Argument Constructor Top Percentage Over 1")
		void testIllegalArgumentTop() {
			assertThrows(IllegalArgumentException.class,()->{new TopScaling(1.1);});
		}
		
		@Test
		@DisplayName("Illegal Argument Constructor Top Percentage Under 0")
		void testIllegalArgumentTop1() {
			assertThrows(IllegalArgumentException.class,()->{new TopScaling(-1.1);});
		}
	}
	
	@Test
	@DisplayName("All individuals scaled")
	void testConsistentCount() {
		assertEquals(TEST_SET.length,scaler.scaleFitness(TEST_SET,2).length);
	}

	@DisplayName("Sum of scaled fitness")
	@ParameterizedTest
	@ValueSource(ints = {1,2,3,20})
	void testScaledValueSum(int count) {
		double summedFitness = Arrays.stream(scaler.scaleFitness(TEST_SET,count))
			.mapToDouble(item -> item.getScaledFitness()).sum();
		assertEquals(count,summedFitness,1e-5);
	}
	
	@DisplayName("Sum of scaled fitness differnt top percentages")
	@ParameterizedTest
	@ValueSource(doubles = {0.1d,0.2d,0.3d,0.8d,0.9d,1d})
	void testScaledValueSum(double percentage) {
		double summedFitness = Arrays.stream(new TopScaling(percentage).scaleFitness(TEST_SET,2))
			.mapToDouble(item -> item.getScaledFitness()).sum();
		assertEquals(2,summedFitness,1e-5);
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
		int parentsNeeded = 1;
		double percentage = 0.4;
		ScaledFitness[] result = new TopScaling(percentage).scaleFitness(SMALL_TEST_SET_DISTINCT,parentsNeeded);
		
		int validCandidates = (int) Math.round((SMALL_TEST_SET_DISTINCT.length * percentage));
		
		double assumedFitness = parentsNeeded / validCandidates;
		
		for(int i = 0; i < SMALL_TEST_SET_DISTINCT.length;i++) {
			
			if(i < validCandidates) {
				assertEquals(assumedFitness,result[i].getScaledFitness(),1e-6);
			}else {
				assertEquals(0d,result[i].getScaledFitness(),1e-6);
			}
		}
		
	}
}
