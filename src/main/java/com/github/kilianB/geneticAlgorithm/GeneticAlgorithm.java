package com.github.kilianB.geneticAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.kilianB.Require;
import com.github.kilianB.StringUtil;
import com.github.kilianB.concurrency.NamedThreadFactory;
import com.github.kilianB.datastructures.CircularQueue;
import com.github.kilianB.datastructures.CountHashCollection;
import com.github.kilianB.geneticAlgorithm.Individual.Origin;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategy;
import com.github.kilianB.geneticAlgorithm.crossover.ScatteredDiscrete;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.RankScaling;
import com.github.kilianB.geneticAlgorithm.migration.direction.MigrationProcess;
import com.github.kilianB.geneticAlgorithm.migration.direction.NetworkMigration;
import com.github.kilianB.geneticAlgorithm.migration.strategy.Elitism;
import com.github.kilianB.geneticAlgorithm.migration.strategy.MigrationStrategy;
import com.github.kilianB.geneticAlgorithm.mutationScaling.MutationScalingStrategy;
import com.github.kilianB.geneticAlgorithm.prototypes.IndividualPrototype;
import com.github.kilianB.geneticAlgorithm.result.Result;
import com.github.kilianB.geneticAlgorithm.result.Result.TerminationReason;
import com.github.kilianB.geneticAlgorithm.result.ResultListener;
import com.github.kilianB.geneticAlgorithm.rng.RngPool;
import com.github.kilianB.geneticAlgorithm.rng.RngPoolThreadLocal;
import com.github.kilianB.geneticAlgorithm.selection.SelectionStrategy;
import com.github.kilianB.geneticAlgorithm.selection.StochasticUniform;
import com.github.kilianB.pcg.fast.PcgRSFast;

/**
 * @author Kilian
 *
 */
public class GeneticAlgorithm {

	/**
	 * A warning which might be thrown multiple time per ga invocation and flood the
	 * log file. It's still a warning but may be muted separately
	 */
	public static final Level REP_WARNING = new Level("MILD WARNING", Level.WARNING.intValue() - 1) {
		private static final long serialVersionUID = 1L;
	};

	/** Logger instance */
	private static final Logger LOGGER = Logger.getLogger(GeneticAlgorithm.class.getName());

	/** Concurrent random number generator instance. */
	public static final RngPool RNG = RngPoolThreadLocal.pcgRS();

	// -------------- Stop Conditions-----------------------------------------------
	// -----------------------------------------------------------------------------

	/** Stop after computing x generations */
	private int maxGenerationCount;

	/** Stop if the ga takes longer than x ms to execute */
	private long maxExecutionTime;

	/** Stops if the fitness is smaller than the error Limit */
	private double targetFitness;

	/** Stop if the best result did not improve for x generations in a row */
	private int maxStallGenerations;

	// -------------- Reproduction Settings----------------------------------------
	// ----------------------------------------------------------------------------

	/** How likely is a gene in a mutation child altered */
	private List<Double> mutationProbability;

	/** How are parents for the next generation selected based on their fitness */
	private List<SelectionStrategy> selectionStrategy;

	/** How is the fitness weighted for parent selection */
	private List<FitnessScalingStrategy> scalingStrategy;

	/** Which genes are taken from which parent */
	private List<CrossoverStrategy> crossoverStrategy;

	/**
	 * How is the scale factor modified during algorithm generation passed to the
	 * mutation function
	 */
	private List<MutationScalingStrategy> mutationScaleStrategy;

	// -------------- Population Settings-------------------------------------------
	// -----------------------------------------------------------------------------

	/** How many individuals does a single generation contain */
	private List<Integer> populationCount;

	/**
	 * How many of the best individuals shall be kept in the next generation? A too
	 * high count will result in an inefficient search. By keeping the n - best
	 * individuals we guarantee that the populations best fitness does not decrease.
	 */
	private List<Integer> eliteCount;
	/**
	 * How many of the individuals in each generation are created using crossover
	 * operations
	 */
	private List<Integer> crossoverCount;

	/** How many of the individuals in each generation are created using mutation */
	private List<Integer> mutationCount;

	/** How many individuals migrate from one population to the next */
	private int migrationCount = 1;

	/** How many parents are needed in total for each generation */
	private List<Integer> newParentsNeededPerGeneration;

	/**
	 * Turn of for numerical problems? Useful for discrete TODO does not work for
	 * booleans! TODO: clone prevention crossover operation. force mutation mix..
	 */
	private boolean forceCloneMutation;

	/** After how many tries shall the mutation be considered unsuccessful */
	private int mutationAttemptCutoff;

	// -------------- Migration Options---------------------------------------
	// -----------------------------------------------------------------------

	/**
	 * How often do the best individuals migrate from one sub population to another
	 */
	private int migrationInterval;

	/** How are the individuals selected from each sub population */
	private MigrationStrategy migrationStrategy;

	/** In which direction (from -- to) shall migration happen? */
	private MigrationProcess migrationProcess;

	// -------------- Internal state--------------------------------------------
	// -------------------------------------------------------------------------

	/**
	 * The initial population when this ga was created. Useful to reset the ga to an
	 * earlier state0
	 */
	private ArrayList<Individual[]> initialPopulation;

	// Internal Execution state

	/** The current generation number */
	private int currentGeneration = 0;

	/** The current population */
	private ArrayList<Individual[]> population;

	/**
	 * Holds the best fitness value for the last [1 - maxStaleGenerations] to keep
	 * track if an improvement occurred
	 */
	private CircularQueue<Double> bestFitness;

	/**
	 * Set containing listener which will be updated once intermediate information
	 * from the ga are available
	 */
	private HashSet<ResultListener> resultListener = new HashSet<>();

	/** Manual user interruption flag. */
	private transient volatile boolean interupted = false;

	/** Format used to print results to console */
	private transient String resultFormat;
	/**
	 * Remember to see if our sum expanded beyond what was originally expected. If
	 * this is the case the result format needs to be recomputed
	 */
	private transient int sumLength = -1;

