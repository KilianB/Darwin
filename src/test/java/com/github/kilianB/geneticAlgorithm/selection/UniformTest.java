package com.github.kilianB.geneticAlgorithm.selection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.kilianB.example.imageRaster.DummyIndividual;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

class UniformTest {

	@Test
	void testUniformDistirbution() {
		ScaledFitness[] population = new ScaledFitness[] {
				new ScaledFitness(1,new DummyIndividual(0.1)),
				new ScaledFitness(0.5d,new DummyIndividual(10)),
				new ScaledFitness(0.3d,new DummyIndividual(100))
		};
		
		int parentsNeeded = (int) 1e6;
		
		Individual[] selectedParents = new Uniform().selectParents(population, parentsNeeded);
		
		var map = Arrays.stream(selectedParents).collect(Collectors.groupingBy(
			item -> item, Collectors.counting()	
		));
		
		//Are the results within 3 percent?
		
		for(var entry : map.entrySet()) {
			assertEquals(0.3d,entry.getValue()/ (double)parentsNeeded,0.05);
		}
	
	
	}
	

}
