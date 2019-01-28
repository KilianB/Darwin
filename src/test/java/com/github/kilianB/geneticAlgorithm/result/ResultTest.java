package com.github.kilianB.geneticAlgorithm.result;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.kilianB.example.imageRaster.DummyIndividual;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.Individual.Origin;
import com.github.kilianB.geneticAlgorithm.result.Result.TerminationReason;

/**
 * @author Kilian
 *
 */
@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
class ResultTest {

	@ParameterizedTest()
	@ValueSource(ints = { 2, 5, 10 })
	void generationStep(int gen) {
		Result r = new Result(gen);
		assertEquals(gen, r.getGenerationStep());
	}

	@Test
	void executionTime() {
		Result r = new Result(0);
		r.addGeneration(0, new ArrayList(), 1);
		r.addGeneration(1, new ArrayList(), 10);
		assertEquals(10, r.getExecutionTime());
	}

	@Test
	void executionTimeDecrease() {
		Result r = new Result(0);
		r.addGeneration(0, new ArrayList(), 2);
		assertThrows(IllegalStateException.class, () -> {
			r.addGeneration(1, new ArrayList(), 1);
		});
	}

	void availableGenerations() {
		Result r = new Result(0);
		r.addGeneration(0, new ArrayList(), 0);
		r.addGeneration(1, new ArrayList(), 0);
		r.addGeneration(5, new ArrayList(), 0);
		List<Integer> availableGens = r.getAvailableGenerations();
		assertEquals(3, availableGens.size());
		assertEquals(0, (int) availableGens.get(0));
		assertEquals(1, (int) availableGens.get(1));
		assertEquals(5, (int) availableGens.get(5));
	}

	void availableGenerationsSet() {
		Result r = new Result(0);
		r.addGeneration(0, new ArrayList(), 0);
		r.addGeneration(1, new ArrayList(), 0);
		r.addGeneration(5, new ArrayList(), 0);
		Set<Integer> availableGens = r.getAvailableGenerationsSet();
		assertEquals(3, availableGens.size());
		assertTrue(availableGens.contains(0));
		assertTrue(availableGens.contains(1));
		assertTrue(availableGens.contains(5));
	}

	@Nested
	class BestIndividual {
		@Test
		void sameGeneration() {
			Result r = new Result(1);

			Individual best = new DummyIndividual(0);
			best.setOrigin(Origin.INITIAL_POPULATION);
			Individual[] subPop0 = { best, new DummyIndividual(1), new DummyIndividual(2) };
			addGeneration(r, subPop0);
			assertEquals(best, r.getBestResult());
		}

		@Test
		void sameGenerationFirstSubPop() {
			Result r = new Result(1);

			Individual best = new DummyIndividual(0);
			best.setOrigin(Origin.INITIAL_POPULATION);
			Individual[] subPop0 = { best, new DummyIndividual(1), new DummyIndividual(2) };
			Individual[] subPop1 = { new DummyIndividual(1), new DummyIndividual(1), new DummyIndividual(2) };
			addGeneration(r, subPop0, subPop1);
			assertEquals(best, r.getBestResult());
		}

		@Test
		void sameGenerationSecondSubPop() {
			Result r = new Result(1);

			Individual best = new DummyIndividual(0);
			best.setOrigin(Origin.INITIAL_POPULATION);
			Individual[] subPop0 = { best, new DummyIndividual(1), new DummyIndividual(2) };
			Individual[] subPop1 = { new DummyIndividual(1), new DummyIndividual(1), new DummyIndividual(2) };
			addGeneration(r, subPop1, subPop0);
			assertEquals(best, r.getBestResult());
		}

		@Test
		void secondGeneration() {
			Result r = new Result(1);
			Individual best = new DummyIndividual(0);
			Individual[] subPop0 = { new DummyIndividual(1), new DummyIndividual(2), new DummyIndividual(2) };
			Individual[] subPop01 = { best, new DummyIndividual(2), new DummyIndividual(2) };
			addGeneration(r, subPop0);
			addGeneration(r, subPop01);
			assertEquals(best, r.getBestResult());
		}

		@Test
		void empty() {
			Result r = new Result(1);
			assertThrows(IllegalStateException.class, () -> {
				r.getBestResult();
			});
		}
	}

	@Nested
	class Summary {
		@Test
		void oneGeneration() {
			Result r = new Result(1);
			Individual[] subPop0 = { new DummyIndividual(0), new DummyIndividual(1), new DummyIndividual(2) };
			addGeneration(r, subPop0);
			DoubleSummaryStatistics stat = r.getSummary();

			assertAll(() -> {
				assertEquals(0, stat.getMin());
			}, () -> {
				assertEquals(1, stat.getAverage());
			}, () -> {
				assertEquals(2, stat.getMax());
			});
		}
	}

	@ParameterizedTest()
	@MethodSource("terminationReason")
	void testTerminationReason(TerminationReason t) {
		Result r = new Result(1);
		r.setTerminationReason(t);
		assertEquals(t, r.getTerminationReason());
	}

	private static void addGeneration(Result r, Individual[]... individual) {
		ArrayList<Individual[]> individuals = new ArrayList<>();
		for (Individual[] pop : individual) {
			individuals.add(pop);
		}
		r.addGeneration(r.getGenerationCount() + 1, individuals, r.getExecutionTime() + 1);
	}

	@SuppressWarnings("unused")
	private static Stream<Arguments> terminationReason() {
		return Arrays.stream(TerminationReason.values()).map(t -> Arguments.of(t));
	}
}
