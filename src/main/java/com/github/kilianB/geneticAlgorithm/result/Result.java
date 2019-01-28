package com.github.kilianB.geneticAlgorithm.result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * A result object contains information tracking the progress of the genetic
 * algorithm's computation as well as the generated results. The content is
 * mutable and updated along the line of execution. It's up to the user to cache
 * data if necessary.
 * 
 * <p>
 * Information about sub population
 * 
 * <p>
 * Exactly one result object will be created for each call to
 * {@link com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int)}.
 * The result object passed to the event listener and returned by the calculate
 * method reference the same object.
 * 
 * <p>
 * Result objects usually contains every nth generation as specified in the
 * {@link com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int)
 * calculate(n)} call, the intial population with a generation index of -1 as
 * well as the very last generation created.
 * 
 * @author Kilian
 *
 */
public class Result {

	/** Delimiter used for csv file export */
	private static final String DELIMITER = ";";

	/**
	 * Termination reasons indicating state of the ga
	 */
	public enum TerminationReason {
		/**
		 * Not terminated yet. Intermediate result
		 */
		None,
		/**
		 * GA timed out due to max execution time
		 */
		Runtime,
		/**
		 * Fitness goal reached
		 */
		Fitness,
		/**
		 * Max generation reached
		 */
		Generation,
		/**
		 * Generations did not improve for max staleness generations
		 */
		Staleness,
		/**
		 * User has issued a manual interrupt
		 */
		Interrupted,
		/**
		 * The X generations the user requested to be calculated have passed
		 */
		GenerationStep,
		/**
		 * An exception took place during generation performance. The user should
		 * investigate the log and determine if the returned results are usable.
		 */
		Exception
	}

	// GA Information

	/** Current state of the genetic algorithm */
	protected TerminationReason terminationResult = TerminationReason.None;
	/** Time in ms the ga performed calculation so far! */
	protected long executionTime;
	/** Current last added generation number */
	protected int generations;

	// Info about the history

	/** Every recorded individual */
	protected LinkedHashMap<Integer, ArrayList<Individual[]>> fullHistory = new LinkedHashMap<>();

	/** Combined statistics over all sub populations */
	protected LinkedHashMap<Integer, DoubleSummaryStatistics> generationStatistics = new LinkedHashMap<>();
	/** Sub population specific statistics */
	protected LinkedHashMap<Integer, DoubleSummaryStatistics[]> generationStatisticsSubPopulation = new LinkedHashMap<>();
	/** Exception reference thrown during ga execution */
	protected Exception exception;
	/**
	 * Depending on the ga.calculate(n) method only every nth generation is saved.
	 * Save the n.
	 */
	protected int generationStep;

	/**
	 * @param recordGeneration Indicates which generations are included in the
	 *                         result object and trigger the result listeners.
	 *                         Setting this values does not change any behavior. It
	 *                         simply stores the value submitted to the genetic
	 *                         algorithm.
	 *                         <p>
	 *                         <b>Example:</b>
	 *                         <ul>
	 *                         <li>A value of 1 will return every generation
	 *                         created</li>
	 *                         <li>A value of 10 will return every 10th
	 *                         generation</li>
	 *                         <li>A value of 0 or smaller will return only the
	 *                         final generation. (recommended if you are not
	 *                         interested in further statistics but just the final
	 *                         result)</li>
	 *                         </ul>
	 * 
	 *                         The generation triggering a stop condition will
	 *                         <b>always</b> be included in the result object. Be
	 *                         aware that holding a reference to each and any
	 *                         individual generated during the computation may
	 *                         quickly accumulate memory usage.
	 *                         <p>
	 * @param generationStep
	 */
	public Result(int generationStep) {
		this.generationStep = generationStep;
	}

