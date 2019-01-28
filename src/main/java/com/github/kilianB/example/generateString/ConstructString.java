package com.github.kilianB.example.generateString;

import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.charting.ChartHelper;
import com.github.kilianB.geneticAlgorithm.crossover.ScatteredDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.SinglePointDiscrete;
import com.github.kilianB.geneticAlgorithm.mutationScaling.LinearFitnessMutationScaling;
import com.github.kilianB.geneticAlgorithm.result.Result;

public class ConstructString {

	public static void main(String[] args) {
		System.out.println(performAlgo());
	}
	

	private static int performAlgo() {
		int populationSize = 500;
		String needle = "Evolution is all about passing on the genome to the next generation, adapting and surviving through generation after generation.";

		var initialPopulation = new Individual[populationSize];

		for (int i = 0; i < populationSize; i++) {
			initialPopulation[i] = TextIndividual.createRandomIndividual(needle);
		}

		var ga = GeneticAlgorithm.builder().withInitialPopulation(initialPopulation)
				.withTargetFitness(0)
				.population()
				.advanced()
				.withForceCloneMutation(true, 10)
				.withMutationScalingStrategy(new LinearFitnessMutationScaling())
				.withCrossoverStrategy(new SinglePointDiscrete(2))
				.migration(100)
				.withNewSubpopulation()
				.withNewSubpopulation()
				.withCrossoverStrategy(new ScatteredDiscrete(2))
				.build();

		//Will crash with that great population size. Try lower it to 10 individuals.
		//ChartHelper.displayProgressPane("Con Extended",ga,true,true);
		
		Result r = ga.calculate(5, Integer.MAX_VALUE, true);
		System.out.println(r.getBestResult());
		return r.getGenerationCount();
	}

}
