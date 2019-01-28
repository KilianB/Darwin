package com.github.kilianB.example.bootstrapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

import com.github.kilianB.MathUtil;
import com.github.kilianB.StringUtil;
import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.charting.ChartHelper;
import com.github.kilianB.geneticAlgorithm.charting.VariableDescriptor;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategy;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy;
import com.github.kilianB.geneticAlgorithm.crossover.ScatteredDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.ScatteredFitnessFuzzy;
import com.github.kilianB.geneticAlgorithm.crossover.ScatteredFuzzy;
import com.github.kilianB.geneticAlgorithm.crossover.SinglePointDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.SinglePointFuzzy;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.ProportionalScaling;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.RankScaling;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.TopScaling;
import com.github.kilianB.geneticAlgorithm.prototypes.DoublePrototype;
import com.github.kilianB.geneticAlgorithm.prototypes.IndividualPrototype;
import com.github.kilianB.geneticAlgorithm.result.Result;
import com.github.kilianB.geneticAlgorithm.selection.Remainder;
import com.github.kilianB.geneticAlgorithm.selection.Roulette;
import com.github.kilianB.geneticAlgorithm.selection.SelectionStrategy;
import com.github.kilianB.geneticAlgorithm.selection.StochasticUniform;
import com.github.kilianB.geneticAlgorithm.selection.Tournament;
import com.github.kilianB.pcg.fast.PcgRSFast;

/**
 * @author Kilian
 *
 */
