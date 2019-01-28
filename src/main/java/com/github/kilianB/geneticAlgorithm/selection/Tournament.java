package com.github.kilianB.geneticAlgorithm.selection;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.fitnessScaling.FitnessScalingStrategy.ScaledFitness;

/**
 * 
 * X individuals are randomly chosen from the population and the fittest is
 * selected to be the parent.
 * 
 * @author Kilian
 * @see https://se.mathworks.com/help/gads/genetic-algorithm-options.html#f6593
 */
public class Tournament implements SelectionStrategy {

	private static final Logger LOGGER = Logger.getLogger(Tournament.class.getName());
	
	/**
	 * How many individuals are selected each tournament round
	 */
	private int tournamentSize;

	public Tournament(int tournamentSize) {
		if (tournamentSize < 2) {
			throw new IllegalArgumentException("Tournament size has to be at least 2");
		}
		this.tournamentSize = tournamentSize;
	}

	@Override
	public Individual[] selectParents(ScaledFitness[] scaledFitness, int count) {

		Individual[] selectedParents = new Individual[count];

		if (tournamentSize > scaledFitness.length) {
			tournamentSize = scaledFitness.length;
			LOGGER.warning("Tournament size greater than individuals present. Adjust size to population size. " + scaledFitness.length);
		}

		// Tournament selects x DISTINCT! individuals not regarding the fitness value
		// and simply chooses the best
		// in the set
		for (int i = 0; i < count; i++) {
			List<ScaledFitness> nominationCandidates = new ArrayList<ScaledFitness>(Arrays.asList(scaledFitness));

			// PriorityQueue<ScaledFitness> toutnamentCandidates = new PriorityQueue<>();
			ScaledFitness mostFit = null;

			for (var j = 0; j < tournamentSize; j++) {

				ScaledFitness onTheHotSeat = nominationCandidates.remove(RNG.nextInt(nominationCandidates.size()));

				if (mostFit == null) {
					mostFit = onTheHotSeat;
				} else if (mostFit.compareTo(onTheHotSeat) < 0) {
					mostFit = onTheHotSeat;
				}
			}
			selectedParents[i] = mostFit.getIndividual();
			// Populated not choose the
		}
		return selectedParents;
	}

	/**
	 * A abstracted implementation allowing unit tests returning the candidates as
	 * well as the chosen candidate
	 * 
	 * @param scaledFitness
	 */
	@Deprecated
	public ScaledFitness[] selectParentsUnitTest(ScaledFitness[] scaledFitness) {

		if (tournamentSize > scaledFitness.length) {
			tournamentSize = scaledFitness.length;
			LOGGER.warning("Tournament size smaller than individuals present. Adjust size to population size.");
		}

		ScaledFitness[] selectedParents = new ScaledFitness[tournamentSize + 1];

		List<ScaledFitness> nominationCandidates = new ArrayList<>(Arrays.asList(scaledFitness));

		ScaledFitness mostFit = null;

		for (var j = 0; j < tournamentSize; j++) {

			ScaledFitness onTheHotSeat = nominationCandidates.remove(RNG.nextInt(nominationCandidates.size()));

			if (mostFit == null) {
				mostFit = onTheHotSeat;
			} else if (mostFit.compareTo(onTheHotSeat) < 0) {
				mostFit = onTheHotSeat;
			}

			selectedParents[j] = onTheHotSeat;
		}
		selectedParents[tournamentSize] = mostFit;
		return selectedParents;
	}

	@Override
	public String toString() {
		return "Tournament [tournamentSize=" + tournamentSize + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + tournamentSize;
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
		Tournament other = (Tournament) obj;
		if (tournamentSize != other.tournamentSize)
			return false;
		return true;
	}
	
	
	
	

}