	/**
	 * 
	 * Add a new generation to this result object. Generations should be added in
	 * ascending order.
	 * 
	 * <p style="color:red;">
	 * FOR INTERNAL USE ONLY. Usually there is no need to set any fields of a result
	 * object manually. The result object is propagated to event listener altering
	 * any field may lead to unexpected behavior.
	 * </p>
	 * 
	 * @param generation The current generation number
	 * @param population The individual population
	 * @param runtime    the time in ms passed since the beginning of computation
	 */
	@Deprecated
	public void addGeneration(int generation, ArrayList<Individual[]> population, long runtime) {

		// Generate statistics
		DoubleSummaryStatistics summary = null; // Summary over the entire population
		DoubleSummaryStatistics[] summaryForSubPopulation = new DoubleSummaryStatistics[population.size()];

		for (int i = 0; i < population.size(); i++) {
			summaryForSubPopulation[i] = Arrays.stream(population.get(i)).mapToDouble(ind -> ind.getFitness())
					.summaryStatistics();
			if (i == 0) {
				// Deep copy
				summary = new DoubleSummaryStatistics(summaryForSubPopulation[i].getCount(),
						summaryForSubPopulation[i].getMin(), summaryForSubPopulation[i].getMax(),
						summaryForSubPopulation[i].getSum());
			} else {
				summary.combine(summaryForSubPopulation[i]);
			}
		}

		fullHistory.put(generation, new ArrayList<>(population));
		generationStatisticsSubPopulation.put(generation, summaryForSubPopulation);
		generationStatistics.put(generation, summary);
		this.generations = generation;
		
		if(runtime < this.executionTime) {
			throw new IllegalStateException("Runtime can not decrease");
		}
		this.executionTime = runtime;
	}

	/**
	 * Set the termination reason of this result object. The termination reason
	 * describes the current state of the genetic algorithm.
	 * 
	 * <p style="color:red;">
	 * FOR INTERNAL USE ONLY. Usually there is no need to set any fields of a result
	 * object manually. The result object is propagated to event listener altering
	 * any field may lead to unexpected behavior.
	 * </p>
	 * 
	 * @param reason The new termination reason
	 */
	@Deprecated
	public void setTerminationReason(TerminationReason reason) {
		this.terminationResult = reason;
	}

	/**
	 * Set the exception reference of this result object defining due to which
	 * exception the calculation of the genetic algorithm was aborted. A exception
	 * must be set if the termination reason is equals
	 * {@link TerminationReason#Exception}.
	 * 
	 * <p style="color:red;">
	 * FOR INTERNAL USE ONLY. Usually there is no need to set any fields of a result
	 * object manually. The result object is propagated to event listener altering
	 * any field may lead to unexpected behavior.
	 * </p>
	 * 
	 * @param e The exeception thrown during the genetic algorithms calculation
	 */
	@Deprecated
	public void setException(Exception e) {
		this.exception = e;
	}

	/**
	 * The termination reason describes the current state of the genetic algorithm.
	 * 
	 * <p>
	 * The state defines if the computation is still ongoing
	 * ({@link TerminationReason#None} or if the ga finished due to one of the stop
	 * conditions reached.
	 * 
	 * @return The current state of the algorithm.
	 */
	public TerminationReason getTerminationReason() {
		return terminationResult;
	}

	/**
	 * Return the best individual produced so far. The best individual is guaranteed
	 * to be present in the last generated population. Every sub population is
	 * queried for it's best individual and the first individual found whose fitness
	 * value matches the getSummary().getMin() value is returned.
	 * 
	 * @return The best individual with the lowest fitness score of the most current
	 *         generation
	 */
	public Individual getBestResult() {
		// Check which sub population contains the best individual
		double bestFitness = getFitness();

		var subSum = generationStatisticsSubPopulation.get(generations);
		for (int i = 0; i < subSum.length; i++) {
			if (subSum[i].getMin() == bestFitness) {
				return getGeneration(generations, i)[0];
			}
		}
		throw new IllegalStateException("Could not find the best Individual. This error should not"
				+ "have happened. Maybe the result object is empty?");
	}

	/**
	 * Retrieve a summary of the fitness values of the most recent population. This
	 * summary covers aggregated statistics covering all sup populations.
	 * <p>
	 * This call is equivalent to {@code r.getSummary(r.getGenerationCount());}
	 * 
	 * @return A summary object describing the most recent generation
	 */
	public DoubleSummaryStatistics getSummary() {
		return generationStatistics.get(generations);
	}