public class BootstrapAlgoSettings {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new BootstrapAlgoSettings();
	}

	public BootstrapAlgoSettings() {

		// Finding the best genetic algorithm settings is a hard task.
		// Why not let us use the genetic algorithm to find the best settings for the
		// algorithm?

//		int k = 3;
//		// Transition point for k=3 -> 4.5
//		int maxVars = 15;
//		int numClauses = (int) (maxVars * 4.5);
//		double solvability = numClauses / (double) maxVars;
//		boolean verbose = false;
//
//		System.out.println(solvability);
//
////		for (int i = 0; i < 10; i++) {
////			var fitnessFunction = createFitnessFunction(k, numClauses, maxVars, verbose);
////			var prototype = new BooleanPrototype(fitnessFunction, maxVars);
////
////			Result r = GeneticAlgorithm.builder().withPrototype(prototype).withMaxStaleGenerations(100).build()
////					.calculate(10, Integer.MAX_VALUE, false);
////			System.out.println(r.getTerminationReason());
////		}
//
//		var fitnessFunction = createFitnessFunction(k, numClauses, maxVars, verbose);
//		var prototype = new BooleanPrototype(fitnessFunction, maxVars);
//		var bootStrapProto = new GeneticAglrithmPrototype(prototype,false,true,10,20);

		
		//Rastrigin
		
		
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
		
		var bootStrapProto = new GeneticAglrithmPrototype(proto,true,true,10,20);
		
		var ga= GeneticAlgorithm.builder().withPrototype(bootStrapProto)
				.withMaxGenerationCount(10)
				.withTargetFitness(0)
				.population()
				.withPopulationCount(30)
				.withCrossoverFraction(0.6f)
				.advanced()
				.withMutationProbability(0.4)
				.withForceCloneMutation(true,100)
				.migration(25)
				.withNewSubpopulations(3)
				.withNewSubpopulation()
				.withCrossoverStrategy(new SinglePointDiscrete(3))
			.build();
		
		ChartHelper.displayVarInspectionPane(ga);
		
		Result r = ga.calculate(1);
		
		GeneticAlgorithm gaBest = ((GeneticAlgorithmIndividual)r.getBestResult()).ga;
		//Just in case
		gaBest.reset();
		GeneticAlgorithm gaDefault = GeneticAlgorithm.builder()
				.withPrototype(proto)
				.withTargetFitness(1e-5)
				.advanced()
				.withCrossoverStrategy(new ScatteredFuzzy(2))
				.build();
		
		Result rFound = gaBest.calculate(0);
		Result rDefault = gaDefault.calculate(0);
		
		System.out.println("\nFound: " + rFound);
		System.out.println("Default: " + rDefault);
		
		System.out.println("Winner : " + gaBest);
		System.out.println("Default: " + gaDefault);
		
		// Winner : GeneticAlgorithm [maxGenerationCount=400, maxExecutionTime=Infinite, targetFitness=0.001, maxStaleGenerations=100, mutationProbability=[0.9664356727041786], selectionStrategy=[Remainder], scalingStrategy=[ProportionalScaling ], crossoverStrategy=[ScatteredDiscrete [checkClones=true]], populationCount=[11], eliteCount=[1], crossoverCount=[8], mutationCount=[2]]
		// Winner : GeneticAlgorithm [maxGenerationCount=400, maxExecutionTime=Infinite, targetFitness=0.001, maxStaleGenerations=100, mutationProbability=[0.547572301230002], selectionStrategy=[StochasticUniform], scalingStrategy=[RankScaling ], crossoverStrategy=[SinglePointFuzzy [checkClones=false]], populationCount=[17], eliteCount=[1], crossoverCount=[13], mutationCount=[3]]
		// Winner : GeneticAlgorithm [maxGenerationCount=400, maxExecutionTime=Infinite, targetFitness=0.001, maxStaleGenerations=100, mutationProbability=[1.0], selectionStrategy=[StochasticUniform], scalingStrategy=[RankScaling ], crossoverStrategy=[ScatteredDiscrete [checkClones=true]], populationCount=[16], eliteCount=[1], crossoverCount=[12], mutationCount=[3]]

		//Winner : GeneticAlgorithm [maxGenerationCount=400, maxExecutionTime=Infinite, targetFitness=1.0E-5, maxStaleGenerations=200, mutationProbability=[0.5361566559027836], selectionStrategy=[StochasticUniform], scalingStrategy=[RankScaling ], crossoverStrategy=[ScatteredFuzzy [checkClones=true]], populationCount=[21], eliteCount=[2], crossoverCount=[16], mutationCount=[3]]
		//Winner : GeneticAlgorithm [maxGenerationCount=400, maxExecutionTime=Infinite, targetFitness=1.0E-5, maxStaleGenerations=200, mutationProbability=[0.522016356957826], selectionStrategy=[Remainder], scalingStrategy=[RankScaling ], crossoverStrategy=[ScatteredFuzzy [checkClones=true]], populationCount=[26], eliteCount=[2], crossoverCount=[20], mutationCount=[4]]
		//Winner : GeneticAlgorithm [maxGenerationCount=400, maxExecutionTime=Infinite, targetFitness=1.0E-5, maxStaleGenerations=200, mutationProbability=[0.48713219288377574], selectionStrategy=[StochasticUniform], scalingStrategy=[RankScaling ], crossoverStrategy=[ScatteredFuzzy [checkClones=true]], populationCount=[25], eliteCount=[2], crossoverCount=[20], mutationCount=[3]]
		//Winner : GeneticAlgorithm [maxGenerationCount=400, maxExecutionTime=Infinite, targetFitness=1.0E-5, maxStaleGenerations=200, mutationProbability=[0.7580807332334755], selectionStrategy=[StochasticUniform], scalingStrategy=[RankScaling ], crossoverStrategy=[ScatteredFuzzy [checkClones=true]], populationCount=[24], eliteCount=[2], crossoverCount=[19], mutationCount=[3]]
		//Winner : GeneticAlgorithm [maxGenerationCount=400, maxExecutionTime=Infinite, targetFitness=1.0E-5, maxStaleGenerations=200, mutationProbability=[0.35102622565709574], selectionStrategy=[StochasticUniform], scalingStrategy=[RankScaling ], crossoverStrategy=[ScatteredFuzzy [checkClones=true]], populationCount=[28], eliteCount=[2], crossoverCount=[22], mutationCount=[4]]
		//Winner : GeneticAlgorithm [maxGenerationCount=400, maxExecutionTime=Infinite, targetFitness=1.0E-5, maxStaleGenerations=200, mutationProbability=[0.3570424963782296], selectionStrategy=[Remainder], scalingStrategy=[RankScaling ], crossoverStrategy=[ScatteredFuzzy [checkClones=true]], populationCount=[23], eliteCount=[2], crossoverCount=[18], mutationCount=[3]]
		//Winner : GeneticAlgorithm [maxGenerationCount=400, maxExecutionTime=Infinite, targetFitness=1.0E-5, maxStaleGenerations=200, mutationProbability=[0.5400877416346345], selectionStrategy=[Remainder], scalingStrategy=[RankScaling ], crossoverStrategy=[ScatteredFuzzy [checkClones=true]], populationCount=[27], eliteCount=[2], crossoverCount=[21], mutationCount=[4]]

		System.out.println("");
		System.out.println(r.getAvailableGenerations() + " " + r.getGenerationCount());
		for(Individual ind : r.getGeneration(r.getGenerationCount(),0)) {
			System.out.println(ind);
		}
		
	
		
	}
	
	private static GeneticAlgorithm buildGA() {
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
		
		var bootStrapProto = new GeneticAglrithmPrototype(proto,true,true,10,20);
		
		var ga= GeneticAlgorithm.builder().withPrototype(bootStrapProto)
				.withMaxGenerationCount(50)
				.withTargetFitness(0)
				.population()
				.withPopulationCount(30)
				.withCrossoverFraction(0.6f)
				.advanced()
				.withMutationProbability(0.4)
				.withForceCloneMutation(true,100)
				.migration(25)
				.withNewSubpopulations(3)
				.withNewSubpopulation()
				.withCrossoverStrategy(new SinglePointDiscrete(3))
			.build();
		
		return ga;
	}

	public static class GeneticAglrithmPrototype implements IndividualPrototype {

		IndividualPrototype prototype;
		boolean fuzzy, discrete;
		int minPopulationCount;
		int maxPopulationCount;

		public GeneticAglrithmPrototype(IndividualPrototype prototype, boolean fuzzySupported,
				boolean discreteSupported, int minPopulationCount, int maxPopulationCount) {
			this.fuzzy = fuzzySupported;
			this.discrete = discreteSupported;
			this.prototype = prototype;
			this.minPopulationCount = minPopulationCount;
			this.maxPopulationCount = maxPopulationCount;
		}

		@Override
		public Individual createIndividual() {

			var rng = new PcgRSFast();

			// population count
			int populationCount = rng.nextInt(maxPopulationCount - minPopulationCount) + minPopulationCount;

			// crossover strategy
			CrossoverStrategy crossover = null;
			int rn;

			if (fuzzy) {
				if (discrete) {
					rn = rng.nextInt(5);
				} else {
					rn = 2 + rng.nextInt(3);
				}
			} else {
				// Discrete only
				rn = rng.nextInt(2);
			}

			// TODO alter parent count?
			switch (rn) {
			case 0:
				crossover = new ScatteredDiscrete(2);
				break;
			case 1:
				crossover = new SinglePointDiscrete(2);
				break;
			case 2:
				crossover = new ScatteredFitnessFuzzy(2);
				break;
			case 3:
				crossover = new SinglePointFuzzy(2);
				break;
			case 4:
				crossover = new ScatteredFuzzy(2);
				break;
			}

			SelectionStrategy selection = null;

			switch (rng.nextInt(4)) {
			case 0:
				selection = new Remainder();
				break;
			case 1:
				selection = new Roulette();
				break;
			case 2:
				selection = new Tournament(rng.nextInt(2) + 2);
				break;
			case 3:
				selection = new StochasticUniform();
				break;
			}

			// TODO age scaling
			FitnessScalingStrategy scaling = null;

			switch (rng.nextInt(3)) {
			case 0:
				scaling = new RankScaling();
				break;
			case 1:
				scaling = new ProportionalScaling();
				break;
			case 2:
				scaling = new TopScaling(rng.nextDouble());
				break;
			}

			double mutationProbability = MathUtil.clampNumber(rng.nextGaussian() *0.5 + 0.5,0d,1d);
	
			return new GeneticAlgorithmIndividual(prototype, fuzzy, discrete,populationCount,crossover,selection,scaling,mutationProbability);
		}
	}

	public static class GeneticAlgorithmIndividual extends Individual implements VariableDescriptor {

		/// OPTIMIZE
		// runtime, fitness, gardient, generation count ...

	
		
		int repetitions = 5;
		
		OptimizationTarget optimTarget = OptimizationTarget.GenerationCount;
		
		// The protptype to optimize
		IndividualPrototype protoype;
		boolean isFuzzySupported;

//		CrossoverStrategyFuzzy[] availableFuzzyStrategies = new CrossoverStrategyFuzzy[] {
//			new ScatteredFuzzy
//		};
		boolean isDiscreteSupported;

//		CrossoverStrategyDiscrete[] availableDiscreteStrategies = new CrossoverStrategyDiscrete[] {
//
//		};

		CrossoverStrategy crossoverStrategy;
		SelectionStrategy selectionStrategy;
		private FitnessScalingStrategy scalingStrategy;
		private double mutationProbability;
		private int populationCount;

		// Crossover strategies

		public GeneticAlgorithmIndividual(IndividualPrototype protoype, boolean isFuzzySupported,
				boolean isDiscreteSupported, int populationCount, CrossoverStrategy crossover,
				SelectionStrategy selection, FitnessScalingStrategy scale,double mutationProbability) {

			if (!isFuzzySupported && !isDiscreteSupported) {
				throw new IllegalArgumentException("Either fuzzy or discrete types have to be supported");
			}

			this.protoype = protoype;
			this.isFuzzySupported = isFuzzySupported;
			this.isDiscreteSupported = isDiscreteSupported;

			this.populationCount = populationCount;
			this.crossoverStrategy = crossover;
			this.selectionStrategy = selection;
			this.scalingStrategy = scale;
			this.mutationProbability = mutationProbability;
		}

		@Override
		public int getVariableCount() {
			return 5;
		}

		@Override
		public Individual crossover(CrossoverStrategyFuzzy crossoverStrategy, Individual... crossoverParent) {
			// Currently not supported. But implement fuzzy at least for numeric values

			double populationCount = 0;
			double mutationProbability = 0;
			
			// population can be fuzzy
			double[][] matrix = crossoverStrategy.getCrossoverMatrix(crossoverParent);
			
			for (int i = 0; i < matrix.length; i++) {
				populationCount += matrix[i][0] * (int) crossoverParent[i].getValue(0);
				mutationProbability += matrix[i][4] * (int) crossoverParent[i].getValue(4);
			}
			
			if(populationCount < 5) {
				populationCount = 5;
			}

			int[] vector = CrossoverStrategyFuzzy.fuzzyToDiscrete(matrix);

			// Strategies can not be fuzzied!

			CrossoverStrategy crossover = crossoverParent[vector[1]].getValue(1);
			SelectionStrategy selection = crossoverParent[vector[2]].getValue(2);
			FitnessScalingStrategy scale = crossoverParent[vector[3]].getValue(3);

			
			return new GeneticAlgorithmIndividual(protoype, isFuzzySupported, isDiscreteSupported,
					(int) populationCount, crossover, selection, scale,mutationProbability);
		}

		@Override
		public Individual crossover(CrossoverStrategyDiscrete crossoverStrategy, Individual... crossoverParent) {

			int vector[] = crossoverStrategy.getCrossoverVector(crossoverParent);

			int populationCount = crossoverParent[vector[0]].getValue(0);
			CrossoverStrategy crossover = crossoverParent[vector[1]].getValue(1);
			SelectionStrategy selection = crossoverParent[vector[2]].getValue(2);
			FitnessScalingStrategy scale = crossoverParent[vector[3]].getValue(3);
			double mutationProbability = crossoverParent[vector[4]].getValue(4);

			return new GeneticAlgorithmIndividual(protoype, isFuzzySupported, isDiscreteSupported, populationCount,
					crossover, selection, scale,mutationProbability);
		}

		@Override
		public Individual mutate(double probability, double scaleFactor) {

			// population count
			int populationCount = this.populationCount;
			if (RNG.nextDouble() <= probability) {
				int popChange = RNG.nextInt(3)+1;
				populationCount += (RNG.nextBoolean()) ? popChange : -popChange;
				if(populationCount < 5) {
					populationCount = 5;
				}
			}

			// crossover strategy
			CrossoverStrategy crossover = this.crossoverStrategy;
			if (RNG.nextDouble() <= probability) {

				int rn;

				if (isFuzzySupported) {
					if (isDiscreteSupported) {
						rn = RNG.nextInt(5);
					} else {
						rn = 2 + RNG.nextInt(3);
					}
				} else {
					// Discrete only
					rn = RNG.nextInt(2);
				}

				// TODO alter parent count?
				switch (rn) {
				case 0:
					crossover = new ScatteredDiscrete(2);
					break;
				case 1:
					crossover = new SinglePointDiscrete(2);
					break;
				case 2:
					crossover = new ScatteredFitnessFuzzy(2);
					break;
				case 3:
					crossover = new SinglePointFuzzy(2);
					break;
				case 4:
					crossover = new ScatteredFuzzy(2);
					break;
				}
			}

			SelectionStrategy selection = this.selectionStrategy;
			if (RNG.nextDouble() <= probability) {

				switch (RNG.nextInt(4)) {
				case 0:
					selection = new Remainder();
					break;
				case 1:
					selection = new Roulette();
					break;
				case 2:
					selection = new Tournament(2);
					break;
				case 3:
					selection = new StochasticUniform();
					break;
				}
			}

			// TODO age scaling
			FitnessScalingStrategy scaling = this.scalingStrategy;
			if (RNG.nextDouble() <= probability) {

				switch (RNG.nextInt(3)) {
				case 0:
					scaling = new RankScaling();
					break;
				case 1:
					scaling = new ProportionalScaling();
					break;
				case 2:
					scaling = new TopScaling(RNG.nextDouble());
					break;
				}
			}
			
			double mutationProbability = this.mutationProbability;
			if (RNG.nextDouble() <= probability) {
				mutationProbability += (RNG.nextGaus()*0.5 + 0.5);
				mutationProbability = MathUtil.clampNumber(mutationProbability,0d,1d);
			}

			return new GeneticAlgorithmIndividual(protoype, isFuzzySupported, isDiscreteSupported, populationCount,
					crossover, selection, scaling,mutationProbability);

		}

		@Override
		protected double calculateFitness() {
			// build genetic algorithm and calculate fitness
			GeneticAlgorithm ga = buildGa();
			
			
			double fitness = 0;
			
			//Lets do x calculations.
			for(int i = 0; i < repetitions; i++) {
				ga.reset();
				Result r = ga.calculate(0);
				
				
				switch(optimTarget) {
				case Fitness:
					fitness += (Math.pow(1+r.getFitness(),2))/repetitions;
					break;
				case GenerationCount:
					//TODO if fitness does not reach maybe penalize much much header?
					//fitness += (Math.pow(1+r.getFitness(),2) * 1/r.getGenerationCount())/repetitions;
					fitness += r.getGenerationCount()/repetitions;
					
					break;
				case Runtime:
					fitness += (Math.pow(1+r.getFitness(),2) * 1/r.getExecutionTime())/repetitions;
					break;
				default:
					break;
				}
			}
			return fitness;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue(int index) {

			// We could put everything into an object array or do it manually here
			switch (index) {
			case 0:
				return (T) Integer.valueOf(populationCount);
			case 1:
				return (T) crossoverStrategy;
			case 2:
				return (T) selectionStrategy;
			case 3:
				return (T) scalingStrategy;
			case 4:
				return (T) Double.valueOf(mutationProbability);
			}
			return null;
		}

		@Override
		public String[] getVariableDescription() {
			String[] description = new String[5];
			description[0] = "Population Count";
			description[1] = "Crossover Strategy";
			description[2] = "Selection Strategy";
			description[3] = "Scaling Strategy";
			description[4] = "Mutation Proability";
			
			return description;
		}
		
		@Override
		public boolean[] getGroupByClasses() {
			return new boolean[] {false,false,false,true,false};
		}
		
		
		@Override
		public String[] toCSV() {
			// TODO Auto-generated method stub
			return null;
		}

		private GeneticAlgorithm ga;

		public GeneticAlgorithm buildGa() {

			if (ga == null) {
				
				if(populationCount < 5) {
					System.out.println("Pop count < 5: " + populationCount);
				}
				
				ga = GeneticAlgorithm.builder().withPrototype(protoype)
						// Stop criteria. They may not take to long or else the ga won't execute...
						.withMaxStaleGenerations(200)
						.withTargetFitness(1e-5)
						.advanced().withCrossoverStrategy(crossoverStrategy).withSelectionStrategy(selectionStrategy)
						.withMutationProbability(mutationProbability)
						.withScalingStrategy(scalingStrategy)
						
						// .withForceCloneMutation()
						.population().withPopulationCount(populationCount)
						/*
						.migration()
						.withMigrationInterval(100)
						.withNewSubpopulations(4)
						*/
						.build();
			}
			return ga;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			//result = prime * result + getOuterType().hashCode();
			result = prime * result + ((crossoverStrategy == null) ? 0 : crossoverStrategy.hashCode());
			result = prime * result + (isDiscreteSupported ? 1231 : 1237);
			result = prime * result + (isFuzzySupported ? 1231 : 1237);
			long temp;
			temp = Double.doubleToLongBits(mutationProbability);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + ((optimTarget == null) ? 0 : optimTarget.hashCode());
			result = prime * result + populationCount;
			result = prime * result + repetitions;
			result = prime * result + ((scalingStrategy == null) ? 0 : scalingStrategy.hashCode());
			result = prime * result + ((selectionStrategy == null) ? 0 : selectionStrategy.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GeneticAlgorithmIndividual other = (GeneticAlgorithmIndividual) obj;
//			if (!getOuterType().equals(other.getOuterType()))
//				return false;
			if (crossoverStrategy == null) {
				if (other.crossoverStrategy != null)
					return false;
			} else if (!crossoverStrategy.equals(other.crossoverStrategy))
				return false;
			if (isDiscreteSupported != other.isDiscreteSupported)
				return false;
			if (isFuzzySupported != other.isFuzzySupported)
				return false;
			if (Double.doubleToLongBits(mutationProbability) != Double.doubleToLongBits(other.mutationProbability))
				return false;
			if (optimTarget != other.optimTarget)
				return false;
			if (populationCount != other.populationCount)
				return false;
			if (repetitions != other.repetitions)
				return false;
			if (scalingStrategy == null) {
				if (other.scalingStrategy != null)
					return false;
			} else if (!scalingStrategy.equals(other.scalingStrategy))
				return false;
			if (selectionStrategy == null) {
				if (other.selectionStrategy != null)
					return false;
			} else if (!selectionStrategy.equals(other.selectionStrategy))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GeneticAlgorithmIndividual [isFuzzySupported=" + isFuzzySupported + ", isDiscreteSupported="
					+ isDiscreteSupported + ", crossoverStrategy=" + crossoverStrategy + ", selectionStrategy="
					+ selectionStrategy + ", scalingStrategy=" + scalingStrategy + ", mutationProbability="
					+ mutationProbability + ", populationCount=" + populationCount + ", ga=" + ga + "]";
		}

	

		

//		private BootstrapAlgoSettings getOuterType() {
//			return BootstrapAlgoSettings.this;
//		}
	}

	// Weighted kSAT

	private static Function<boolean[], Double> createFitnessFunction(int k, int numClauses, int maxVars,
			boolean verbose) {
		// 1. Build the claueses. He clause is an integer array pointing to the variable
		// used in the equation
		// A negative number indicates the negation of the variable.
		int clauses[][] = new int[numClauses][k];

		var RNG = new PcgRSFast();

		List<Integer> validVars = new ArrayList<Integer>(maxVars);
		for (int i = 0; i < maxVars; i++) {
			// Start at 1 - maxVars
			validVars.add(i + 1);
		}

		for (int i = 0; i < numClauses; i++) {

			// Make sure we have a "valid clause" i.e. not X and -X at the same time
			Collections.shuffle(validVars, RNG);
			Stack<Integer> s = new Stack<Integer>();
			s.addAll(validVars);

			for (int j = 0; j < k; j++) {
				// Start at 1 or else we can't multiply by negative one
				clauses[i][j] = s.pop() * (RNG.nextBoolean() ? 1 : -1);
			}
		}

		// Print it
		if (verbose)
			printFormula(clauses);

		double[] weightFactor = new double[numClauses];

		for (int i = 0; i < numClauses; i++) {
			weightFactor[i] = 1;// Math.random();
		}

		if (verbose)
			printFormulaModular(clauses, weightFactor, maxVars);

		return (x) -> {
			// The fitness function accepts multi
			double fitness = 0;
			for (int i = 0; i < clauses.length; i++) {

				boolean clauseResult = false;

				for (var literal : clauses[i]) {

					boolean literalResult;
					if (literal < 0) {
						literalResult = !x[-(literal + 1)];

					} else {
						literalResult = x[(literal - 1)];
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
