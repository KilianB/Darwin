package com.github.kilianB.example.mathFunctions.rosenbrock;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.crossover.ScatteredFuzzy;
import com.github.kilianB.geneticAlgorithm.prototypes.DoublePrototype;
import com.github.kilianB.geneticAlgorithm.prototypes.IntPrototype;
import com.github.kilianB.geneticAlgorithm.result.Result;
import com.github.kilianB.pcg.fast.PcgRSFast;

public class RosenbrockNDimensional {

	// rosenbrock function has 2 additional variables
	static int a = 1;	// if a != 0 the function is not symmetric
	static int b = 100;

	
	private static void doubleDomain() {

		/*
		 * N-1
		 * SUM	[b(x{i+1} - x{i}^2)^2 + (a-x{i})^2]
		 * i=1
		 */
		
		Function<double[], Double> rosenbrock = (x) -> {
			double value = 0;
			for(int i = 0; i < x.length-1; i++) {
				value += (b * Math.pow((x[i+1] - Math.pow(x[i],2)),2) + Math.pow((a-x[i]),2));
			}
			return value;
		};

		
		//The size of initial range array specifies how many dimension out problem has
		double[][] initialRange2D = new double[][] { 
			{ -4, 8 }, 		// initial min/max of x0
			{ -4, 3 }};		// initial min/max of x1

		double[][] initialRange3D = new double[][] { 
			{ -4, 8 }, 		// initial min/max of x0
			{ -4, 3 },		// initial min/max of x1
			{ -2, 4 }};	// initial min/max of x2
				
			double[][] initialRange4D = new double[][] { 
			{ -4, 8 }, 		// initial min/max of x0
			{ -4, 3 },		// initial min/max of x1
			{ -6, 6 },		// initial min/max of x2
			{  -4, 4 }};		// initial min/max of x3
						
				
		int dimensions = 15;
		
		double[][] intitialRangeND = generateInitialRange(dimensions);

		System.out.println(Arrays.deepToString(intitialRangeND));
		
		var ga = GeneticAlgorithm.builder()
				.withPrototype(new DoublePrototype(intitialRangeND, rosenbrock))
				.withMaxExecutionTime(10,TimeUnit.SECONDS)
				.withMaxGenerationCount(500_000)
				/*
				.population()
				.withPopulationCount(25)
				.advanced()
				.withCrossoverStrategy(new ScatteredFuzzy(2))
				.withForceCloneMutation(false,0)
				.migration()
				.withMigrationInterval(100)
				.withNewSubpopulations(Runtime.getRuntime().availableProcessors())
				*/
				.build();
					
//		Result result = ga.calculate(10000);
//
//		System.out.println(result);
	
		
		int processors = Runtime.getRuntime().availableProcessors();
		
		
		int totalPopulationCount = 500;
		
		for(int i = 1; i <= processors*3; i++) {
			
			int populationSize = (int)(totalPopulationCount/(double)i);
			
			var ga1 = GeneticAlgorithm.builder()
					.withPrototype(new DoublePrototype(intitialRangeND, rosenbrock))
					.withMaxExecutionTime(25,TimeUnit.SECONDS)
					.withMaxGenerationCount(500_000)
					.withTargetFitness(0)
					.population()
					.withPopulationCount(populationSize)
					.advanced()
					.withCrossoverStrategy(new ScatteredFuzzy(2))
					.withForceCloneMutation(false,0)
					.migration(100)
					.withNewSubpopulations(i)
					.build();
			Result r = ga1.calculate(0);
			
			System.out.println("Sub population: " + i + " Generations: " + r.getGenerationCount() + " Population " + populationSize + " Effective Pop " + (populationSize * i) + " Effective Individuals " +  populationSize * r.getGenerationCount());
		}
		

	
	}
	
	//Concept of fatigue ..
	
	
	
	
	
	private static double[][] generateInitialRange(int dimensions){
		
		var RNG = new PcgRSFast();
		
		double[][] initialRangeND = new double[dimensions][2];
		
		//Initialize the array
		for(int dim = 0; dim < dimensions; dim++) {
			//Lets set the initial range to -7 to 7
			initialRangeND[dim][0] = RNG.nextInt(6)-7;		//Min of xdim
			initialRangeND[dim][1] = RNG.nextInt(6)+1;		//Max of xdim
		}
		return initialRangeND;
	}
	

	private static void intDomain() {

		// (a -x)^2 + b(y-x^2)^2
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

		//intDomain();

	}

}
