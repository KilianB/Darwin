package com.github.kilianB.example.generateString.simple;

import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.charting.ChartHelper;
import com.github.kilianB.geneticAlgorithm.crossover.ScatteredDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.SinglePointDiscrete;
import com.github.kilianB.geneticAlgorithm.mutationScaling.LinearFitnessMutationScaling;
import com.github.kilianB.geneticAlgorithm.prototypes.IndividualPrototype;
import com.github.kilianB.geneticAlgorithm.result.Result;
import com.github.kilianB.pcg.fast.PcgRSUFast;

/**
 * @author Kilian
 *
 */
public class ConstructString {

	public static void main(String[] args) {
		
		int populationSize = 10;
		char[] needle = "Evolution is all about passing on the genome to the next generation, adapting and surviving through generation after generation.".toCharArray();
		
		IndividualPrototype prototype = ()->{
			char[] genes = new char[needle.length];
			for(int i = 0; i < needle.length; i++) {
				genes[i] = (char)(PcgRSUFast.nextInt(95)+32);
			}
			
			return new TextIndividualSimple(needle,genes);
		};
		
		var ga = GeneticAlgorithm.builder().withPrototype(prototype)
				.withTargetFitness(0)
				.population()
				.withPopulationCount(populationSize)
				.advanced()
				.withForceCloneMutation(false, 10)
				.withMutationScalingStrategy(new LinearFitnessMutationScaling())
				.withCrossoverStrategy(new SinglePointDiscrete(2))
				.migration(100)
				.withNewSubpopulation()
				.withNewSubpopulation().withCrossoverStrategy(new ScatteredDiscrete(2))
				.build();
		
		ChartHelper.displayProgressPane("Con Simple",ga,true,true);
		
		
		Result r = ga.calculate(100, Integer.MAX_VALUE, false);
		System.out.println(r.getBestResult());

	}
	
}