	private GeneticAlgorithm(MigrationBuilder migrationBuilder) {

		// Repwarning
		LOGGER.setLevel(Level.SEVERE);

		Builder builder = migrationBuilder.internalBuilder;

		/*
		 * Overall ga settings
		 */

		// How many sub populations do we have?
		int subPopulationCount = migrationBuilder.populationCount.size();

		// Stop criteria

		this.maxGenerationCount = Require.positiveValue(builder.maxGenerationCount,
				"Generation count has to be a positive int value");

		this.maxExecutionTime = Require.positiveValue(builder.maxExecutionTime,
				"Execution time has to be a positive int value");

		this.targetFitness = Require.inRange(builder.targetFitness, 0d, Double.MAX_VALUE,
				"Target fitness has to be in range of [0-Double.MAX]");

		this.maxStallGenerations = builder.maxStaleGenerations;

		if (maxStallGenerations > 0) {
			bestFitness = new CircularQueue<Double>(maxStallGenerations);
		}

		// Migration
		migrationInterval = Require.positiveValue(builder.migrationInterval, "Migration Interval must be positive");

		migrationStrategy = Objects.requireNonNull(builder.migrationStrategy, "Migration Strategy may not be null");

		migrationProcess = Objects.requireNonNull(builder.migrationProcess, "Migration Process may not be null");

		if (subPopulationCount > 0 && migrationInterval > maxGenerationCount) {
			LOGGER.warning(
					"Max generation count is > than migration interval -> no migration will take place. Consider "
							+ "to use a single large population instead to increase performance");
		}

		/*
		 * Sub Population specific settings
		 */

		// Check input parameters
		this.populationCount = migrationBuilder.populationCount;

		for (Integer popCount : populationCount) {
			Require.positiveValue(popCount, "Population count has to be a positive int value");
		}

		this.eliteCount = new ArrayList<>(subPopulationCount);
		this.crossoverCount = new ArrayList<>(subPopulationCount);
		this.mutationCount = new ArrayList<>(subPopulationCount);
		this.newParentsNeededPerGeneration = new ArrayList<>(subPopulationCount);
		this.mutationProbability = (List<Double>) Require.inRange(migrationBuilder.mutationProbability, 0d, 1d,
				"Valid range for mutation probability [0-1]");

		this.mutationScaleStrategy = (List<MutationScalingStrategy>) Require
				.nonNull(migrationBuilder.mutationScalingStrategy, "Mutation scaling strategy may not be null");

		this.crossoverStrategy = (List<CrossoverStrategy>) Require.nonNull(migrationBuilder.crossoverStrategy,
				"Crossover stratgey can't be null");

		for (CrossoverStrategy strat : crossoverStrategy) {
			Require.inRange(strat.getParentCount(), 2, Integer.MAX_VALUE,
					"Valid range for parent per crossover [2-Integer Max]");
		}

		// Calculate the elite count for each sub population
		for (int i = 0; i < subPopulationCount; i++) {

			double eliteFraction = migrationBuilder.eliteFraction.get(i);
			double crossoverFraction = migrationBuilder.crossoverFraction.get(i);

			if (eliteFraction + crossoverFraction > 1) {
				throw new IllegalArgumentException(
						"The sum of elite fraction and crossover fraction may not be greater than 1");
			}

			int eCount = (int) Math.ceil(populationCount.get(i) * eliteFraction);

			this.eliteCount.add(eCount);

			if (eCount <= 0) {
				LOGGER.warning(
						"No elite children may impact the algorithm ability to produce good results. Population: " + i);
			} else if (eCount == populationCount.get(i)) {
				LOGGER.warning(
						"Only elite children will result in total converggence after 1 itteration. Population: " + i);
			}

			this.crossoverCount.add((int) Math.floor(populationCount.get(i) * crossoverFraction));

			this.mutationCount.add(populationCount.get(i) - eliteCount.get(i) - crossoverCount.get(i));

			if (crossoverCount.get(i) <= 0) {
				LOGGER.warning("No crossover children may impact the algorithm ability to converge. Population: " + i);
			}

			if (mutationCount.get(i) <= 0) {
				LOGGER.warning(
						"No mutation children may impact the algorithm ability to produce a diverse population. Population: "
								+ i);
			}

			if (mutationProbability.get(i) == 0) {
				LOGGER.warning(
						"A mutation probability of 0% will impact the algorithm's ability to produce a diverse population. Population: "
								+ i);
			}

			newParentsNeededPerGeneration
					.add(crossoverStrategy.get(i).getParentCount() * crossoverCount.get(i) + mutationCount.get(i));
		}

		// Shall we make this population specific?
		if (builder.initialPopulation != null) {
			builder.initialPopulation[0].getVariableCount();
		}

		this.forceCloneMutation = builder.forceCloneMutation;
		if (forceCloneMutation) {
			this.mutationAttemptCutoff = Require.positiveValue(builder.mutationAttemptCutoff,
					"Mutation Cutoff has to be positive if force clone is enabled");
		}

		population = new ArrayList<Individual[]>(subPopulationCount);
		initialPopulation = new ArrayList<Individual[]>(subPopulationCount);

		// Step 0 build initial population

		/*
		 * This can be achieved by either supplying the population immediately or by
		 * passing on a prototype object.
		 */

		if (builder.initialPopulation != null) {

			// Flag the individuals for statistical analysis
			for (var individual : builder.initialPopulation) {
				individual.setBirth(-1);
				individual.setOrigin(Origin.INITIAL_POPULATION);
			}

			// Since we only have an initial population we just copy it to every
			// suppopulation
			for (int i = 0; i < subPopulationCount; i++) {
				initialPopulation.add(builder.initialPopulation);
			}
		} else {
			if (builder.individualPrototype == null) {
				throw new IllegalStateException("Either a initial population or a prototype has to be" + "supplied");
			}

			for (int i = 0; i < subPopulationCount; i++) {
				Individual[] initialPopulation = new Individual[populationCount.get(i)];

				for (int j = 0; j < populationCount.get(i); j++) {
					initialPopulation[j] = builder.individualPrototype.createIndividual();
					initialPopulation[j].setBirth(-1);
					initialPopulation[j].setOrigin(Origin.INITIAL_POPULATION);
				}
				this.initialPopulation.add(initialPopulation);
			}
		}

		this.selectionStrategy = (List<SelectionStrategy>) Require.nonNull(migrationBuilder.selectionStrategy,
				"Selection strategy can't be null");

		this.scalingStrategy = (List<FitnessScalingStrategy>) Require.nonNull(migrationBuilder.scalingStrategy,
				"Scaling strategy can't be null");

		// Shallow copy

		// Do we also need to deep clone the individual arrays?
		this.population = new ArrayList<>(this.initialPopulation);

		// 0.1 Sort initial population
		for (int i = 0; i < this.population.size(); i++) {
			Arrays.sort(this.population.get(i));
		}

	}

	/**
	 * 
	 * Perform a single generation step of one of the sub populations.
	 * 
	 * 1. Scaling fitness 2. Selecting parents for next generation 3. Reproduction
	 * 3.1 Elite Children 3.2 Mutation 3.3 Crossover 4.Clone prevention
	 * 
	 * As a side effect the population array in the index is updated. The returned
	 * population is stricly sorted by fitness.
	 * 
	 * @param popIndex
	 * @return the fitness value of the best individual contained in the newly
	 *         generated population
	 */
	private double performGeneration(int popIndex) {

		Individual[] population = this.population.get(popIndex);

		// 1 Scale fitness
		ScaledFitness[] scaledPopulation = scalingStrategy.get(popIndex).scaleFitness(population,
				newParentsNeededPerGeneration.get(popIndex));

		// 2. Select parents
		List<Individual> parents = new ArrayList<>(Arrays.asList(selectionStrategy.get(popIndex)
				.selectParents(scaledPopulation, newParentsNeededPerGeneration.get(popIndex))));

		// TODO or use thread local rng if we already have one. we save object creation
		// overhead
		var rnng = new PcgRSFast();
		Collections.shuffle(parents, rnng);

		// 3. Reproduction
		Individual[] nextGeneration = new Individual[populationCount.get(popIndex)];

		// 3.1 Elite children
		for (int i = 0; i < eliteCount.get(popIndex); i++) {
			nextGeneration[i] = population[i];
		}

		// 3.2 Mutation

		// TODO max stall generations
		double scale = mutationScaleStrategy.get(popIndex).computeScaleFactor(currentGeneration,
				this.maxGenerationCount, this.population.get(popIndex)[0].getFitness(), this.targetFitness, 0);

		CrossoverStrategy crossoverStrategy = this.crossoverStrategy.get(popIndex);

		for (int i = 0; i < mutationCount.get(popIndex); i++) {

			// Parents are randomized. no need to remove from random position. Remove from
			// tail or O(1)
			Individual newIndividual = parents.remove(parents.size() - 1).mutate(mutationProbability.get(popIndex),
					scale);
			newIndividual.setOrigin(Origin.MUTATION);
			newIndividual.setBirth(currentGeneration);
			nextGeneration[i + eliteCount.get(popIndex)] = newIndividual;
		}

		// 3.3 Crossover
		int parentsPerCrossover = crossoverStrategy.getParentCount();

		// jmh benchmarked on i5-6600
		if (parentsPerCrossover < 14) {
			/*
			 * Parents are expected to be randomized
			 */
			crossover(parents, nextGeneration, parentsPerCrossover, crossoverStrategy, eliteCount.get(popIndex),
					mutationCount.get(popIndex), crossoverCount.get(popIndex));
		} else {
			/*
			 * When working with larger parent sizes per crossover the O(n) of the
			 * arraylist's contain method starts to take it's toll. Use a hash hash
			 * collection instead
			 */
			crossoverHash(parents, nextGeneration, parentsPerCrossover, crossoverStrategy, eliteCount.get(popIndex),
					mutationCount.get(popIndex), crossoverCount.get(popIndex));
		}

		// 4 Clone Prevention

		/*
		 * Prevent clones from being present in the population. This step usually
		 * decreases average generations needed as well as total time for an ga
		 * execution and works positively on diversity. On the other hand the population
		 * will not converge as quickly. The force mutation might not be optimal for
		 * some discrete solutions
		 */

		// TODO fix this for double individuals ... individuals are not equal due to
		// floating point numbers
		if (forceCloneMutation) {

			int originalLenght = nextGeneration.length;

			// Make sure that we keep the oldest individual in case of clones. This might
			// get interesting for age scaling at some point
			Arrays.sort(nextGeneration, sortByAge);
			HashSet<Individual> uniqueParents = new HashSet<>();
			List<Individual> nextGenerationTemp = new ArrayList<>(nextGeneration.length);

			// Increase the mutation probability linearly at the last attempt reach 100%
			double mutationScale = 1 / (mutationProbability.get(popIndex) * (mutationAttemptCutoff - 1));

			for (var individual : nextGeneration) {

				int attempt = 0;

				while (uniqueParents.contains(individual)) {
					/*
					 * Make sure that individuals who rely on scale to alter values always have a
					 * bit wiggle room to work with something.
					 */
					if (scale > 1e-10) {
						individual = individual.mutate(mutationProbability.get(popIndex) * mutationScale * attempt,
								scale);
					} else {
						individual = individual.mutate(mutationProbability.get(popIndex) * mutationScale * attempt,
								1e-10);
					}

					individual.setBirth(currentGeneration);
					individual.setOrigin(Origin.FORCE_CLONE_MUTATION);
					attempt++;
					if (attempt > mutationAttemptCutoff) {
						LOGGER.log(REP_WARNING, "Mutation Cutoff reached. Carry over clone");
						break; // FIX 11.09.18
					}
				}
				;
				uniqueParents.add(individual);
				nextGenerationTemp.add(individual);
			}

			nextGeneration = nextGenerationTemp.toArray(new Individual[nextGenerationTemp.size()]);
			if (nextGeneration.length < 5) {
				System.out.println("NEXT GEN ERROR: Next Gen Length: " + nextGeneration.length + " Original Length: "
						+ originalLenght + " Requested Lenght: " + populationCount.get(popIndex));
			}
		}

		assert nextGeneration.length == populationCount
				.get(popIndex) : "Next Generation not same length as requested count";

		this.population.set(popIndex, nextGeneration);

		// 0 Sort population by fitness values
		Arrays.sort(nextGeneration);

		return population[0].getFitness();

	}

