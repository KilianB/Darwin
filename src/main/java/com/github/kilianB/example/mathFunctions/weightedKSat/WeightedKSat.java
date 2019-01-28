package com.github.kilianB.example.mathFunctions.weightedKSat;

import java.util.Arrays;
import java.util.function.Function;

import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.prototypes.BooleanPrototype;


public class WeightedKSat{

	
	
	
	
	

	public static void main(String[] args) {

		// Function<boolean[], Double> fitnessFunction
		// Example function

		/*
		 * SAT problems are in conjunctive normal form (
		 * 
		 * A OR B) AND (B OR C OR A) AND (X...Y)
		 * 
		 * k-Sat has exactly k literals / clause
		 * 
		 * NP-Hard for k >= 3
		 * 
		 * https://www.researchgate.net/publication/
		 * 220800490_Ant_Colony_Optimization_with_Adaptive_Fitness_Function_for_Satisfiability_Testing
		 * 
		 * 
		 * 	weighted k-Sat
		 *  n = p
		 * Sum( ci(m) * wi )  
		 *  i = 1
		 *  
		 *  ci(x) == 1 if expression is true
		 *  	  == 0 otherwise
		 *  
		 */

		
		
		//unweighted MAX-SAT
		double weightFactor[] = {1,1,1,1};
		
		int variableCounts = 4;
		
		//Hard coded clauses
		Function<boolean[],Boolean> clause0 = (x)->{
			return x[0] || !x[1] || x[3];
		};
		
		Function<boolean[],Boolean> clause1 = (x)->{
			return !x[0] || x[2] || !x[3];
		};
		
		Function<boolean[],Boolean> clause2 = (x)->{
			return  !x[1] || !x[2] || x[3];
		};
	
		
		//Dynamic clauses
	
		
		//Type erasure prevents us from creating generic arrays 
		@SuppressWarnings("unchecked")
		Function<boolean[],Boolean>[] clauses = bundleClauses(clause0,clause1,clause2);
		
//		Function<boolean[],Boolean>[] clauses = {};
		
		
		/*
		 * If clause true : 1
		 * 			 else : 0 
		 */
		Function<boolean[],Double> fitnessFunction = (x) -> {
			//The fitness function accepts multi
			double fitness = 0;
			for(int i = 0; i < clauses.length; i++) {	
				if(clauses[i].apply(x)) {
					fitness += weightFactor[i];
				}
			}
			return fitness;
		};
		
		var prototype = new BooleanPrototype(fitnessFunction,variableCounts);
	
		var ga = GeneticAlgorithm.builder().withPrototype(prototype)
			.withMaxGenerationCount(1000)
			.build();
		
		System.out.println(ga);
		
		System.out.println(ga.calculate(1));
		
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 2; j++) {
				for(int m = 0; m < 2; m++) {
					for(int n = 0; n < 2; n++) {
						boolean b[]= {i==0,j==0,m==0,n==0};
						System.out.println(Arrays.toString(b) + " " + fitnessFunction.apply(b));
						
					}
				}
			}
		}
		
		
		//brute force
		
		
	}
	
	private static Function<boolean[],Boolean>[] bundleClauses(Function<boolean[],Boolean>...functions) {
		return functions;
	}

}
