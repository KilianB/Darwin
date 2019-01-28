package com.github.kilianB.example.travelingSalesman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.charting.ChartHelper;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy;
import com.github.kilianB.geneticAlgorithm.crossover.ScatteredDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.SinglePointDiscrete;
import com.github.kilianB.geneticAlgorithm.mutationScaling.MutationScalingStrategy;
import com.github.kilianB.geneticAlgorithm.prototypes.IndividualPrototype;
import com.github.kilianB.pcg.fast.PcgRSFast;
import com.github.kilianB.pcg.sync.PcgRR;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.stage.Stage;

//TODO minimize the longest way. This might lead to an even better result...?

//TODO additional paper: https://www.hindawi.com/journals/cin/2017/7430125/ ... maybe something interesting there
public class TravelingSalesmanGui extends Application {

	public static Point2D[] cities;
	private static Collection<Integer> validConnections;

	@Override
	public void start(Stage primaryStage) throws IOException {

		var controller = new TravelingSalesmanController();

		var loader = new FXMLLoader();

		loader.setController(controller);

		Scene scene = new Scene(loader.load(getClass().getResourceAsStream("TravelingSalesman.fxml")),1500,800);
		primaryStage.setScene(scene);
		ChartHelper.setLogo(primaryStage);
		primaryStage.setTitle("Drawin- Traveling Salesman");
		primaryStage.show();

		// TODO last population gets worse
		/*
		 * 
		 * 30000 | 2723,0867 | 4007,08 | 2913,90 | 582780,32 ||| 2723,0867 | 2723,0867 |
		 * 2723,0867 | 2723,0867 30200 | 2723,0867 | 4246,30 | 2947,23 | 589446,83 |||
		 * 2723,0867 | 2723,0867 | 2723,0867 | 2936,4314 30400 | 2723,0867 | 4510,82 |
		 * 2959,00 | 591800,40 ||| 2723,0867 | 2723,0867 | 2723,0867 | 2936,4314 30600 |
		 * 2723,0867 | 4251,85 | 2915,79 | 583157,19 ||| 2723,0867 | 2723,0867 |
		 * 2723,0867 | 2854,9577 30800 | 2723,0867 | 4415,88 | 2936,25 | 587249,16 |||
		 * 2723,0867 | 2723,0867 | 2723,0867 | 2854,9577
		 * 
		 */

		int cityCount = 50;// 60;
		int xMax = 1000;
		int yMax = 1000;
		var rng = new PcgRR();

		cities = new Point2D[cityCount];
		validConnections = new ArrayList<Integer>(cityCount);
		for (int i = 0; i < cityCount; i++) {
			validConnections.add(i);
		}

		// Generate our problem domain
		for (int j = 0; j < cityCount; j++) {
			double x = rng.nextInt(xMax);
			double y = rng.nextInt(yMax);
			cities[j] = new Point2D(x, y);
		}

		GeneticAlgorithm ga = GeneticAlgorithm.builder().withPrototype(new TravelingSalesmanPrototype(true, 3))
				.withMaxGenerationCount(10000)
				/*.withMaxStaleGenerations(200)*/
				.population()
				.withPopulationCount(20)
				.advanced()
				.withForceCloneMutation(true, 10)
				.withMutationProbability(0.35)
				.withMutationScalingStrategy(MutationScalingStrategy.LINEAR_GENERATION)
				.migration(500)
				/*.withNewSubpopulation() */
				.withNewSubpopulation().withCrossoverStrategy(new SinglePointDiscrete(2))
				.withNewSubpopulation().withCrossoverStrategy(new ScatteredDiscrete(2))
				.withNewSubpopulation().withCrossoverStrategy(new SinglePointDiscrete(5))
				.build();

		controller.registerGA(ga,xMax,yMax,cities);
		

		new Thread(() -> {
			ga.calculate(100, Integer.MAX_VALUE, false);
//
//			double f0 = 0, f1 = 0, f2 = 0;
//
//			for (int i = 0; i < 10; i++) {
//				// Generate our problem domain
//				for (int j = 0; j < cityCount; j++) {
//					double x = rng.nextInt(xMax);
//					double y = rng.nextInt(yMax);
//					cities[j] = new Point2D(x, y);
//				}
//
//				var ga2 = GeneticAlgorithm.builder().withPrototype(new TravelingSalesmanPrototype(true, 3))
//						.withMaxStaleGenerations(4000).population().withPopulationCount(25).advanced()
//						.withForceCloneMutation(true, 10).migration().withMigrationInterval(500).withNewSubpopulation()
//						.withCrossoverStrategy(new SinglePointDiscrete(2)).withNewSubpopulation()
//						.withCrossoverStrategy(new ScatteredDiscrete(2)).withNewSubpopulation()
//						.withCrossoverStrategy(new SinglePointDiscrete(5)).withNewSubpopulation().build();
//
//				var ga3 = GeneticAlgorithm.builder().withPrototype(new TravelingSalesmanPrototype(true, 4))
//						.withMaxStaleGenerations(4000).population().withPopulationCount(25).advanced()
//						.withForceCloneMutation(true, 10).migration().withMigrationInterval(500).withNewSubpopulation()
//						.withCrossoverStrategy(new SinglePointDiscrete(2)).withNewSubpopulation()
//						.withCrossoverStrategy(new ScatteredDiscrete(2)).withNewSubpopulation()
//						.withCrossoverStrategy(new SinglePointDiscrete(5)).withNewSubpopulation().build();
//
//				f1 += ga2.calculate(0).getFitness();
//				f2 += ga3.calculate(0).getFitness();
//
//				System.out.println(i);
//			}
//
//			System.out.println(f1);
//			System.out.println(f2);

			/*
			 * 7451.52386003906 7270.611240650295 7739.9188135923705 29331.084018957583
			 * 28754.428849194195 30506.246861379623
			 */
		}).start();
	}