	/**
	 * Retrieve a summary of the fitness values of the population generated in the
	 * specified generation.This summary covers aggregated statistics covering all
	 * sup populations.
	 * 
	 * @param generation the generation number [0 - currentGeneration] or -1 for the
	 *                   initial population. Lower numbers represent older
	 *                   generations
	 * 
	 * @return A summary object describing the most recent generation
	 */
	public DoubleSummaryStatistics getSummary(int generation) {
		return generationStatistics.get(generation);
	}

	/**
	 * Retrieve a summary of the fitness values of the population generated in the
	 * specified generation.This summary allows to access statistics describing
	 * individual sub populations.
	 * <p>
	 * To retrieve information about the most current generation call
	 * {@code r.getSummarySubPopulation(r.getGenerationCount(),subIndex);}
	 * 
	 * @param generation         the generation number [0 - currentGeneration] or -1
	 *                           for the initial population. Lower numbers represent
	 *                           older generations
	 * @param subPopulationIndex the index of the sub population.
	 * 
	 * @return A summary object describing the most recent generation
	 */
	public DoubleSummaryStatistics getSummarySubPopulation(int generation, int subPopulationIndex) {
		return generationStatisticsSubPopulation.get(generation)[subPopulationIndex];
	}

	/**
	 * Get the fitness of the currently best individual
	 * 
	 * @return the best fitness of the entire population
	 */
	public double getFitness() {
		try {
			return generationStatistics.get(generations).getMin();
		}catch(NullPointerException n) {
			throw new IllegalStateException("Can't retrieve fitness of emptry result object");
		}
		
	}

	/**
	 * Save the content of the result object to csv files for further external
	 * statistical analysis with ";" as default delimiter used. The population file
	 * contains information (fitness values, age and usually variable states) of
	 * every individuals in each recorded generation. The summary file contains
	 * min,max,average,sums of the fitness value of each recorded generation.
	 * <p>
	 * 
	 * <b>Population file layout:</b>
	 * 
	 * <pre>
	 * generation, subPopulationIndex, min, max, average, sum
	 * </pre>
	 * 
	 * For each generation an entry with subPopulationIndex -1 preccedes the list
	 * indicating a summary over all sub population.
	 * <p>
	 * 
	 * <b>Summary file layout:</b>
	 * 
	 * Each individual:
	 * 
	 * <pre>
	 * generation, subPopulationIndex, token[0] ... token[n]
	 * </pre>
	 * 
	 * tokens* as returned by {@link Individual#toCSV()}
	 * 
	 * 
	 * @param populationFile location the population file will be saved to
	 * @param summaryFile    location the summary file will be saved to
	 * @throws IOException thrown if an IO error occurs
	 */
	public void toFile(File populationFile, File summaryFile) throws IOException {
		toFile(populationFile, summaryFile, DELIMITER);
	}