	/**
	 * Perform crossover with an arraylist as it's base.
	 * 
	 * @param parents             Individual array with parents used for crossover
	 * @param nextGeneration      array holding the created individuals
	 * @param parentsPerCrossover number of parents used per crossover operation
	 * @param crossoverStrategy   The strategy used for crossover
	 * @param eliteCount          The number of elite children used as offset to
	 *                            access the right the nextGeneration array
	 * @param mutationCount       The number of mutation children used as offset to
	 *                            access the right the nextGeneration array
	 * @param crossoverCount      The number of crossover children used as offset to
	 *                            access the right the nextGeneration array
	 */
	private void crossover(List<Individual> parents, Individual[] nextGeneration, int parentsPerCrossover,
			CrossoverStrategy crossoverStrategy, int eliteCount, int mutationCount, int crossoverCount) {

		ArrayList<Individual> crossoverParticipants = new ArrayList<>(parentsPerCrossover);

		for (int i = 0; i < crossoverCount; i++) {
			/*
			 * Fix for cloned individual bug 23.08.2018 When working with a small set of
			 * individuals the most fit parent is likely to occur more often in the parents
			 * set then others which might result in a crossover between the same individual
			 * -> no new individual but rather a clone of the parent is created.!
			 */
			crossoverParticipants.clear();

			Individual firstParent = parents.remove(parents.size() - 1);
			crossoverParticipants.add(firstParent);

			for (int j = 1; j < parentsPerCrossover; j++) {

				// TODO this shows up as 2% of the runtime. Can we speed it up?
				// Are those in sequential order btw? Those are potentially orderde
				// This stream will return wrong parents!.

				// They are ordered randomly anways. start from the back and take a look
				// itterating
				// at worst it is as expensive as the stream implementation. (IT won't be!).
				// we can memorize the index and remove it quickly form the ArrayList.class if
				// we
				// are lucky it's at the end and we even safe the array copy call

				// only bigger ...
				int index = -1;
				for (int m = parents.size() - 1; m >= 0; m--) {
					if (!crossoverParticipants.contains(parents.get(m))) {
						index = m;
						break;
					}
				}

				if (index > 0) {
					crossoverParticipants.add(parents.remove(index));
				} else {
					LOGGER.log(REP_WARNING, "No two individual parents found for crossover. Introduce a clone");
					crossoverParticipants.add(parents.remove(parents.size() - 1));
				}

			}

			Individual newIndividual = crossoverParticipants.get(0).crossover(crossoverStrategy,
					crossoverParticipants.toArray(new Individual[crossoverParticipants.size()]));
			newIndividual.setOrigin(Origin.CROSSOVER);
			newIndividual.setBirth(currentGeneration);
			nextGeneration[i + eliteCount + mutationCount] = newIndividual;
		}
	}

	/**
	 * Perform crossover with a hash collection as it's base. Opposed to
	 * {@link #crossover(List, Individual[], int, CrossoverStrategy, int, int, int)}
	 * this methods complexity does not scale linearly with n and will be quicker
	 * for small parent crossover points.
	 * 
	 * @param parents             Individual array with parents used for crossover
	 * @param nextGeneration      array holding the created individuals
	 * @param parentsPerCrossover number of parents used per crossover operation
	 * @param crossoverStrategy   The strategy used for crossover
	 * @param eliteCount          The number of elite children used as offset to
	 *                            access the right the nextGeneration array
	 * @param mutationCount       The number of mutation children used as offset to
	 *                            access the right the nextGeneration array
	 * @param crossoverCount      The number of crossover children used as offset to
	 *                            access the right the nextGeneration array
	 */
	private void crossoverHash(List<Individual> parents, Individual[] nextGeneration, int parentsPerCrossover,
			CrossoverStrategy crossoverStrategy, int eliteCount, int mutationCount, int crossoverCount) {

		CountHashCollection<Individual> parentSet = new CountHashCollection<Individual>();

		for (int i = 0; i < crossoverCount; i++) {
			/*
			 * Fix for cloned individual bug 23.08.2018 When working with a small set of
			 * individuals the most fit parent is likely to occur more often in the parents
			 * set then others which might result in a crossover between the same individual
			 * -> no new individual but rather a clone of the parent is created.!
			 */

			parentSet.clear();
			Individual firstParent = parents.remove(parents.size() - 1);
			parentSet.add(firstParent);

			for (int j = 1; j < parentsPerCrossover; j++) {

				Optional<Individual> parentCandidate = parents.stream().filter(newPar -> !parentSet.contains(newPar))
						.findAny();

				// Arraylist contains is expensive. Convert it to a hashset first?

				if (parentCandidate.isPresent()) {
					Individual secondParent = parentCandidate.get();
					parents.remove(secondParent);
					parentSet.add(secondParent);
				} else {
					LOGGER.log(REP_WARNING, "No two individual parents found for crossover. Introduce a clone");
					// Since we need to stay consistent simply delete a random individual. they are
					// all the same.

					Individual duplicateParent = parents.remove(parents.size() - 1);
					parentSet.add(duplicateParent);
				}
			}
			Individual[] crossoverParticipants = parentSet.toArray(new Individual[parentSet.size()]);

			Individual newIndividual = crossoverParticipants[0].crossover(crossoverStrategy, crossoverParticipants);
			newIndividual.setOrigin(Origin.CROSSOVER);
			newIndividual.setBirth(currentGeneration);
			nextGeneration[i + eliteCount + mutationCount] = newIndividual;
		}
	}

	/**
	 * Execute the genetic algorithm in verbose mode. Intermediate information will
	 * be printed to the console.
	 * 
	 * <p>
	 * Calling calculate twice without calling {@link #reset()} will carry on
	 * optimization from the currently active population. The algorithm will
	 * continue to work on populations which was produced last and might create
	 * meaningful results when it was stopped due to generationCount/runtime or
	 * staleness conditions.
	 * 
	 * <p>
	 * If the algorithms terminated due to reaching it's determined fitness score a
	 * subsequent call to calculate will do nothing.
	 * 
	 * @param recordGeneration Indicates which generations are included in the
	 *                         result object and trigger the result listener
	 *                         <p>
	 *                         <b>Example:</b>
	 *                         <ul>
	 *                         <li>A value of 1 will return every generation ever
	 *                         created</li>
	 *                         <li>A value of 10 will return every 10th
	 *                         generation</li>
	 *                         <li>A value of 0 or smaller will return only the
	 *                         final generation. (recommended if you are not
	 *                         interested in further statistics but just the final
	 *                         result)</li>
	 *                         </ul>
	 *                         Be aware that holding a reference to each and any
	 *                         individual generated during the computation may
	 *                         quickly accumulate memory usage.
	 * 
	 * 
	 * @return A result object containing information about the generated
	 *         populations as well as statistics . The most fit individual contains
	 *         the best solution found by the GA.
	 * @see {@link Result}
	 */
	public Result calculate(int recordGeneration) {
		return calculate(recordGeneration, Integer.MAX_VALUE, true);
	}