	public static void main(String[] args) {
		launch(args);
	}

	public static class TraelingSalesmanIndividual extends Individual {

		int[] connections;
		boolean kMut = false;
		int k = 0;

		public TraelingSalesmanIndividual(int[] connections, boolean kMut, int k) {
			this.connections = connections;
			this.kMut = kMut;
			this.k = k;
		}

		@Override
		public int getVariableCount() {
			return connections.length;
		}

		@Override
		public Individual crossover(CrossoverStrategyFuzzy crossoverStrategy, Individual... crossoverParent) {
			throw new UnsupportedOperationException("Individual does not support fuzzy crossover");
		}

		public Individual crossover1(CrossoverStrategyDiscrete crossoverStrategy, Individual... crossoverParent) {

			int[] newValues = new int[connections.length];
			var rng = GeneticAlgorithm.RNG;
			List<Integer> validTarget = new ArrayList<>(validConnections);
			Collections.shuffle(validTarget, rng.getUnderlayingRNG());

			int crossoverVector[] = crossoverStrategy.getCrossoverVector(crossoverParent);

			for (int i = 0; i < connections.length; i++) {
				int parentIndex = crossoverVector[i];
				int obj = crossoverParent[parentIndex].getValue(i);
				if (validTarget.contains(obj)) {
					newValues[i] = obj;
					validTarget.remove((Integer) obj);
				} else {
					// Take annother random one
					// Removing from the end is much quicker
					newValues[i] = validTarget.remove(validTarget.size() - 1);
				}
			}
			return new TraelingSalesmanIndividual(newValues, kMut, k);
		}

