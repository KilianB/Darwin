package com.github.kilianB.example.mathFunctions.weightedKSat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.function.Function;

import com.github.kilianB.StringUtil;
import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.prototypes.BooleanPrototype;
import com.github.kilianB.geneticAlgorithm.result.Result;
import com.github.kilianB.geneticAlgorithm.result.Result.TerminationReason;
import com.github.kilianB.pcg.fast.PcgRSFast;

/**
 * A dynamic version of the {@link WeightedKSat} automatically generating
 * clauses
 * 
 * @author Kilian
 *
 */
public class WeigthedKSatDynamic {

	/*
	 * Generate RNG Provider with a seed to get the same formula every time for testing.
	 * The GA uses it's own unseeded instance therefore results from execution will vary.
	 */

	private static final Random RNG = new PcgRSFast();
	
	public static void main(String[] args) {

		//This ga doesn't perform any 
		
		int k = 3;				//k Number of variables per clause
		int numClauses;			//M	Number of clauses 		// 50 
		int maxVars = 15;		//N	Number of variables		// 15
		
		boolean verbose = false;
		
	
		//transition point for 3 sets //4.25

		HashMap<Double,Pair<Integer,Integer>> results = new LinkedHashMap<>();
		
		int stopAtTwoFails = 0;
		
		double totalRuntime = 0;
		
		for(int i = 1; ; i++) {
			int solvable = 0;
			int unsolvable = 0;
			numClauses = i;
			double solvability = numClauses/(double)maxVars;
			
			for(int j = 0; j < 600; j++) {
				var fitnessFunction = createFitnessFunction(k,numClauses,maxVars,verbose);
				
				var prototype = new BooleanPrototype(fitnessFunction, maxVars);

				var ga = GeneticAlgorithm.builder().withPrototype(prototype).withMaxGenerationCount(1000)
						.withMaxStaleGenerations(100)
						.population()
						.withPopulationCount(20)
						.advanced()
						
						//.withCrossoverFraction(0.5f)
						.build();

				Result r = ga.calculate(1,Integer.MAX_VALUE,false);
				
				totalRuntime += (r.getExecutionTime()/1000d);
				
				if(r.getTerminationReason().equals(TerminationReason.Fitness)) {
					solvable++;
				}else {
					unsolvable++;
				}
				
				//Performance no sub pop  pop count 20.
				
			}
			
			//TODO dificulty graph! by counting max generations
				
			results.put(solvability,new Pair<Integer,Integer>(solvable,unsolvable));
			System.out.println(i + " " + "Solvability: " + String.format("%.2f",solvability) + " Solvable: " + solvable + " Unsolvable: " + unsolvable);
			if(solvable == 0 && (++stopAtTwoFails == 2)) {
				break;
			}
		}
		
		
		System.out.println("Total runtime: " + totalRuntime);
		
		//Export to file
//		File outputFile = new File("WeightedKSat"+k+".csv");
//		
//		try(BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))){
//			String delim = ",";
//			//Write header
//			bw.write("k"+delim+"solvability"+delim+"solvable"+delim+"unsolvable"+delim+"ratio" + System.lineSeparator());
//			
//			StringBuilder sb = new StringBuilder();
//			
//			for(var entry : results.entrySet()) {
//				
//				int solvable = entry.getValue().s;
//				int unsolvable = entry.getValue().t;
//				
//				int sum = solvable+unsolvable;
//				
//				double ratio = unsolvable / (double)sum;;
//				
//				sb.append(k).append(delim)
//					.append(entry.getKey()).append(delim)
//					.append(entry.getValue().s).append(delim)
//					.append(entry.getValue().t).append(delim)
//					.append(ratio).append(System.lineSeparator());
//				
//				bw.write(sb.toString());
//				//clear
//				sb.setLength(0);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		
	}

	private static Function<boolean[], Double> createFitnessFunction(int k, int numClauses, int maxVars, boolean verbose) {
		//1. Build the claueses. He clause is an integer array pointing to the variable used in the equation
		// A negative number indicates the negation of the variable.
		int clauses[][] = new int[numClauses][k];
		
		List<Integer> validVars = new ArrayList<Integer>(maxVars);
		for(int i = 0; i < maxVars; i++) {
			//Start at 1 - maxVars
			validVars.add(i+1);
		}
		
		for (int i = 0; i < numClauses; i++) {
			
			//Make sure we have a "valid clause" i.e. not X and -X at the same time
			Collections.shuffle(validVars, RNG);
			Stack<Integer> s = new Stack<Integer>();
			s.addAll(validVars);
			
			for (int j = 0; j < k; j++) {
				//Start at 1 or else we can't multiply by negative one
				clauses[i][j] = s.pop() * (RNG.nextBoolean() ? 1 : -1);
			}
		}

		// Print it
		if(verbose)
			printFormula(clauses);

		double[] weightFactor = new double[numClauses];

		for (int i = 0; i < numClauses; i++) {
			weightFactor[i] = 1;//Math.random();
		}

		if(verbose)
			printFormulaModular(clauses, weightFactor, maxVars);

		return (x) -> {
			// The fitness function accepts multi
			double fitness = 0;
			for (int i = 0; i < clauses.length; i++) {

				boolean clauseResult = false;

				for (var literal : clauses[i]) {

					boolean literalResult;
					if (literal < 0) {
						literalResult = !x[-(literal+1)];

					} else {
						literalResult = x[(literal-1)];
					}

					if (literalResult) {
						clauseResult = true;
						// No need to further evaluate if one literal is true
						break;
					}
				}

				if (!clauseResult) {
					fitness += weightFactor[i];
				}
			}
			return fitness;
		};
	}

	private static void printFormulaModular(int clauses[][], double weightFactor[], int maxVars) {

		// Build format
		StringBuilder sb = new StringBuilder();

		int charsNeeded = StringUtil.charsNeeded(maxVars) + 2;

		for (int i = 0; i < clauses.length; i++) {
			sb.append("Weight: %3.2f : [");

			for (int j = 0; j < clauses[i].length; j++) {
				sb.append("%" + charsNeeded + "s");
				if (j < clauses[i].length - 1)
					sb.append(" OR ");
			}
			sb.append("]%n");

			Object[] o = new Object[clauses[i].length + 1];
			o[0] = weightFactor[i];
			// System.arraycopy(clauses[i], 0, o, 1, clauses[i].length);
			for (int m = 0; m < clauses[i].length; m++) {
				if (clauses[i][m] < 0) {
					o[m + 1] = "!x" + -clauses[i][m];
				} else {
					o[m + 1] = "x" + clauses[i][m];
				}
			}

			System.out.printf(sb.toString(), o);
			// System.out.print(temp.replace('-', '!'));
			sb.setLength(0);

		}
	}

	private static void printFormula(int clauses[][]) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < clauses.length; i++) {
			sb.append("(");
			for (int j = 0; j < clauses[i].length; j++) {

				if (clauses[i][j] < 0) {
					sb.append("!").append("x").append(-clauses[i][j]);
				} else {
					sb.append("x").append(clauses[i][j]);
				}
				if (j < clauses[i].length - 1) {
					sb.append(" OR ");
				}

			}

			sb.append(") ");
			if (i < clauses.length - 1) {
				sb.append("AND ");
			}
		}
		System.out.println(sb.toString());
	}

}