	//@formatter:off
	/**
	 * Execute the genetic algorithm.
	 * 
	 * <p> Calling calculate twice without execution {@link #reset()} first, will carry on
	 * optimization from the currently active population. If calculate was aborted due to 
	 * <code>maxGenerationCount</code>,<code>maxstaleness</code>, or <code>fitness</code> subsequent calls to calculate
	 * will do nothing.
	 * 
	 * @param recordGeneration
	 *            Indicates which generations are included in the result object and
	 *            trigger the result listeners.
	 *            <p><b>Example:</b> 
	 *            <ul> 
	 *            	<li>A value of 1 will return every
	 *            		generation created</li> <li>A value of 10 will return every
	 *            		10th generation</li> 
	 *            	<li>A value of 0 or smaller will return only
	 *            		the final generation. (recommended if you are not interested in
	 *            		further statistics but just the final result)</li> 
	 *            </ul> 
	 *            
	 *            The generation triggering a stop condition will <b>always</b> be included 
	 *            in the result object.
	 *            Be aware that holding a reference to each and any individual generated
	 *            during the computation may quickly accumulate memory usage. <p>
	 * 
	 * @param generation Only execute up to generations generation. This allows you to alter settings
	 * 	of the ga before continuing operation. This setting <b>should not</b> be used as an 
	 * ordinary stop parameter for the genetic algorithm. Please use 
	 * {@link Builder#withMaxGenerationCount(int)} instead as this will have proper effects
	 * on the scale factor during mutation. <p>
	 * 
	 * @param verbose if true output will be printed to the console. If false 
	 * 	the algorithms runs silently omitting console output but still notifying the attached listeners.
	 * 
	 * @return A result object containing information about the generated
	 *         populations as well as statistics . The most fit individual contains
	 *         the best solution found by the GA.
	 * @see {@link Result}
	 */
	//@formatter:on
	@SuppressWarnings("deprecation")
	public Result calculate(int recordGeneration, int generations, boolean verbose) {

		/*
		 * Setup execution environment
		 */
		Result resultObject = new Result(recordGeneration);

		// Add the initial population
		resultObject.addGeneration(-1, population, 0);

		ExecutorService threadPool = Executors.newCachedThreadPool(new NamedThreadFactory("Perform Generation"));

		// Prepare tasks to asynchronously create a new generation
		List<Callable<Double>> performGeneration = new ArrayList<>(population.size());

		for (int i = 0; i < population.size(); i++) {
			performGeneration.add(new PerformGeneration(i));
		}

		/*
		 * Current calculation generation count. Opposed to currentGeneration which
		 * starts at the first time calulate was called (doesn't reset after stopping).
		 */
		int generation = 0;
		// Clear interrupt flag just in case
		interupted = false;

		long startRuntime = System.currentTimeMillis();

		// Generation count stopping criteria
		for (generation = 0; currentGeneration < maxGenerationCount; currentGeneration++, generation++) {
			// Check stop criteria

			// User requested to only perform x generations and to abort afterwards (not the
			// same as max generations)
			if (generation == generations) {
				LOGGER.warning("Abort execution due to stepwise requested generation reached");
				resultObject.setTerminationReason(TerminationReason.GenerationStep);
				break;
			}

			if (interupted) {
				interupted = false;
				LOGGER.warning("Abort execution due to user interrupt");
				resultObject.setTerminationReason(TerminationReason.Interrupted);
				break;
			}

			long runtime = System.currentTimeMillis() - startRuntime;
			if ((runtime) > maxExecutionTime) {
				LOGGER.warning("Abort execution due to time limitation");
				resultObject.setTerminationReason(TerminationReason.Runtime);
				break;
			}

			// Perform generation

			double bestGeneratedFitness = Double.MAX_VALUE;

			try {
				List<Future<Double>> futures = threadPool.invokeAll(performGeneration);

				// Wait for all sub populations to perform generation
				for (var future : futures) {
					double fitness = future.get();
					if (fitness < bestGeneratedFitness) {
						bestGeneratedFitness = fitness;
					}
				}
			} catch (InterruptedException | ExecutionException e) {
				resultObject.setTerminationReason(TerminationReason.Exception);
				resultObject.setException(e);
				LOGGER.severe("Abort: An error occured during the execution. The produced results"
						+ " are most likely not meaningful. " + e);
				e.printStackTrace();
				break;
			}

			// 5.0 Migration

			// For memory sake don't copy the entire population. This will break if our
			// migration count
			// is higher than our population count as this WILL lead to migration children
			// to be potentially
			// selected to migrate into the next , but on the flipside if this happens the
			// settings of the
			// ga are simply screwed. No reason to waste too much performance on this case

			// TODO

			boolean migrationGeneration = population.size() > 1 && currentGeneration % migrationInterval == 0
					&& (currentGeneration != 0);

			// True if we should migrate
			if (migrationGeneration) {

				// migration candidates from each population
				ArrayList<Individual[]> migratingIndividuals = new ArrayList<Individual[]>();

				for (int i = 0; i < population.size(); i++) {
					migratingIndividuals.add(migrationProcess.migratedIndividuals(population, i,
							migrationStrategy.getMigrationCount(), migrationStrategy));
				}

				for (int i = 0; i < population.size(); i++) {

					// Swap out the currently worst candidates even if they are better?
					System.arraycopy(migratingIndividuals.get(i), 0, population.get(i),
							population.get(i).length - migrationCount, migrationCount);
					// TODO New concept population laziness. Remember which origin they came from
					// and
					// prevent an individual to migrate to every other sub population or else this
					// will just flood everything.
				}

				for (int i = 0; i < population.size(); i++) {
					Arrays.sort(this.population.get(i));
				}

			}

			if (bestGeneratedFitness <= targetFitness) {
				LOGGER.warning("Done due to good solution");
				resultObject.setTerminationReason(TerminationReason.Fitness);
				break;
			}

			if (currentGeneration == (maxGenerationCount - 1)) {
				LOGGER.warning("Done due generation limit reached");
				resultObject.setTerminationReason(TerminationReason.Generation);
				break;
			}

			if (bestFitness != null) {
				bestFitness.add(bestGeneratedFitness);
				if (bestFitness.isAtFullCapacity() && bestFitness.peek() == bestGeneratedFitness) {
					LOGGER.warning("Abort execution due generation staleness reached");
					resultObject.setTerminationReason(TerminationReason.Staleness);
					break;
				}
			}

			if (recordGeneration > 0 && currentGeneration % recordGeneration == 0) {

				resultObject.addGeneration(currentGeneration, population, runtime);

				if (verbose) {
					// Statistics over all populations
					DoubleSummaryStatistics summary = resultObject.getSummary();

					boolean firstGen = generation == 0;
					int chNeededForSum = StringUtil.charsNeeded(summary.getSum());

					if (firstGen || sumLength < chNeededForSum) {
						// Create the output format for console logging
						resultFormat = createOutputFormatAndPrintHeader(summary, firstGen);
						sumLength = chNeededForSum;
					}
					printGenerationStatistics(resultObject, summary, migrationGeneration);
				}

				// Notify event listener
				for (var listener : resultListener) {
					listener.intermediateResult(resultObject);
				}
			}
		}

		// Stop thread pool no tasks should be scheduled at this point. We don't return
		// early but let all generations finish before aborting.
		threadPool.shutdown();

		long totalRuntime = System.currentTimeMillis() - startRuntime;

		// Sort one last time.
		for (int i = 0; i < population.size(); i++) {
			Arrays.sort(this.population.get(i));
		}

		resultObject.addGeneration(currentGeneration, population, totalRuntime);

		// Print some more stats
		var summary = resultObject.getSummary();

		if (verbose && resultFormat != null && summary != null) {

			printGenerationStatistics(resultObject, summary, false);
			System.out.println("* migration took place.\n");
			System.out.printf("%-20s %d ms%n", "Runtime Total:", totalRuntime);
			System.out.printf("%-20s %.3f %s%n", "Runtime: ",
					(totalRuntime / (double) resultObject.getGenerationCount()), "ms per gen");
			System.out.printf("%-20s %s%n%n", "Termination Reason:", resultObject.getTerminationReason());
		}

		for (var listener : resultListener) {
			listener.finalResult(resultObject);
		}

		return resultObject;
	}

	private void printGenerationStatistics(Result resultObject, DoubleSummaryStatistics summary,
			boolean migrationGeneration) {
		// nest them in an obj array
		Object[] obj = new Object[6 + population.size()];
		obj[0] = migrationGeneration ? "*" : " ";
		obj[1] = currentGeneration;
		obj[2] = summary.getMin();
		obj[3] = summary.getMax();
		obj[4] = summary.getAverage();
		obj[5] = summary.getSum();

		// Print subpopulation
		if (population.size() > 1) {
			for (int i = 0; i < population.size(); i++) {
				obj[6 + i] = resultObject.getSummarySubPopulation(currentGeneration, i).getMin();
			}
		}

		System.out.printf(resultFormat, obj);
	}

	/**
	 * Resets the ga to it's initial state. A reset leads to the same population
	 * being used as starting point even if a prototype was used.
	 */
	public void reset() {
		// Shallow copy
		this.population = new ArrayList<>(initialPopulation);
		currentGeneration = 0;
		resultFormat = null;
		if (bestFitness != null) {
			bestFitness.clear();
		}
		
		if(mutationScaleStrategy != null) {
			for(var strategy: mutationScaleStrategy) {
				strategy.reset();
			}
		}
		sumLength = -1;
	}

	/**
	 * Signals the ga to stop calculation and to keep the
	 * 
	 * The ga will stop at it's next generation calculation and
	 * 
	 */
	public void stop() {
		interupted = true;
	}

	/**
	 * Adds a result listener to this object if it isn't already attached. The
	 * result listener will be notified when ever a new generation was created.
	 * 
	 * <p>
	 * The result listener will only be called for every nth generation. The n is
	 * defined with the {@link #calculate(int)} call.
	 * 
	 * @param listener The result listener to add
	 * @return true if the listener was added successfully. False if it was already
	 *         added.
	 */
	public synchronized boolean addResultListener(ResultListener listener) {
		if (listener != null) {
			return resultListener.add(listener);
		}
		return false;
	}

	/**
	 * Remove the result listener from this genetic algorithm object. Removed result
	 * listeners will no longer be notified of new generations.
	 * 
	 * @param listener The listener to remove
	 * @return true if the listener was removed. False if the listener was not part
	 *         of the genetic algorithm.
	 */
	public synchronized boolean removeResultListener(ResultListener listener) {
		return resultListener.remove(listener);
	}

	@Override
	public String toString() {

		String excecutionTime;
		if (maxExecutionTime == Long.MAX_VALUE) {
			excecutionTime = "Infinite";
		} else {
			excecutionTime = String.valueOf(maxExecutionTime);
		}

		return "GeneticAlgorithm [maxGenerationCount=" + maxGenerationCount + ", maxExecutionTime=" + excecutionTime
				+ ", targetFitness=" + targetFitness + ", maxStaleGenerations=" + maxStallGenerations
				+ ", mutationProbability=" + mutationProbability + ", selectionStrategy=" + selectionStrategy
				+ ", scalingStrategy=" + scalingStrategy + ", crossoverStrategy=" + crossoverStrategy
				+ ", populationCount=" + populationCount + ", eliteCount=" + eliteCount + ", crossoverCount="
				+ crossoverCount + ", mutationCount=" + mutationCount + "]";
	}

	/**
	 * Comparator sorting individuals based on their creation generation.
	 */
	private Comparator<Individual> sortByAge = (i1, i2) -> {
		return Integer.compare(i1.getBirth(), i2.getBirth());
	};

