package com.github.kilianB.geneticAlgorithm;


import com.github.kilianB.example.imageRaster.DummyIndividual;

public class IndividualTestSets {

	public static final Individual[] SMALL_TEST_SET_DISTINCT = {
			new DummyIndividual(0.1),
			new DummyIndividual(0.2),
			new DummyIndividual(4),
			};
	
	public static final Individual[] SMALL_TEST_SET_DUPLICATES = {
			new DummyIndividual(0.2),
			new DummyIndividual(0.2),
			new DummyIndividual(4),
			};
	
	public static final Individual[] TEST_SET = {
			new DummyIndividual(0.1),
			new DummyIndividual(0.2),
			new DummyIndividual(0.2),
			new DummyIndividual(0.5),
			new DummyIndividual(10),
			new DummyIndividual(12),
			new DummyIndividual(16),
			new DummyIndividual(16),
			new DummyIndividual(20),
			new DummyIndividual(100),
			new DummyIndividual(110),
	};
}
