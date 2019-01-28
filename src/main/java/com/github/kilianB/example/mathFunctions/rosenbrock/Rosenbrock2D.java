package com.github.kilianB.example.mathFunctions.rosenbrock;

import java.util.function.Function;

import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.crossover.ScatteredFuzzy;
import com.github.kilianB.geneticAlgorithm.prototypes.DoublePrototype;
import com.github.kilianB.geneticAlgorithm.prototypes.IntPrototype;
import com.github.kilianB.geneticAlgorithm.result.Result;

public class Rosenbrock2D {

	// rosenbrock function has 2 additional variables
	static int a = 1;	// if a != 0 the function is not symmetric
	static int b = 100;

	
	private static void doubleDomain() {

		// (a -x)^2 + b(y-x^2)^2
		Function<double[], Double> rosenbrock = (x) -> {
			return Math.pow((a - x[0]), 2) + b * Math.pow((x[1] - Math.pow(x[0], 2)), 2);
		};

		double[][] initialRange = new double[][] { 
			{ -4, 8 }, 		// initial min/max of x
			{ -4, 3 } };	// initial min/max of y

		
		var ga = GeneticAlgorithm.builder()
				.withPrototype(new DoublePrototype(initialRange, rosenbrock))
				.withMaxGenerationCount(1000).population()
				.withPopulationCount(40).advanced()
				.withCrossoverStrategy(new ScatteredFuzzy(4))	//Using the fuzzy approach with 4 parents is much much better than with 2 
				.build();

		Result result = ga.calculate(100);

		System.out.println(result);
	}

	private static void intDomain() {

		// (a -x)^2 + b(y-x^2)^2
		//The fitness function stays the same for the whole number domain. The only change being 
		//that our input values are limited to integer values
		Function<int[], Double> rosenbrock = (x) -> {
			return Math.pow((a - x[0]), 2) + b * Math.pow((x[1] - Math.pow(x[0], 2)), 2);
		};

		int[][] initialRange = new int[][] { 
			{ -10, 10 }, 
			{ -10, 10 } };

		var ga = GeneticAlgorithm.builder().withPrototype(new IntPrototype(initialRange, rosenbrock))
				.withMaxGenerationCount(1000).build();

		Result result = ga.calculate(100);
		
		System.out.println(result);

	}

	public static void main(String[] args) {

		doubleDomain();

		intDomain();

	}

}
