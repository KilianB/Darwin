package com.github.kilianB.geneticAlgorithm.crossover;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.kilianB.ArrayUtil;
import com.github.kilianB.example.imageRaster.DummyIndividual;
import com.github.kilianB.geneticAlgorithm.Individual;

class SinglePointDiscreteTest {

	
	@Test 
	void getParentCount() {
		assertEquals(2, new SinglePointDiscrete(2,true).getParentCount());
	}
	
	@Test
	@DisplayName("Correct Structure")
	void testStructure() {
		
		Individual[] parents = {
				new DummyIndividual(0.4,2),
				new DummyIndividual(0.5,2)
		};
		SinglePointDiscrete strategy = new SinglePointDiscrete(2);
		
		assertEquals(2,strategy.getCrossoverVector(parents).length);
	}
	
	
	@Disabled
	@ParameterizedTest
	@ValueSource(ints= {2,5,10})
	void testSinglePoint(int numParents) {
		
		Individual[] parents = new Individual[numParents];
		ArrayUtil.fillArray(parents,(i)->{return new DummyIndividual(i);});
		SinglePointDiscrete strategy = new SinglePointDiscrete(3);
		int[] crossoverVector = strategy.getCrossoverVector(parents);
		
	}
	
	
	
	@ParameterizedTest
	@ValueSource(ints= {2,5,10})
	//Check indexoutofboundsexception for more than 2 parents
	void noException(int parents) {
		Individual[] parentSet = {
				new DummyIndividual(0.4,2),
				new DummyIndividual(0.5,2)
		};
		SinglePointDiscrete strategy = new SinglePointDiscrete(parents);
		assertEquals(2,strategy.getCrossoverVector(parentSet).length);
	}
	

}
