package com.github.kilianB.geneticAlgorithm.selection;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.kilianB.example.imageRaster.DummyIndividual;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

class TournamentTest {

	private static final ScaledFitness[] SCALED_FITNESS_TESTSET = new ScaledFitness[] {
			new ScaledFitness(5.5, new DummyIndividual(0.3)), new ScaledFitness(5, new DummyIndividual(0.4)),
			new ScaledFitness(4.5, new DummyIndividual(0.5)), new ScaledFitness(4, new DummyIndividual(0.6)),
			new ScaledFitness(3, new DummyIndividual(0.7)), new ScaledFitness(3, new DummyIndividual(0.8)),
			new ScaledFitness(2, new DummyIndividual(0.9)), new ScaledFitness(2.2, new DummyIndividual(1)),
			new ScaledFitness(1, new DummyIndividual(1.1)), };

	@Test
	@DisplayName("Constructor Invalid")
	void testTournament() {

		assertAll("Invalid Tournement Size", () -> {
			assertThrows(IllegalArgumentException.class, () -> {
				new Tournament(1);
			});
		}, () -> {
			assertThrows(IllegalArgumentException.class, () -> {
				new Tournament(-1);
			});
		});
	}

	@Test
	@DisplayName("Constructor Valid")
	void validObject() {
		assertNotNull(new Tournament(2));
	}

	@Test
	@DisplayName("Select best candidate")
	void testSelectParents() {

		Tournament t = new Tournament(SCALED_FITNESS_TESTSET.length);

		Individual[] expectedResults = new Individual[] { SCALED_FITNESS_TESTSET[0].getIndividual(),
				SCALED_FITNESS_TESTSET[0].getIndividual() };

		assertArrayEquals(expectedResults, t.selectParents(SCALED_FITNESS_TESTSET, 2));
	}

	@ParameterizedTest
	@ValueSource(ints = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 })
	@DisplayName("Correct tournament size")
	void testSelectParentsInternalTournament(int tournamentSize) {

		Tournament t = new Tournament(tournamentSize);

		if (tournamentSize <= SCALED_FITNESS_TESTSET.length) {
			assertEquals(tournamentSize, t.selectParentsUnitTest(SCALED_FITNESS_TESTSET).length - 1);
		} else {
			assertEquals(SCALED_FITNESS_TESTSET.length, t.selectParentsUnitTest(SCALED_FITNESS_TESTSET).length - 1);
		}

	}
	
	@RepeatedTest(4)
	@DisplayName("Select best candidate Unit")
	void testSelectParentsInternal() {

		Tournament t = new Tournament(4);

		// The last returned value is our best candidate
		ScaledFitness[] returnValues = t.selectParentsUnitTest(SCALED_FITNESS_TESTSET);

		ScaledFitness bestCandidate = returnValues[returnValues.length - 1];
		double bestScaledFitness = bestCandidate.getScaledFitness();

		for (int i = 0; i < returnValues.length - 1; i++) {
			if (returnValues[i].getScaledFitness() > bestScaledFitness) {
				fail("Better candidate found in tournament");
			}
		}
	}
}
