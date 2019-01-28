package com.github.kilianB.geneticAlgorithm.mutationScaling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * @author Kilian
 *
 */
class MutationScalingStrategyTest {

	@Nested
	class LinearFitness {

		MutationScalingStrategy strategy;

		@BeforeEach
		void setup() {
			strategy = new LinearFitnessMutationScaling();
		}

		@Test
		@DisplayName("0% Target")
		void nullInput() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 100;
			int targetFitness = 0;
			int currentStallGenerations = 0;
			assertEquals(1d, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(1d, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
		}

		@Test
		@DisplayName("0% Target Offset")
		void nullInputOffset() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 110;
			int targetFitness = 10;
			int currentStallGenerations = 0;
			assertEquals(1d, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(1d, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
		}

		@Test
		@DisplayName("10% Target")
		void twentyFivePvercent() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 100;
			int targetFitness = 0;
			int currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(.9, strategy.computeScaleFactor(curGen, maxGen, 90, targetFitness, currentStallGenerations));
		}

		@Test
		@DisplayName("10% Target Offset")
		void twentyFivePvercentOffset() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 110;
			int targetFitness = 10;
			int currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(.9, strategy.computeScaleFactor(curGen, maxGen, 100, targetFitness, currentStallGenerations));
		}

		@Test
		@DisplayName("50% Target")
		void fiftyPercent() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 100;
			int targetFitness = 0;
			int currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(.5, strategy.computeScaleFactor(curGen, maxGen, 50, targetFitness, currentStallGenerations));
		}

		@Test
		@DisplayName("50% Target Offset")
		void fiftyPercentOffset() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 110;
			int targetFitness = 10;
			int currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(.5, strategy.computeScaleFactor(curGen, maxGen, (curBestFitenss + targetFitness) / 2d,
					targetFitness, currentStallGenerations));
		}

		@Test
		@DisplayName("100% Target")
		void houndredPercent() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 100;
			int targetFitness = 0;
			int currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(0,
					strategy.computeScaleFactor(curGen, maxGen, targetFitness, targetFitness, currentStallGenerations));
		}

		@Test
		@DisplayName("100% Target Offset")
		void houndredPercentOffset() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 110;
			int targetFitness = 10;
			int currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(0,
					strategy.computeScaleFactor(curGen, maxGen, targetFitness, targetFitness, currentStallGenerations));
		}

		@Test
		void reset() {

			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 110;
			int targetFitness = 10;
			int currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(0,
					strategy.computeScaleFactor(curGen, maxGen, targetFitness, targetFitness, currentStallGenerations));

			strategy.reset();

			curGen = 0;
			maxGen = 0;
			curBestFitenss = 200;
			targetFitness = 10;
			currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(.5, strategy.computeScaleFactor(curGen, maxGen, (curBestFitenss + targetFitness) / 2d,
					targetFitness, currentStallGenerations));
			assertEquals(0,
					strategy.computeScaleFactor(curGen, maxGen, targetFitness, targetFitness, currentStallGenerations));
		}

	}

	@Nested
	class LinearGeneration {

		MutationScalingStrategy strategy;

		@BeforeEach
		void setup() {
			strategy = MutationScalingStrategy.LINEAR_GENERATION;
		}

		@Test
		@DisplayName("0% Target")
		void nullInput() {
			int curGen = 0;
			int maxGen = 100;
			int curBestFitenss = 0;
			int targetFitness = 0;
			int currentStallGenerations = 0;
			assertEquals(1d, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
		}

		@Test
		@DisplayName("10% Target")
		void twentyFivePvercent() {
			int curGen = 10;
			int maxGen = 100;
			int curBestFitenss = 0;
			int targetFitness = 0;
			int currentStallGenerations = 0;
			assertEquals(.9, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
		}

		@Test
		@DisplayName("50% Target")
		void fiftyPercent() {
			int curGen = 50;
			int maxGen = 100;
			int curBestFitenss = 0;
			int targetFitness = 0;
			int currentStallGenerations = 0;
			assertEquals(.5, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
		}

		@Test
		@DisplayName("100% Target")
		void houndredPercent() {
			int curGen = 100;
			int maxGen = 100;
			int curBestFitenss = 0;
			int targetFitness = 0;
			int currentStallGenerations = 0;
			assertEquals(0, strategy.computeScaleFactor(curGen, maxGen, 0, targetFitness, currentStallGenerations));
		}
	}

	@Nested
	class Constant {

		MutationScalingStrategy strategy = MutationScalingStrategy.CONSTANT;

		@Test
		void nullInput() {
			assertEquals(1d, strategy.computeScaleFactor(0, 0, 0, 0, 0));
		}

		@Test
		void randomInput() {
			assertEquals(1d, strategy.computeScaleFactor(1, 2, 3, 4, 5));
		}

	}

	@Nested
	class RichardFitness{
		MutationScalingStrategy strategy;

		@BeforeEach
		void setup() {
			strategy = new RichardFitnessMutationScaling();
		}

		@Test
		@DisplayName("0% Target")
		void nullInput() {
			int curGen = 0;
			int maxGen = 100;
			int curBestFitenss = 100;
			int targetFitness = 0;
			int currentStallGenerations = 0;
			assertEquals(1d, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations),1e-4);
			assertEquals(1d, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations),1e-4);
		}

		@Test
		@DisplayName("0% Target Offset")
		void nullInputOffset() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 110;
			int targetFitness = 10;
			int currentStallGenerations = 0;
			assertEquals(1d, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations),1e-4);
			assertEquals(1d, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations),1e-4);
		}

		@Test
		@DisplayName("100% Target")
		void houndredPercent() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 100;
			int targetFitness = 0;
			int currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations),1e-4);
			assertEquals(0,
					strategy.computeScaleFactor(curGen, maxGen, targetFitness, targetFitness, currentStallGenerations),1e-4);
		}

		@Test
		@DisplayName("100% Target Offset")
		void houndredPercentOffset() {
			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 110;
			int targetFitness = 10;
			int currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations),1e-4);
			assertEquals(0,
					strategy.computeScaleFactor(curGen, maxGen, targetFitness, targetFitness, currentStallGenerations),1e-4);
		}

		@Test
		void reset() {

			int curGen = 0;
			int maxGen = 0;
			int curBestFitenss = 110;
			int targetFitness = 10;
			int currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(0,
					strategy.computeScaleFactor(curGen, maxGen, targetFitness, targetFitness, currentStallGenerations));

			strategy.reset();

			curGen = 0;
			maxGen = 0;
			curBestFitenss = 200;
			targetFitness = 10;
			currentStallGenerations = 0;
			assertEquals(1, strategy.computeScaleFactor(curGen, maxGen, curBestFitenss, targetFitness,
					currentStallGenerations));
			assertEquals(.5, strategy.computeScaleFactor(curGen, maxGen, (curBestFitenss + targetFitness) / 2d,
					targetFitness, currentStallGenerations));
			assertEquals(0,
					strategy.computeScaleFactor(curGen, maxGen, targetFitness, targetFitness, currentStallGenerations));
		}
	}
	
}