	/*
	 * Helper methods
	 */
	/**
	 * Assemble the console output to display the progress of the genetic algorithm
	 * 
	 * @param summary     summary statistics describing the ga's current state
	 * @param printHeader if true also create the header of the table.
	 * @return the string to print
	 */
	private String createOutputFormatAndPrintHeader(DoubleSummaryStatistics summary, boolean printHeader) {
		// Build string format to display the result.

		// +2 for decimal part +1 for delimiter
		int cNeededForGen = StringUtil.charsNeeded(maxGenerationCount);
		int cNeededForBest = StringUtil.charsNeeded(summary.getMin()) + 5;
		int cNeededForWorst = StringUtil.charsNeeded(summary.getMax()) + 3 + 1;
		int cNeededForAvg = Math.max("Average".length(), StringUtil.charsNeeded(summary.getAverage()) + 3 + 1);
		int cNeededForSum = StringUtil.charsNeeded(summary.getAverage() * summary.getCount()) + 3 + 1;

		StringBuilder resultFormatSb = new StringBuilder();
		// Migration generation flag wirst %s
		resultFormatSb.append("%s").append("%").append(cNeededForGen).append("d | %").append(cNeededForBest)
				.append(".4f | %").append(cNeededForWorst).append(".2f | %").append(cNeededForAvg).append(".2f | %")
				.append(cNeededForSum).append(".2f ");

		// add sub population information
		if (population.size() > 1) {
			resultFormatSb.append("||");
			for (int i = 0; i < population.size(); i++) {
				int cNeeded = Math.max(cNeededForBest, ("Best(" + i + ")").length());
				resultFormatSb.append("| %").append(cNeeded).append(".4f ");
			}
		}

		resultFormatSb.append("%n");

		// Print header to console

		if (printHeader) {
			StringBuilder resultHeaderSb = new StringBuilder();

			resultHeaderSb.append("%n%-").append(cNeededForGen + 1).append("s | %-").append(cNeededForBest)
					.append("s | %-").append(cNeededForWorst).append("s | %-").append(cNeededForAvg).append("s | %-")
					.append(cNeededForSum).append("s");

			if (population.size() > 1) {
				resultHeaderSb.append(" ||");
				for (int i = 0; i < population.size(); i++) {
					int cNeeded = Math.max(cNeededForBest, ("Best(" + i + ")").length());
					resultHeaderSb.append("| %").append(cNeeded).append("s ");
				}
			}

			// Print header. Printf accepts arrays only if they are not mixed. Wrap
			// everything into a single array

			Object[] obj = new Object[5 + population.size()];
			obj[0] = "Gen";
			obj[1] = "Best";
			obj[2] = "Worst";
			obj[3] = "Average";
			obj[4] = "Sum";

			if (population.size() > 1) {
				for (int i = 0; i < population.size(); i++) {
					obj[5 + i] = "Best(" + i + ")";
				}
				int lengthUntilSubpopulation = cNeededForGen + cNeededForBest + cNeededForWorst + cNeededForAvg
						+ cNeededForSum + 12 + 1;
				System.out.printf("%n%-" + lengthUntilSubpopulation + "s ||| %s", "Overall Population",
						"Subpopulation");
			}

			resultHeaderSb.append("%n");

			System.out.printf(resultHeaderSb.toString(), obj);
		}

		return resultFormatSb.toString();
	}

	/*
	 * Getters and setters. How extensive do we want to be?
	 */

	/**
	 * The number of sub populations this ga is configured to use.
	 * 
	 * @return the sub population count
	 */
	public int getSubPopulationCount() {
		return population.size();
	}

	/*
	 * Multi threading
	 */

	/**
	 * Wrapper class representing the tasks to perform a new generation for each sub
	 * population.
	 * 
	 * @author Kilian
	 *
	 */
	private class PerformGeneration implements Callable<Double> {
		private int populationIndex;

		private PerformGeneration(int populationIndex) {
			this.populationIndex = populationIndex;
		}

		@Override
		public Double call() throws Exception {
			return performGeneration(populationIndex);
		}
	}

	/*
	 * Builder
	 */

	/**
	 * @return a builder to build a {@link GeneticAlgorithm} object
	 */
	public static IInitialPopulationStage builder() {
		return new Builder();
	}

	/**
	 * First stage of the genetic algorithm builder with exclusive but mandatory
	 * settings regarding how the initial population is supplied.
	 * 
	 * @author Kilian
	 *
	 */
	public interface IInitialPopulationStage {

		/**
		 * Set the initial population for the genetic algorithm.
		 * 
		 * @param initialPopulation The first set of individuals used to generate
		 *                          children
		 * @return a builder in the stop criteria stage
		 */
		public IStopCriteriaStage withInitialPopulation(Individual[] initialPopulation);

		/**
		 * Sets a prototype for the genetic algorithm which will take care of generating
		 * an initial population.
		 * 
		 * @param prototype used to generate new individuals at startup
		 * @return a builder in the stop criteria stage
		 */
		public IStopCriteriaStage withPrototype(IndividualPrototype prototype);
	}

	/**
	 * The stop criteria stage of the builder takes care when the GA shall stop
	 * executing it's calculation process.
	 * 
	 * @author Kilian
	 *
	 */
	public interface IStopCriteriaStage {

		/**
		 * Set how many generations the ga creates if no other stop criteria is reached.
		 * It is recommended to start with <code>x * 200</code> generations with x being
		 * the count of variables being optimized in the utility function. More
		 * generations increase computation time but may result in a better result.
		 * 
		 * @param maxGenerationCount the amount of generations calculated before
		 *                           aborting.
		 * @return the builder
		 */
		public IStopCriteriaStage withMaxGenerationCount(int maxGenerationCount);

		/**
		 * Set the maximum execution time before the algorithm halts computation.
		 * 
		 * If no value is supplied no time constraint is imposed.
		 * 
		 * @param maxExecutionTime in timeunits
		 * @param timeunit         the timunit of the execution time
		 * @return the builder
		 */
		public IStopCriteriaStage withMaxExecutionTime(long maxExecutionTime, TimeUnit timeunit);

		/**
		 * Set the fitness the genetic algorithm will consider a solution good enough to
		 * halt calculation. The required fitness greatly depends on the fitness
		 * function and shall be adjusted accordingly.
		 * 
		 * A smaller fitness values indicates a better solution with a value of 0 being
		 * the optimal solution.
		 * 
		 * Be aware that fitness functions are minimized and smaller values indicate
		 * better results.
		 * 
		 * A default of <code>1e-3</code> is assumed if no value is present.
		 * 
		 * @param targetFitness
		 * @return the builder
		 */
		public IStopCriteriaStage withTargetFitness(double targetFitness);

		/**
		 * Sets the count after how many generations the ga shall stop calculation if no
		 * better solution was found. A negative value indicates that the ga shall not
		 * consider stall generations. Default : -1
		 * 
		 * @param maxStaleGenerations the amount of generations after which the ga stops
		 *                            executing if no better solution was found
		 * @return
		 */
		public IStopCriteriaStage withMaxStaleGenerations(int maxStallGenerations);

		/**
		 * Return a builder with advanced configuration settings concerning the
		 * population composition.
		 * 
		 * @return the builder
		 */
		public IPopulationCompositeStage population();

		/**
		 * Optional: move to the advanced building stage. The advanced build stage lets
		 * you choose global settings which will be applied to each sub population.
		 * <p>
		 * If you don't need to change any advanced settings you may either directly
		 * build the algorithm by calling {@link #build()} or configure the individual
		 * sub populations {@link #migration(int)}.
		 * 
		 * @return the builder
		 */
		public IBuildStage advanced();

		/**
		 * Optional configure sub population to enable multi threading. The migration
		 * stage lets you fully customize the number of sub populations as well as it's
		 * individual settings. For the best performance it's usually good to choose the
		 * number of populations equals to your cpu cores.
		 * 
		 * @param migrationInterval the number of generations to pass until migration
		 *                          takes place between different populations.
		 *                          <p>
		 *                          Example a value of 200 indicates that migration
		 *                          takes place at the 200, 400 ... generation.
		 * @return the builder
		 */
		public IMigrationStage migration(int migrationInterval);

		/**
		 * Use the current settings and create a genetic algorithm object
		 * 
		 * @return The genetic algorithm object
		 */
		public GeneticAlgorithm build();
	}

	/**
	 * 
	 * General population settings applied to every sub population handling the
	 * composition of a population.
	 * 
	 * @author Kilian
	 *
	 */
	public interface IPopulationCompositeStage {

		/**
		 * Set the number of individuals present in each sub population. A higher
		 * population count will lead to a more thoroughly exploration of the search
		 * place but carries a performance penalty.
		 * <p>
		 * Usually a tradeoff has to be made between number of generations calculated
		 * and number of individuals per generation calculated.
		 * 
		 * <p>
		 * <b>Default value:</b> 20
		 * 
		 * @param populationCount The number of individuals computed per generation per
		 *                        sub population
		 * @return the builder
		 */
		public IPopulationCompositeStage withPopulationCount(int populationCount);

		/**
		 * Set the elite fraction of the algorithm. The elite fraction defines how many
		 * percent of the best offsprings of last generation are transfered into the new
		 * generation without altering.
		 * <p>
		 * The population is a composite of elite children, mutation children and
		 * crossover children. Constraint: the elite fraction + crossover fraction may
		 * not be greater than 1
		 * 
		 * <p>
		 * The number of mutation children is the difference between the crossover
		 * fraction and the elite fraction.
		 * </p>
		 * 
		 * <p>
		 * <b>Default value:</b> 0.05f
		 * 
		 * @param eliteFraction the percent of elite children of each generation based
		 *                      on the total population
		 * @return the builder
		 */
		public IPopulationCompositeStage withEliteFraction(float eliteFraction);