	/**
	 * Save the content of the result object to csv files for further external
	 * statistical analysis. The population file contains information (fitness
	 * values, age and usually variable states) of every individuals in each
	 * recorded generation. The summary file contains min,max,average,sums of the
	 * fitness value of each recorded generation.
	 * <p>
	 * 
	 * <b>Population file layout:</b>
	 * 
	 * <pre>
	 * generation, subPopulationIndex, min, max, average, sum
	 * </pre>
	 * 
	 * For each generation an entry with subPopulationIndex -1 preccedes the list
	 * indicating a summary over all sub population.
	 * <p>
	 * 
	 * <b>Summary file layout:</b>
	 * 
	 * Each individual:
	 * 
	 * <pre>
	 * generation, subPopulationIndex, token[0] ... token[n]
	 * </pre>
	 * 
	 * tokens* as returned by {@link Individual#toCSV()}
	 * 
	 * 
	 * @param populationFile location the population file will be saved to
	 * @param summaryFile    location the summary file will be saved to
	 * @param csvDelimiter   delimiter between tokens in the csv files
	 * @throws IOException thrown if an IO error occurs
	 */
	public void toFile(File populationFile, File summaryFile, String csvDelimiter) throws IOException {

		// Remove files if present
		populationFile.delete();
		summaryFile.delete();

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(summaryFile))) {

			for (var entry : generationStatistics.entrySet()) {

				StringBuffer sb = new StringBuffer(100);
				// Construct a line at a time
				int generation = entry.getKey();
				var stat = entry.getValue();
				sb.append(generation).append(csvDelimiter)
						// Sub population index of overview is -1
						.append(-1).append(csvDelimiter).append(stat.getMin()).append(csvDelimiter)
						.append(stat.getMax()).append(csvDelimiter).append(stat.getAverage()).append(csvDelimiter)
						.append(stat.getSum()).append(System.lineSeparator());
				bw.write(sb.toString());

				// For each generation also print information about sub population

			}
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(populationFile))) {
			for (var entry : fullHistory.entrySet()) {

				// Construct a line at a time
				int generation = entry.getKey();
				var subPopulation = entry.getValue();
				for (int i = 0; i < subPopulation.size(); i++) {
					for (var individual : subPopulation.get(i)) {
						StringBuffer sb = new StringBuffer(100);
						sb.append(generation).append(csvDelimiter).append(subPopulation).append(csvDelimiter);
						String[] tokens = individual.toCSV();
						for (int j = 0; j < tokens.length; j++) {
							sb.append(tokens[j]).append((j != tokens.length - 1) ? csvDelimiter : "");
						}
						sb.append(System.lineSeparator());
						bw.write(sb.toString());
					}
				}
			}
		}
	}

	/**
	 * Return all sub populations generated in the specified generation.
	 * 
	 * <p>
	 * The returned population is increasingly sorted by fitness value. The best
	 * individual of each generation can be retrieved by calling
	 * <code>getGeneration()[0]</code>
	 * 
	 * @param generation the generation number [0 - currentGeneration] or -1 for the
	 *                   initial population. Lower numbers represent older
	 *                   generations
	 * @return the entire population of the specified generation or null if no data
	 *         exists for this generation
	 */
	public List<Individual[]> getGeneration(int generation) {
		return Collections.unmodifiableList(fullHistory.get(generation));
	}

	/**
	 * Return all individuals generated in the specified generation for the given
	 * subPopulations.
	 * 
	 * @param generation         the generation number [0 - currentGeneration] or -1
	 *                           for the initial population. Lower numbers represent
	 *                           older generations
	 * @param subPopulationIndex the index of the sub population
	 * @return All individuals of this sub population of the specified generation or
	 *         null if the generation was not recorded
	 * @throws IndexOutOfBoundsException if the sub population index is out of range
	 */
	public Individual[] getGeneration(int generation, int subPopulationIndex) {

		List<Individual[]> hist = fullHistory.get(generation);
		if (hist != null) {
			return hist.get(subPopulationIndex);
		}
		return null;
	}

	/**
	 * Return the number of sub populations the genetic algorithm used to create the
	 * result
	 * 
	 * @return the number of sub populations
	 */
	public int getSubPopulationCount() {
		return fullHistory.get(generations).size();
	}

	/**
	 * Return the most recent generation number.
	 * 
	 * @return the current generation number.
	 */
	public int getGenerationCount() {
		return generations;
	}

	/**
	 * Return the time passed between the start of the algorithms execution and the
	 * last time this result object was updated
	 * 
	 * @return the execution time so far in milli seconds
	 */
	public long getExecutionTime() {
		return executionTime;
	}

	/**
	 * Return the exception which was thrown during the genetic algorithm's
	 * calculation phase. An exception has been set if the termination reason holds
	 * a value of {@link TerminationReason#Exception}.
	 * 
	 * @return the exception thrown or null
	 */
	public Exception getException() {
		return exception;
	}

	@Override
	public String toString() {
		return "Result [terminationResult=" + terminationResult + ", executionTime=" + executionTime
				+ "ms , generations=" + generations + "\nBest Individual: " + getBestResult() + "]";
	}

	/**
	 * Retrieve a list will all generation numbers for which individual data is
	 * available.
	 * <p>
	 * The initial population will be labeled with generation number of -1
	 * 
	 * @return a list containing all available generations for which individuals are
	 *         available
	 */
	public List<Integer> getAvailableGenerations() {
		return Collections.unmodifiableList(new ArrayList<>(fullHistory.keySet()));
	}

	// Carefull modifiable!
	public Set<Integer> getAvailableGenerationsSet() {
		return fullHistory.keySet();
	}

	/**
	 * Retrieve a list will all generations for which statistic data is available.
	 * This method returns the equivalent number of generations as returned by
	 * {@link #getAvailableGenerations()} if {@link #resetState()} wasn't called
	 * prior.
	 * <p>
	 * The initial population will be labeled with generation number of -1
	 * 
	 * @return a list containing all available generations for which statistics are
	 *         available-
	 */
	public List<Integer> getAvailableStatisticsGenerations() {
		return Collections.unmodifiableList(new ArrayList<>(generationStatistics.keySet()));
	}

	/**
	 * Return the generation step supplied to this result object. The generation
	 * step reflects the gap between consecutive generations. E.g. if only every 5th
	 * generation is recorded this method will return 5.
	 * <p>
	 * A 0 or negative value indicates that only the final generation is stored.
	 * This relationship holds true for all generations except the initial
	 * population (marked as 5) and the final population which might have gap of [1
	 * - generationStep].
	 * 
	 * @return the number of generations skipped between each record.
	 */
	public int getGenerationStep() {
		return generationStep;
	}

	// Reset ability

	/**
	 * Clear all individuals saved in this result object. Calling this method can be
	 * necessary if you choose a small recording number
	 * {@link com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int)}
	 * in order to invoke the event handlers regularly but run out of memory due to
	 * many individuals recorded.
	 * 
	 * <p>
	 * This method resets only the individuals but still keeps track of the
	 * statistics. Alternative method exist to also clear the statistics as well as
	 * only removing older generations
	 * 
	 */
	public void resetState() {
		fullHistory.clear();
		// TODO do we want to suggest a gc to be run? Probably not. Let the vm figure
		// this out on his own.
		// System.gc();
	}

	/**
	 * Clear all individuals saved in this result object. Calling this method can be
	 * necessary if you choose a small recording number
	 * {@link com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int)}
	 * in order to invoke the event handlers regularly but run out of memory due to
	 * many individuals recorded.
	 * 
	 * @param resetStatistics weather or not to also reset the statistics
	 */
	public void resetState(boolean resetStatistics) {
		resetState();
		if (resetStatistics) {
			generationStatistics.clear();
			generationStatisticsSubPopulation.clear();
		}
	}

	/**
	 * Clear all individuals saved in this result object which belong to the
	 * generation smaller than specified. Calling this method can be necessary if
	 * you choose a small recording number
	 * {@link com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int)}
	 * in order to invoke the event handlers regularly but run out of memory due to
	 * many individuals recorded.
	 * 
	 * <p>
	 * This method resets only the individuals but still keeps track of the
	 * statistics. Alternative method exist to also clear the statistics as well as
	 * only removing older generations
	 * 
	 * @param fromGeneration Individuals belonging to this or any older generation
	 *                       will be removed
	 */
	public void resetState(int fromGeneration) {
		// TODO keyset is sorted. we could binary search and cut down cost?
		clearLinkedHashMap(fullHistory, fromGeneration);
	}

	/**
	 * Clear all individuals saved in this result object which belong to the
	 * generation smaller than specified. Calling this method can be necessary if
	 * you choose a small recording number
	 * {@link com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int)}
	 * in order to invoke the event handlers regularly but run out of memory due to
	 * many individuals recorded.
	 * 
	 * 
	 * @param fromGeneration  Individuals belonging to this or any older generation
	 *                        will be removed
	 * @param resetStatistics weather or not to also reset the statistics
	 */
	public void resetState(int fromGeneration, boolean resetStatistics) {
		resetState(fromGeneration);
		if (resetStatistics) {
			clearLinkedHashMap(generationStatistics, fromGeneration);
			clearLinkedHashMap(generationStatisticsSubPopulation, fromGeneration);
		}
	}

	/**
	 * @param map An LinkedHashMap with integer keys ordered ascendingly
	 * @param cut all values smaller or equal to the value
	 */
	private static void clearLinkedHashMap(LinkedHashMap<Integer, ?> map, int cut) {
		var iter = map.keySet().iterator();

		while (iter.hasNext()) {
			if (iter.next() >= cut) {
				iter.remove();
			} else {
				return;
			}
		}
	}

	/**
	 * Write the content of the history to sys out
	 */
	public void dumpContent() {

		for (var entry : fullHistory.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}

	}
}
