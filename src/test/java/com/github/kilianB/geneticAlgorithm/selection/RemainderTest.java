package com.github.kilianB.geneticAlgorithm.selection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.kilianB.example.imageRaster.DummyIndividual;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

class RemainderTest {

	private static final ScaledFitness[] SCALED_FITNESS_TESTSET_60 = new ScaledFitness[] {
			new ScaledFitness(18.3, new DummyIndividual(0.3)), 
			new ScaledFitness(11.7, new DummyIndividual(0.4)),
			new ScaledFitness(9.4, new DummyIndividual(0.5)), 
			new ScaledFitness(8.6, new DummyIndividual(0.6)),
			new ScaledFitness(4.2, new DummyIndividual(0.7)), 
			new ScaledFitness(3.8, new DummyIndividual(0.8)),
			new ScaledFitness(1.4, new DummyIndividual(0.9)), 
			new ScaledFitness(1.2, new DummyIndividual(1)),
			new ScaledFitness(1, new DummyIndividual(1.1)), 
			new ScaledFitness(0.4, new DummyIndividual(1.3))
			};
	
	@Test
	void nonNull() {
		Remainder r = new Remainder();
		int parentsNeeded = 60;
		
		Individual[] selectedParents = r.selectParents(SCALED_FITNESS_TESTSET_60, parentsNeeded);
		assertFalse(Arrays.asList(selectedParents).contains(null));
	}
	
	@Test
	void testIntegerPart() {
		
		Remainder r = new Remainder();
		/*
		 * Remainder returns at least int part individuals of the same kind.
		 */
		int parentsNeeded = 60;
		Individual[] selectedParents = r.selectParents(SCALED_FITNESS_TESTSET_60, parentsNeeded);
		
		Map<Individual, Long> count = Arrays.stream(selectedParents).collect(
				Collectors.groupingBy(
							Function.identity(),
							Collectors.counting()
				)
		);

		for(var individual : SCALED_FITNESS_TESTSET_60) {		
			int integerPart = (int)individual.getScaledFitness();	
			//If int part < 0 it might not appear in the set
			if(integerPart > 0 && count.get(individual.getIndividual()) < integerPart) {
				fail("Individual did not appear often enough");
			}
		}
		
	}

	@Test
	@Disabled
	void testRemainderPart() {
		//This is tournament selection. JUnit is not good at testing random distributions.
		fail("Not yet implemented");
	}
	
}