		/**
		 * Set the crossover fraction of the algorithm. The crossover fraction defines
		 * how many percent of the offsprings are generated via a crossover operation.
		 * <p>
		 * The population is a composite of elite children, mutation children and
		 * crossover children. Constraint: the elite fraction + crossover fraction may
		 * not be greater than 1.
		 * 
		 * <p>
		 * The number of mutation children is the difference between the crossover
		 * fraction and the elite fraction.
		 * </p>
		 * 
		 * <p>
		 * <b>Default value:</b> 0.8f
		 * 
		 * @param crossoverFraction the percent of crossover children in each generation
		 *                          based on the total population.
		 * @return the builder
		 */
		public IPopulationCompositeStage withCrossoverFraction(float crossoverFraction);

		/**
		 * Optional: move to the advanced building stage. The advanced build stage lets
		 * you choose global settings which will be applied to each sub population.
		 * <p>
		 * If you don't need to change any advanced settings you may either directly
		 * build the algorithm by calling {@link #build()} or configure the individual
		 * sub populations {@link #migration(int)}.
		 * 
		 * @return the builder
		 */
		public IBuildStage advanced();

		/**
		 * Optional configure sub population to enable multi threading. The migration
		 * stage lets you fully customize the number of sub populations as well as it's
		 * individual settings. For the best performance it's usually good to choose the
		 * number of populations equals to your cpu cores.
		 * 
		 * @param migrationInterval the number of generations to pass until migration
		 *                          takes place between different populations.
		 *                          <p>
		 *                          Example a value of 200 indicates that migration
		 *                          takes place at the 200, 400 ... generation.
		 * @return the builder
		 */
		public IMigrationStage migration(int migrationInterval);

		/**
		 * Use the current settings and create a genetic algorithm object
		 * 
		 * @return The genetic algorithm object
		 */
		public GeneticAlgorithm build();
	}

	/**
	 * The general population reproduction settings. Everything defined here will be
	 * applied to each subpopulation unless overwritten.
	 */
	public interface IBuildStage {

		/**
		 * Set the selection strategy of the genetic algorithm.
		 * 
		 * <p>
		 * The selection strategy defines how likely certain parents from the last
		 * generation are selected to pass on their genes for the next generation. The
		 * selection criteria defines how many weak offsprings may have a chance to
		 * survive to boost exploration.
		 * 
		 * <p>
		 * Selection strategies base their decision on the scaled fitness value computed
		 * by
		 * {@link com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy
		 * FitnessScalingStrategy}
		 * </p>
		 * 
		 * <p>
		 * <b>Default Value:</b>
		 * {@link com.github.kilianB.geneticAlgorithm.selection.StochasticUniform
		 * StochasticUniform}
		 * </p>
		 * 
		 * @param selectionStrategy The selection strategy used to select the parent
		 *                          candidates.
		 * @return the builder
		 */
		public IBuildStage withSelectionStrategy(SelectionStrategy selectionStrategy);

		/**
		 * Set the scaling strategy of the genetic algorithm.
		 * 
		 * <p>
		 * The scaling strategy is used to scale the fitness values of individuals to a
		 * normalized range of [0-numParentsNeeded] in order for the genetic algorithm
		 * to define which parents are used as offsprings for the next generation.
		 * 
		 * <p>
		 * A fitness scaling is appropriate to discriminate fit individuals giving
		 * weaker ones a chance to survive until the next generation.
		 * 
		 * <p>
		 * <b>Default Value:</b>
		 * {@link com.github.kilianB.geneticAlgorithm.fitnessScaling.RankScaling
		 * RankScaling}
		 * </p>
		 * 
		 * @param scalingStrategy the scaling strategy used to normalize the fitness
		 *                        value of the individuals
		 * @return the builder
		 */
		public IBuildStage withScalingStrategy(FitnessScalingStrategy scalingStrategy);

		/**
		 * Set the crossover strategy used during crossover operation.
		 * 
		 * <p>
		 * Crossover strategies can be classified into 2 categories.
		 * {@link com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete
		 * Discrete strategies} (categorical and numerical domains) and
		 * {@link com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy
		 * Fuzzy strategies} (numerical domain) defining which gene is taken from which
		 * parent during a crossover operation.
		 * </p>
		 * 
		 * <p>
		 * <b>Default Value:</b>
		 * {@link com.github.kilianB.geneticAlgorithm.crossover.ScatteredDiscrete} with
		 * 2 parents
		 * </p>
		 * 
		 * @param crossoverStrategy The strategy used during crossover operation.
		 * @return the builder
		 */
		public IBuildStage withCrossoverStrategy(CrossoverStrategy crossoverStrategy);

		/**
		 * Return a builder with advanced configuration settings concerning the
		 * population composition.
		 * 
		 * @return the builder
		 */
		public IPopulationCompositeStage population();

		/**
		 * The mutation probability defines how likely it is that an individual gene is
		 * altered during mutation.
		 * <p>
		 * No guarantee is given since each individual is responsible to implement this
		 * contract on it's own. In some cases an individual might deviate from the
		 * probability score if it can assess that a certain gene might be more
		 * profitable to mutate.
		 * <p>
		 * 
		 * Be aware that contrary to the most GA's used elsewhere this algorithms
		 * usually requires a higher probability to produce reasonable results due to
		 * the fact that mutation only takes place on a fraction of the entire
		 * population (1 - crossover fraction - elite fraction)
		 * 
		 * <p>
		 * <b>Default Value:</b> 0.1
		 * </p>
		 * 
		 * @param mutationProbability the probability that a certain gene will be
		 *                            modified
		 * @return the builder
		 */
		public IBuildStage withMutationProbability(double mutationProbability);

		/**
		 * Perform additional mutations on all duplicated individuals in the population
		 * at the end of each generation. This usually leads to a decrease in
		 * generations needed and an overall speed up in execution time.
		 * <p>
		 * The diversity of the population increases and does not converge as quickly.
		 * Removing duplicates might not be optimal for some discrete problems
		 * especially with a limited search space.
		 * <p>
		 * 
		 * <b>Specifics:</b>
		 * <p>
		 * <ul>
		 * <li>The mutation probability increases linearly from [mutationProbability -
		 * 1] with every fail attempt</li>
		 * <li>The scale factor is clamped at a minimum of 0.01</li>
		 * </ul>
		 * 
		 * Older aged duplicates are privileged and are protected as long as a younger
		 * clone exists allowing to perform age based scaling in future generations.
		 * 
		 * *
		 * <p>
		 * <b>Default Value:</b> true with a cutoff of 10
		 * </p>
		 * 
		 * @param forceCloneMutation    Enable/Disable additional mutation
		 * @param mutationAttemptCutoff how many times shall we attempt to mutated an
		 *                              individual before it is simply carried over in
		 *                              the next generation even though it still is a
		 *                              clone. Depending on how expensive mutations are
		 *                              and how tight the search space is 10 is a good
		 *                              starting value.
		 * @return the builder
		 */
		public IBuildStage withForceCloneMutation(boolean forceCloneMutation, int mutationAttemptCutoff);

		/**
		 * Set the mutation scaling strategy of the genetic algorithm.
		 * 
		 * <p>
		 * The mutation scaling strategy defines how the scale variable passed to the
		 * individuals mutation function changes with the progress of the algorithm. The
		 * scale value indicates how drastic a mutation should alter one of the genes.
		 * <p>
		 * During the start of the algorithm a greater mutation is desired to explore a
		 * broad area of the problem domain, as times go one the algorithm should focus
		 * on a more specific region favoring smaller changes.
		 * 
		 * <p>
		 * Some strategies may adapt the scale factor based on how long no better result
		 * was found.
		 * 
		 * *
		 * <p>
		 * <b>Default Value:</b>
		 * {@link com.github.kilianB.geneticAlgorithm.mutationScaling.MutationScalingStrategy#RICHARD}
		 * </p>
		 * 
		 * @param mutationScalingStrategy the mutation strategy used by this genetic
		 *                                algorithm
		 * @return the builder
		 */
		public IBuildStage withMutationScalingStrategy(MutationScalingStrategy mutationScalingStrategy);

		/**
		 * Optional configure sub population to enable multi threading. The migration
		 * stage lets you fully customize the number of sub populations as well as it's
		 * individual settings. For the best performance it's usually good to choose the
		 * number of populations equals to your cpu cores.
		 * 
		 * @param migrationInterval the number of generations to pass until migration
		 *                          takes place between different populations.
		 *                          <p>
		 *                          Example a value of 200 indicates that migration
		 *                          takes place at the 200, 400 ... generation.
		 * @return the builder
		 */
		public IMigrationStage migration(int migrationInterval);

		/**
		 * Use the current settings and create a genetic algorithm object
		 * 
		 * @return The genetic algorithm object
		 */
		public GeneticAlgorithm build();
	}

	/**
	 * The migration stage defines how migration shall be performed.
	 * 
	 * @author Kilian
	 *
	 */
	public interface IMigrationStage {