		@Override
		public Individual crossover(CrossoverStrategyDiscrete crossoverStrategy, Individual... crossoverParent) {

			int[] newValues = new int[connections.length];
			var rng = GeneticAlgorithm.RNG;
			List<Integer> validTarget = new ArrayList<>(validConnections);
			Collections.shuffle(validTarget, rng.getUnderlayingRNG());

			int crossoverVector[] = crossoverStrategy.getCrossoverVector(crossoverParent);
			for (int i = 0; i < connections.length; i++) {

				// Lets deviate a little bit. the traveling salesman is heavily depended on
				// adjacent variables.
				// use the last value as a marker and start from there.

				// O(N^2)
				int parentIndex = crossoverVector[i];
				int start = parentIndex;
				if (i == 0) {
					newValues[0] = (int) crossoverParent[parentIndex].getValue(0);
					validTarget.remove((Integer) newValues[0]);
				} else {
					int obj;
					do {
						int lastValue = newValues[i - 1];
						int nextIndex = -1;
						for (int j = 0; j < connections.length; j++) {
							if (lastValue == (int) crossoverParent[parentIndex].getValue(j)) {
								nextIndex = j + 1;
								break;
							}
						}
						if (nextIndex == connections.length) {
							nextIndex = 0;
						}

						if (nextIndex == -1) {
							System.out.println(lastValue + " " + Arrays
									.toString(((TraelingSalesmanIndividual) crossoverParent[parentIndex]).connections));
						}

						obj = crossoverParent[parentIndex].getValue(nextIndex);

						parentIndex++;
						if (parentIndex == crossoverParent.length) {
							parentIndex = 0;
						}
						if (start == parentIndex) {
							// One time spin around
							obj = validTarget.get(validTarget.size() - 1);
							break;
						}
					} while (!validTarget.contains(obj));

					newValues[i] = obj;
					validTarget.remove((Integer) obj);
				}

			}
			return new TraelingSalesmanIndividual(newValues, kMut, k);
		}

		@Override
		public Individual mutate(double probability, double scaleFactor) {

			int[] newValues;
			if (kMut) {
				newValues = mutateK(probability*scaleFactor, k);
			} else {
				newValues = mutateNew(probability*scaleFactor);
			}
			
			var rng = new PcgRSFast();
			
			switch(rng.nextInt(3)) {
			case 0:
				newValues = mutateK(probability*scaleFactor, k);
				break;
			case 1: 
				newValues = mutateNew(probability*scaleFactor);
				break;
			case 2:
				newValues = mutateSwap(probability*scaleFactor);
				break;
			}

			return new TraelingSalesmanIndividual(newValues, kMut, rng.nextInt(5)+2);
		}

		private int[] mutateSwap(double probability) {
			int[] newValues = new int[connections.length];

			var rng = GeneticAlgorithm.RNG;

			System.arraycopy(connections, 0, newValues, 0, connections.length);
			// If mutate. SWAP two fields!

			for (int i = 0; i < connections.length; i++) {
				if (rng.nextDouble() <= probability) {

					// Todo swap with left or right is better?
					int swapWith = i + (rng.nextBoolean() ? 1 : -1);

					if (swapWith < 0) {
						swapWith = 1;
					} else if (swapWith == connections.length) {
						swapWith = i - 1;
					}

					int temp = newValues[i];
					newValues[i] = newValues[swapWith];
					newValues[swapWith] = temp;
				}
			}
			return newValues;
		}

		private int[] mutateNew(double probability) {
			int[] newValues = new int[connections.length];

			var rng = GeneticAlgorithm.RNG;

			System.arraycopy(connections, 0, newValues, 0, connections.length);
			// If mutate. SWAP two fields!

			for (int i = 0; i < connections.length; i++) {
				if (rng.nextDouble() <= probability) {

					int swapWith;
					do {
						swapWith = rng.nextInt(connections.length);
					} while (swapWith == i);

					int temp = newValues[i];
					newValues[i] = newValues[swapWith];
					newValues[swapWith] = temp;
				}
			}
			return newValues;
		}

