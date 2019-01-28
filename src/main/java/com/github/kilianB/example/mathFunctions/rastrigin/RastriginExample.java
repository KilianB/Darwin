package com.github.kilianB.example.mathFunctions.rastrigin;

import java.util.function.Function;

import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.charting.ChartHelper;
import com.github.kilianB.geneticAlgorithm.crossover.ScatteredDiscrete;
import com.github.kilianB.geneticAlgorithm.prototypes.DoublePrototype;
import com.github.kilianB.geneticAlgorithm.prototypes.IndividualPrototype;
import com.github.kilianB.geneticAlgorithm.result.Result;
import com.github.kilianB.geneticAlgorithm.result.ResultListener;

/**
 * A simple sample demonstrating on how to calculate the minimum value of a 
 * math function.
 * 
 * In this case the Rastrigin function commonly used to test optimization algorithms serves as
 * an example with 2 input parameters.
 * 
 * @author Kilian
 *
 */
public class RastriginExample {

	public static void main(String[] args) {
		/*
		 * Define the function we want to find the minimum. 
		 * Rastrigin :   f(x,y) = 20 + x^2 + y^2 - 10 * (cos(2*pi*x) + cos(x*pi*y))
		 */
		Function<double[],Double> fitnessFunction = (double[] x) -> {
			return 20 + Math.pow(x[0],2) + Math.pow(x[1], 2) - 10*(Math.cos(2*Math.PI * x[0]) + Math.cos(2*Math.PI * x[1]));
		};
		
		/*
		 * Define the range in which the initial population shall be generated. The bigger the range
		 * the greater the area the algorithm will search. The optimal initial range has the potential
		 * solution situated in the middle. 
		 * 
		 * For rastrigin the solution is 0/0 lets choose an not optimal starting point as example.
		 */
		double[][] initialRange =	{
				{ 1, 5},	// initial min and max of x 
				{-5,-1}	    // initial min and max of y
		};

		//Create a prototype object
		IndividualPrototype proto = new DoublePrototype(initialRange,fitnessFunction);
		
		//Create the genetic algorithm object
		var geneticAlgorithm = GeneticAlgorithm.builder().withPrototype(proto)
				.withMaxGenerationCount(5000)
				.withTargetFitness(1e-30)
				.population()
				.withPopulationCount(20)
				.advanced()
				.withMutationProbability(0.2)
				.withForceCloneMutation(false,10)
				.withCrossoverStrategy(new ScatteredDiscrete(2))
				.migration(500)
				.withNewSubpopulation()
				.withNewSubpopulation()
				//.withMutationScalingStrategy(MutationScalingStrategy.LINEAR_GENERATION)
				.build();
		
		geneticAlgorithm.addResultListener(new ResultListener() {
			@Override
			public void intermediateResult(Result r) {
				if(r.getGenerationCount() == 1800) {
					for(Individual i : r.getGeneration(r.getGenerationCount(),0)) {
						System.out.println(i + " " + i.getBirth() + " " + i.getOrigin());
					}
				}
			}

			@Override
			public void finalResult(Result r) {
				Individual[] ind = r.getGeneration(r.getGenerationCount(),0);
//				for(Individual i : ind) {
//					System.out.println(i + " " + i.getOrigin());
//				}
				
				
			}
		});
		
		
		
		//Spawn a chart window
		String title = "Rastrigin";
		boolean displayAverage = true;
		boolean logScale = true;
		
		//ChartHelper.displayProgressPane(title,geneticAlgorithm,displayAverage,logScale);
		//ChartHelper.displayProgressPane("Rastrigin",geneticAlgorithm,false,false);
		ChartHelper.displayVarInspectionPane("Rastrigin",geneticAlgorithm);
		
		Result r = geneticAlgorithm.calculate(5);
	
		System.out.println(r.getBestResult());
	}
	
	
}
