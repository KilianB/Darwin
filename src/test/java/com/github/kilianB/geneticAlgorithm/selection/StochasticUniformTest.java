package com.github.kilianB.geneticAlgorithm.selection;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.github.kilianB.example.imageRaster.DummyIndividual;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

class StochasticUniformTest {

	private static final ScaledFitness[] SCALED_FITNESS_TESTSET_60 = new ScaledFitness[] {
			new ScaledFitness(18.3, new DummyIndividual(0.3)), 
			new ScaledFitness(11.7, new DummyIndividual(0.4)),
			new ScaledFitness(9.4, new DummyIndividual(0.5)), 
			new ScaledFitness(8.6, new DummyIndividual(0.6)),
			new ScaledFitness(4.2, new DummyIndividual(0.7)), 
			new ScaledFitness(3.8, new DummyIndividual(0.8)),
			new ScaledFitness(1.8, new DummyIndividual(0.9)), 
			new ScaledFitness(1.2, new DummyIndividual(1)),
			new ScaledFitness(1, new DummyIndividual(1.1)), };
	
	@Test
	void nonNull() {
		StochasticUniform r = new StochasticUniform();
		Individual[] selectedParents = r.selectParents(SCALED_FITNESS_TESTSET_60, 60);
		System.out.println(Arrays.toString(selectedParents));
		assertFalse(Arrays.asList(selectedParents).contains(null));
	}
}