		/**
		 * Sets the migration strategy used to determine which individuals of a
		 * population are selected as potential migration candidates during migration.
		 * 
		 ** <p>
		 * <b>Default Value:</b>
		 * {@link com.github.kilianB.geneticAlgorithm.migration.strategy.Elitism}
		 * </p>
		 * 
		 * @param migrationStrategy used to determine how to select potential migration
		 *                          candidates
		 * @return the builder
		 */
		public IMigrationStage withMigrationStrategy(MigrationStrategy migrationStrategy);

		/**
		 * Define how migration takes place. The migration process dictates the source
		 * and target population for the migration process.
		 * 
		 * <p>
		 * If the sub populations all run with the same settings NetworkMigration
		 * usually is the way to go. If settings are individually tuned it might be
		 * desired to only allow migration in a certain direction.
		 * 
		 * <p>
		 * <b>Default Value:</b>
		 * {@link com.github.kilianB.geneticAlgorithm.migration.direction.NetworkMigration}
		 * </p>
		 * 
		 * @param migrationProcess used to determine how migration takes place
		 * @return the builder
		 */
		public IMigrationStage withMigrationProcess(MigrationProcess migrationProcess);

		/**
		 * Creates a new sub population which can be configured manually. This method
		 * can be invoked multiple times adding a new sub population each time.
		 * 
		 * <p>
		 * The added sub population copies all global settings set in the pre migration
		 * builder.
		 * 
		 * This method can be chained with {@link #withNewSubpopulations(int)}.
		 * 
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withNewSubpopulation();

		/**
		 * Creates n new sub populations. The added sub populations copies all global
		 * settings set in the pre migration builder.
		 * 
		 * <p>
		 * Subsequent calls to configure methods will only apply to one of the newly
		 * created populations (the last).
		 * 
		 * This method can be chained with {@link #withNewSubpopulations()}.
		 * 
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withNewSubpopulations(int subpopulations);

		/**
		 * Use the current settings and create a genetic algorithm object
		 * 
		 * @return The genetic algorithm object
		 */
		public GeneticAlgorithm build();
	}

	/**
	 * The migration individual builder is the final stage of the genetic algorithm
	 * configuration stage allowing to customize individual populations to use
	 * specific settings.
	 * 
	 * @author Kilian
	 *
	 */
	public interface IMigrationIndividualBuilder {

		/**
		 * Creates a new sub population which can be configured manually. This method
		 * can be invoked multiple times adding a new sub population each time.
		 * 
		 * <p>
		 * The added sub population copies all global settings set in the pre migration
		 * builder.
		 * 
		 * This method can be chained with {@link #withNewSubpopulations(int)}.
		 * 
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withNewSubpopulation();

		/**
		 * Creates n new sub populations. The added sub populations copies all global
		 * settings set in the pre migration builder.
		 * 
		 * <p>
		 * Subsequent calls to configure methods will only apply to one of the newly
		 * created populations (the last).
		 * 
		 * This method can be chained with {@link #withNewSubpopulations()}.
		 * 
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withNewSubpopulations(int subpopulations);

		/**
		 * Set the selection strategy for the last created sub population
		 * 
		 * <p>
		 * The selection strategy defines how likely certain parents from the last
		 * generation are selected to pass on their genes for the next generation. The
		 * selection criteria defines how many weak offsprings may have a chance to
		 * survive to boost exploration.
		 * 
		 * <p>
		 * Selection strategies base their decision on the scaled fitness value computed
		 * by
		 * {@link com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy
		 * FitnessScalingStrategy}
		 * </p>
		 * 
		 * <p>
		 * <b>Default Value:</b>
		 * {@link com.github.kilianB.geneticAlgorithm.selection.StochasticUniform
		 * StochasticUniform} if not previously altered.
		 * </p>
		 * 
		 * @param selectionStrategy The selection strategy used to select the parent
		 *                          candidates.
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withSelectionStrategy(SelectionStrategy selectionStrategy);

		/**
		 * Set the mutation scaling strategy for the last created sub population
		 * 
		 * <p>
		 * The mutation scaling strategy defines how the scale variable passed to the
		 * individuals mutation function changes with the progress of the algorithm. The
		 * scale value indicates how drastic a mutation should alter one of the genes.
		 * <p>
		 * During the start of the algorithm a greater mutation is desired to explore a
		 * broad area of the problem domain, as times go one the algorithm should focus
		 * on a more specific region favoring smaller changes.
		 * 
		 * <p>
		 * Some strategies may adapt the scale factor based on how long no better result
		 * was found.
		 * 
		 * *
		 * <p>
		 * <b>Default Value:</b>
		 * {@link com.github.kilianB.geneticAlgorithm.mutationScaling.MutationScalingStrategy#RICHARD}
		 * if not previously altered.
		 * </p>
		 * 
		 * @param mutationScalingStrategy the mutation strategy used by this genetic
		 *                                algorithm
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withScalingStrategy(FitnessScalingStrategy scalingStrategy);

		/**
		 * Set the crossover strategy used during crossover operation for the last
		 * created sub population
		 * 
		 * <p>
		 * Crossover strategies can be classified into 2 categories.
		 * {@link com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete
		 * Discrete strategies} (categorical and numerical domains) and
		 * {@link com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy
		 * Fuzzy strategies} (numerical domain) defining which gene is taken from which
		 * parent during a crossover operation.
		 * </p>
		 * 
		 * <p>
		 * <b>Default Value:</b>
		 * {@link com.github.kilianB.geneticAlgorithm.crossover.ScatteredDiscrete} with
		 * 2 parents if not previously altered.
		 * </p>
		 * 
		 * @param crossoverStrategy The strategy used during crossover operation.
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withCrossoverStrategy(CrossoverStrategy crossoverStrategy);

		/**
		 * Set the number of individuals present in the last created sub population. A
		 * higher population count will lead to a more thoroughly exploration of the
		 * search place but carries a performance penalty.
		 * <p>
		 * Usually a tradeoff has to be made between number of generations calculated
		 * and number of individuals per generation calculated.
		 * 
		 * <p>
		 * <b>Default value:</b> 20 if not previously altered.
		 * 
		 * @param populationCount The number of individuals computed per generation per
		 *                        sub population
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withPopulationCount(int populationCount);

		/**
		 * Set the elite fraction of the algorithm. The elite fraction defines how many
		 * percent of the best offsprings of last generation are transfered into the new
		 * generation without altering.
		 * <p>
		 * The population is a composite of elite children, mutation children and
		 * crossover children. Constraint: the elite fraction + crossover fraction may
		 * not be greater than 1
		 * 
		 * <b>Default:</b> 0.05f if not previously altered.
		 * 
		 * @param eliteFraction the new elite fraction
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withEliteFraction(float eliteFraction);

		/**
		 * Set the crossover fraction for the last generated sub population. The
		 * crossover fraction defines how many percent of the offsprings are generated
		 * via a crossover operation.
		 * <p>
		 * The population is a composite of elite children, mutation children and
		 * crossover children. Constraint: the elite fraction + crossover fraction may
		 * not be greater than 1.
		 * 
		 * <p>
		 * The number of mutation children is the difference between the crossover
		 * fraction and the elite fraction.
		 * </p>
		 * 
		 * <p>
		 * <b>Default value:</b> 0.8f if not previously altered.
		 * 
		 * @param crossoverFraction the percent of crossover children in each generation
		 *                          based on the total population.
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withCrossoverFraction(float crossoverFraction);

		/**
		 * Sets the mutation proability for the last generated sub population.
		 * <p>
		 * The mutation probability defines how likely it is that an individual gene is
		 * altered during mutation.
		 * <p>
		 * No guarantee is given since each individual is responsible to implement this
		 * contract on it's own. In some cases an individual might deviate from the
		 * probability score if it can assess that a certain gene might be more
		 * profitable to mutate.
		 * <p>
		 * 
		 * Be aware that contrary to the most GA's used elsewhere this algorithms
		 * usually requires a higher probability to produce reasonable results due to
		 * the fact that mutation only takes place on a fraction of the entire
		 * population (1 - crossover fraction - elite fraction)
		 * 
		 * <p>
		 * <b>Default Value:</b> 0.1 if not previously altered.
		 * </p>
		 * 
		 * @param mutationProbability the probability that a certain gene will be
		 *                            modified
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withMutationProbability(double mutationProbability);

		/**
		 * Set the mutation scaling strategy for the last generated sub population.
		 * 
		 * <p>
		 * The mutation scaling strategy defines how the scale variable passed to the
		 * individuals mutation function changes with the progress of the algorithm. The
		 * scale value indicates how drastic a mutation should alter one of the genes.
		 * <p>
		 * During the start of the algorithm a greater mutation is desired to explore a
		 * broad area of the problem domain, as times go one the algorithm should focus
		 * on a more specific region favoring smaller changes.
		 * 
		 * <p>
		 * Some strategies may adapt the scale factor based on how long no better result
		 * was found.
		 * 
		 * *
		 * <p>
		 * <b>Default Value:</b>
		 * {@link com.github.kilianB.geneticAlgorithm.mutationScaling.MutationScalingStrategy#RICHARD}
		 * </p>
		 * 
		 * @param mutationScalingStrategy the mutation strategy used by this genetic
		 *                                algorithm
		 * @return the builder
		 */
		public IMigrationIndividualBuilder withMutationScalingStrategy(MutationScalingStrategy mutationScalingStrategy);

		/**
		 * Use the current settings and create a genetic algorithm object
		 * 
		 * @return The genetic algorithm object
		 */
		public GeneticAlgorithm build();
	}