		private int[] mutateK(double probability, int k) {
			int[] newValues = new int[connections.length];

			var rng = GeneticAlgorithm.RNG;

			System.arraycopy(connections, 0, newValues, 0, connections.length);
			// If mutate. SWAP two fields!

			for (int i = 0; i < connections.length; i++) {
				if (rng.nextDouble() <= probability) {

					List<Integer> swapIndex = new ArrayList<>(k);
					List<Integer> swapValues = new ArrayList<>(k);

					// Swapping candidates
					for (int j = 0; j < k; j++) {
						int index;
						do {
							index = rng.nextInt(connections.length);
						} while (swapIndex.contains(index));
						swapIndex.add(index);
						swapValues.add(newValues[index]);
					}

					// Actually perform the swap
					Collections.shuffle(swapValues, RNG.getUnderlayingRNG());
					Collections.shuffle(swapIndex, RNG.getUnderlayingRNG());
					while (!swapIndex.isEmpty()) {
						int indexToSwap = swapIndex.remove(0);
						newValues[indexToSwap] = swapValues.remove(0);
					}
				}
			}
			return newValues;
		}
//		
//		private int[] mutateExpensiveEdges(double probability) {
//			int[] newValues = new int[connections.length];
//			var rng = GeneticAlgorithm.RNG;
//
//			double[] cost calculate the most expensive edge and mutate those with a higher 
//			probability
//			//TODO heuristic. avoid crossing edges...
//			
//			System.arraycopy(connections, 0, newValues, 0, connections.length);
//			// If mutate. SWAP two fields!
//
//			for (int i = 0; i < connections.length; i++) {
//				if (rng.nextDouble() <= probability) {
//
//					// Todo swap with left or right is better?
//					int swapWith;
//					do {
//						swapWith = rng.nextInt(connections.length);
//					} while (swapWith == i);
//
//					int temp = newValues[i];
//					newValues[i] = newValues[swapWith];
//					newValues[swapWith] = temp;
//				}
//			}
//			return newValues;
//		}

		private int[] mutateOld(double probability) {
			int[] newValues = new int[connections.length];

			var rng = GeneticAlgorithm.RNG;

			/*
			 * Traveling salesman is a constrain problem. Randomly mutating will produce
			 * many invalid results.
			 */

			List<Integer> validTarget = new ArrayList<>(validConnections);
			Collections.shuffle(validTarget, rng.getUnderlayingRNG());

			// If mutate. SWAP two fields!

			for (int i = 0; i < connections.length; i++) {
				if (rng.nextDouble() <= probability) {
					// Mutate

					// Removing from the end is much quicker
					newValues[i] = validTarget.remove(validTarget.size() - 1);
				} else {
					if (validTarget.contains(connections[i])) {
						newValues[i] = connections[i];
						validTarget.remove((Integer) connections[i]);
					} else {

						// Removing from the end is much quicker
						newValues[i] = validTarget.remove(validTarget.size() - 1);
					}
				}
			}
			return newValues;
		}

		@Override
		protected double calculateFitness() {
			Point2D currentCity = cities[connections[0]];
			double distance = 0;
			for (int i = 1; i < connections.length; i++) {
				distance += cities[connections[i]].distance(currentCity);
				currentCity = cities[connections[i]];
			}
			// And from the very end to the beginning again
			distance += cities[connections[0]].distance(currentCity);
			return distance;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue(int index) {
			return (T) Integer.valueOf(connections[index]);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(connections);
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
			TraelingSalesmanIndividual other = (TraelingSalesmanIndividual) obj;
			if (!Arrays.equals(connections, other.connections))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TraelingSalesmanIndividual [connections=" + Arrays.toString(connections) + "] fitness:"
					+ getFitness();
		}

		@Override
		public String[] toCSV() {
			return null;
		}

	}

	public static int linearSearch(int[] array, int needle, int start, int stop) {
		int maxIndex = Math.min(array.length, stop);
		for (int i = start; i < maxIndex; i++) {
			if (array[i] == needle) {
				return i;
			}
		}
		return -1;
	}

	class TravelingSalesmanPrototype implements IndividualPrototype {

		boolean kMut = false;
		int k = 0;

		public TravelingSalesmanPrototype(boolean b, int i) {
			kMut = b;
			k = i;
		}

		public TravelingSalesmanPrototype() {
		}

		@Override
		public Individual createIndividual() {
			var rng = GeneticAlgorithm.RNG;

			int connections[] = new int[cities.length];
			// Traveling salesman is a constraint problem. we have to be careful to produce
			// valid solution
			List<Integer> validTarget = new ArrayList<>(validConnections);
			Collections.shuffle(validTarget, rng.getUnderlayingRNG());

			for (int i = 0; i < connections.length; i++) {
				connections[i] = validTarget.get(i);
			}
			return new TraelingSalesmanIndividual(connections, kMut, k);
		}
	}

}