	/**
	 * Builder to build {@link GeneticAlgorithm}.
	 */
	public static final class Builder implements IInitialPopulationStage, IStopCriteriaStage, IPopulationCompositeStage,
			IBuildStage, IMigrationStage {

		// Algorithm specific values

		private Individual[] initialPopulation;
		private IndividualPrototype individualPrototype;

		// Algorithm stop criteria
		private int maxGenerationCount;
		private long maxExecutionTime = Long.MAX_VALUE;
		private double targetFitness = 1e-3;
		private int maxStaleGenerations = -1;

		// Multi threading - migration
		private int migrationInterval = Integer.MAX_VALUE;
		private MigrationStrategy migrationStrategy = new Elitism(2);
		private MigrationProcess migrationProcess = new NetworkMigration();

		// Everything below might be adjusted per generation

		// Population composition
		private int populationCount = 20;
		private float eliteFraction = 0.05f;
		private float crossoverFraction = 0.8f;

		// Advanced
		private SelectionStrategy selectionStrategy = new StochasticUniform();
		private FitnessScalingStrategy scalingStrategy = new RankScaling();
		private CrossoverStrategy crossoverStrategy = new ScatteredDiscrete(2);
		private double mutationProbability = 0.1;
		private boolean forceCloneMutation = true;
		private int mutationAttemptCutoff = 10;

		private MutationScalingStrategy mutationScalingStrategy = MutationScalingStrategy.RICHARD;

		// hide default constructor
		private Builder() {
		}

		@Override
		public IStopCriteriaStage withInitialPopulation(Individual[] initialPopulation) {
			this.initialPopulation = initialPopulation;
			// Assume default
			this.maxGenerationCount = initialPopulation[0].getVariableCount() * 200;
			this.populationCount = initialPopulation.length;
			return this;
		}

		@Override
		public IStopCriteriaStage withPrototype(IndividualPrototype prototype) {
			this.individualPrototype = prototype;

			int variableCount = prototype.createIndividual().getVariableCount();

			this.maxGenerationCount = variableCount * 200;
			return this;
		}

		@Override
		public IStopCriteriaStage withMaxGenerationCount(int maxGenerationCount) {
			this.maxGenerationCount = maxGenerationCount;
			return this;
		}

		@Override
		public IStopCriteriaStage withMaxExecutionTime(long maxExecutionTime, TimeUnit timeunit) {
			this.maxExecutionTime = timeunit.toMillis(maxExecutionTime);
			return this;
		}

		@Override
		public IStopCriteriaStage withTargetFitness(double targetFitness) {
			this.targetFitness = targetFitness;
			return this;
		}

		@Override
		public IStopCriteriaStage withMaxStaleGenerations(int maxStaleGenerations) {
			this.maxStaleGenerations = maxStaleGenerations;
			return this;
		}

		@Override
		public IBuildStage advanced() {
			return this;
		}

		@Override
		public IBuildStage withSelectionStrategy(SelectionStrategy selectionStrategy) {
			this.selectionStrategy = selectionStrategy;
			return this;
		}

		@Override
		public IBuildStage withScalingStrategy(FitnessScalingStrategy scalingStrategy) {
			this.scalingStrategy = scalingStrategy;
			return this;
		}

		@Override
		public IBuildStage withCrossoverStrategy(CrossoverStrategy crossoverStrategy) {
			this.crossoverStrategy = crossoverStrategy;
			return this;
		}

		@Override
		public IPopulationCompositeStage withPopulationCount(int populationCount) {
			this.populationCount = populationCount;
			return this;
		}

		@Override
		public IPopulationCompositeStage withEliteFraction(float eliteFraction) {
			this.eliteFraction = eliteFraction;
			return this;
		}

		@Override
		public IPopulationCompositeStage withCrossoverFraction(float crossoverFraction) {
			this.crossoverFraction = crossoverFraction;
			return this;
		}

		@Override
		public IPopulationCompositeStage population() {
			return this;
		}

		@Override
		public IBuildStage withMutationProbability(double mutationProbability) {
			this.mutationProbability = mutationProbability;
			return this;
		}

		@Override
		public IBuildStage withForceCloneMutation(boolean forceCloneMutation, int mutationAttemptCutoff) {
			this.forceCloneMutation = forceCloneMutation;
			this.mutationAttemptCutoff = mutationAttemptCutoff;
			return this;
		}

		@Override
		public IMigrationStage migration(int migrationInterval) {
			this.migrationInterval = migrationInterval;
			return this;
		}

		@Override
		public IMigrationStage withMigrationStrategy(MigrationStrategy migrationStrategy) {
			this.migrationStrategy = migrationStrategy;
			return this;
		}

		@Override
		public IMigrationStage withMigrationProcess(MigrationProcess migrationProcess) {
			this.migrationProcess = migrationProcess;
			return this;
		}

		@Override
		public IBuildStage withMutationScalingStrategy(MutationScalingStrategy mutationScalingStrategy) {
			this.mutationScalingStrategy = mutationScalingStrategy;
			return this;
		}

		@Override
		public GeneticAlgorithm build() {
			return new MigrationBuilder(this).build();
		}

		@Override
		public IMigrationIndividualBuilder withNewSubpopulation() {

			IMigrationIndividualBuilder builder = new MigrationBuilder(this);
			builder.withNewSubpopulation();
			return builder;
		}

		@Override
		public IMigrationIndividualBuilder withNewSubpopulations(int subpopulations) {
			IMigrationIndividualBuilder builder = new MigrationBuilder(this);
			builder.withNewSubpopulations(subpopulations);
			return builder;
		}
	}

	/**
	 * The migration builder allows to add and customize new subpopulations
	 * individually.
	 * 
	 * @author Kilian
	 *
	 */
	public static final class MigrationBuilder implements IMigrationIndividualBuilder {

		private Builder internalBuilder;

		int curIndex = -1;

		private List<SelectionStrategy> selectionStrategy = new ArrayList<>();
		private List<FitnessScalingStrategy> scalingStrategy = new ArrayList<>();
		private List<CrossoverStrategy> crossoverStrategy = new ArrayList<>();

		private List<Integer> populationCount = new ArrayList<>();
		private List<Float> eliteFraction = new ArrayList<>();
		private List<Float> crossoverFraction = new ArrayList<>();

		private List<MutationScalingStrategy> mutationScalingStrategy = new ArrayList<>();
		private List<Double> mutationProbability = new ArrayList<>();

		private MigrationBuilder(Builder internalBuilder) {
			this.internalBuilder = internalBuilder;
		}

		@Override
		public IMigrationIndividualBuilder withNewSubpopulation() {
			curIndex++;
			// Push defaults
			selectionStrategy.add(internalBuilder.selectionStrategy);
			scalingStrategy.add(internalBuilder.scalingStrategy);
			crossoverStrategy.add(internalBuilder.crossoverStrategy);

			populationCount.add(internalBuilder.populationCount);
			eliteFraction.add(internalBuilder.eliteFraction);
			crossoverFraction.add(internalBuilder.crossoverFraction);

			mutationProbability.add(internalBuilder.mutationProbability);
			mutationScalingStrategy.add(internalBuilder.mutationScalingStrategy);

			return this;
		}

		@Override
		public IMigrationIndividualBuilder withNewSubpopulations(int subpopulations) {
			for (int i = 0; i < subpopulations; i++) {
				withNewSubpopulation();
			}
			return this;
		}

		@Override
		public IMigrationIndividualBuilder withSelectionStrategy(SelectionStrategy selectionStrategy) {
			this.selectionStrategy.set(curIndex, selectionStrategy);
			return this;
		}

		@Override
		public IMigrationIndividualBuilder withScalingStrategy(FitnessScalingStrategy scalingStrategy) {
			this.scalingStrategy.set(curIndex, scalingStrategy);
			return this;
		}

		@Override
		public IMigrationIndividualBuilder withCrossoverStrategy(CrossoverStrategy crossoverStrategy) {
			this.crossoverStrategy.set(curIndex, crossoverStrategy);
			return this;
		}

		@Override
		public IMigrationIndividualBuilder withPopulationCount(int populationCount) {
			this.populationCount.set(curIndex, populationCount);
			return this;
		}

		@Override
		public IMigrationIndividualBuilder withEliteFraction(float eliteFraction) {
			this.eliteFraction.set(curIndex, eliteFraction);
			return this;
		}

		@Override
		public IMigrationIndividualBuilder withCrossoverFraction(float crossoverFraction) {
			this.crossoverFraction.set(curIndex, crossoverFraction);
			return this;
		}

		@Override
		public IMigrationIndividualBuilder withMutationProbability(double mutationProbability) {
			this.mutationProbability.set(curIndex, mutationProbability);
			return this;
		}

		@Override
		public GeneticAlgorithm build() {
			if (curIndex == -1) {
				withNewSubpopulation();
			}
			return new GeneticAlgorithm(this);
		}

		@Override
		public IMigrationIndividualBuilder withMutationScalingStrategy(
				MutationScalingStrategy mutationScalingStrategy) {
			this.mutationScalingStrategy.set(curIndex, mutationScalingStrategy);
			return this;
		}

	}

	/**
	 * 
	 */
	public void printCurrentPopulation() {
		System.out.println(Arrays.toString(population.get(0)));
	}

}
