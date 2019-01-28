package com.github.kilianB.geneticAlgorithm.charting;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import com.github.kilianB.MultiTypeChart;
import com.github.kilianB.SymbolType;
import com.github.kilianB.TypedSeries;
import com.github.kilianB.ValueMarker;
import com.github.kilianB.concurrency.DelayedConsumerHashMap;
import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.result.Result;
import com.github.kilianB.geneticAlgorithm.result.ResultListener;

import javafx.application.Platform;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.paint.Color;

/**
 * @author Kilian
 *
 */
public class ProgressChart extends MultiTypeChart<Number, Number> {

	private TypedSeries<Number, Number> bestFitness = TypedSeries.<Number, Number>builder("Best").line().build();
	private TypedSeries<Number, Number>[] populationSeries;

	boolean axisInit = false;
	// TODO add a few more colors
	int[] colorIndex = { 79, 83, 13, 25, 113, 98, 0, 0, 0, 0 };

	private boolean displayAverage;
	private boolean logScale;

	ValueMarker<Integer> valueMarker;

	ValueAxis<Number> yAxis;

	ReentrantLock lock = new ReentrantLock();
	List<List<Data<Number, Number>>> dataCache = new ArrayList<>();

	double axisNewMax = -Double.MAX_VALUE;
	double axisNewMin = Double.MAX_VALUE;

	// TODO shutdown
	ExecutorService exec = Executors.newSingleThreadExecutor();

	DelayedConsumerHashMap<List<List<Data<Number, Number>>>> consumerHashMap = new DelayedConsumerHashMap<>((item) -> {
		
		Platform.runLater(() -> {

			for (int subPopulation = 0; subPopulation < item.size(); subPopulation++) {
				int gen = subPopulation;
				// update axis
				if (axisNewMin < yAxis.getLowerBound() || !axisInit) {
					System.out.println("Set New Min: " + axisNewMin);
					yAxis.setLowerBound(axisNewMin);
				}
				if (axisNewMax > yAxis.getUpperBound() || !axisInit) {
					System.out.println("Set New Max: " + axisNewMax);
					yAxis.setUpperBound(axisNewMax);
					axisInit = true;
				}

				lock.lock();
				List<Data<Number, Number>> copy = new ArrayList<>(item.get(gen));
				item.get(gen).clear();
				lock.unlock();

				if (gen == 0) {
					bestFitness.addDataBatch(copy);
				} else {
					populationSeries[gen - 1].addDataBatch(copy);
				}
			}
		});
	}, 500);

	/**
	 * @param xAxis
	 * @param yAxis
	 */
	@SuppressWarnings("unchecked")
	public ProgressChart(String title, GeneticAlgorithm ga, boolean displayAverage, boolean logScale) {
		super(new NumberAxis(), logScale ? new LogarithmicNumberAxis(1, 1) : new NumberAxis());

		if (title != null && !title.isEmpty()) {
			this.setTitle(title);
		}

		yAxis = (ValueAxis<Number>) this.getYAxis();

		// this.setTitle("Title");
		this.getXAxis().setLabel("Generation");
		this.getYAxis().setLabel("Fitness");

		populationSeries = new TypedSeries[ga.getSubPopulationCount() * (displayAverage ? 2 : 1)];
		this.addSeries(bestFitness);

		this.displayAverage = displayAverage;
		this.logScale = logScale;

		this.setSeriesColor(bestFitness.getSeries(), 53);

		for (int i = 0; i < populationSeries.length; i++) {

			if (displayAverage) {
				populationSeries[i] = TypedSeries.<Number, Number>builder("Population (" + i + ")").line().build();
				populationSeries[++i] = TypedSeries.<Number, Number>builder("Average Population (" + i + ")").scatter()
						.build();
				this.addSeries(populationSeries[i - 1]);
				this.addSeries(populationSeries[i]);
				this.setSeriesColor(i, colorIndex[i] + 1);
				this.setSeriesSymbol(i, SymbolType.solidCicrle);
				this.setSeriesColor(i + 1, colorIndex[i]);
				this.setSeriesSymbol(i + 1, SymbolType.hollowCircle);
			} else {
				populationSeries[i] = TypedSeries.<Number, Number>builder("Population (" + i + ")").scatter().build();
				this.addSeries(populationSeries[i]);
				this.setSeriesColor(i + 1, colorIndex[i]);
				this.setSeriesSymbol(i + 1, SymbolType.solidCicrle);
			}
		}

		this.setAnimated(false);

		ga.addResultListener(new ResultListener() {
			@Override
			public void intermediateResult(Result r) {
				updateSeries(r);
			}

			@Override
			public void finalResult(Result r) {
				updateSeries(r);
			}

			private void updateSeries(Result r) {

				int generation = r.getGenerationCount();

				// Use an executor service to not block the genetic algorithm
				exec.execute(() -> {
					List<Data<Number, Number>[]> dataArray = new ArrayList<>();

					// Cache new axis values
					DoubleSummaryStatistics best = r.getSummary(generation);
					if (best.getMin() < axisNewMin) {
						axisNewMin = best.getMin();
					}
					if (best.getMax() > axisNewMax) {
						axisNewMax = best.getMax();
					}

					// Prepare data
					if (!displayAverage) {
						List<Individual[]> individuals = r.getGeneration(generation);
						for (int i = 0; i < populationSeries.length; i++) {
							// Collect to batch request
							Individual[] individualArr = individuals.get(i);
							Data[] d = new Data[individualArr.length];
							for (int j = 0; j < individualArr.length; j++) {
								d[j] = new Data<>(generation, individualArr[j].getFitness());
							}
							dataArray.add(d);
						}
					} else {

						// Lets work on the average case alone
						lock.lock();

						for (int i = -1; i < populationSeries.length / 2d; i++) {
							// Collect to batch request
							List<Data<Number, Number>> dataPoints;

							
							// Handle best fitness
							if (i == -1) {
								if (dataCache.isEmpty()) {
									dataPoints = new ArrayList<>();
									dataCache.add(dataPoints);
									System.out.println("Add new base Series: " + dataCache.size());
								} else {
									dataPoints = dataCache.get(0);
								}
								dataPoints.add(new Data<>(generation, r.getSummary(generation).getMin()));
							} else {
								// Individual series
								DoubleSummaryStatistics stats = r.getSummarySubPopulation(generation, i);
								
								int j = ((i)*2);
								
								for (int m = 0; m < 2; m++) {
									if (dataCache.size() <= j + m+1) {
										dataPoints = new ArrayList<>();
										dataCache.add(dataPoints);
										System.out.println("Add new Series: " + dataCache.size());
									} else {
										dataPoints = dataCache.get(j + m+1);
									}

									if (m == 0) {
										dataPoints.add(new Data<>(generation, stats.getMin()));
									} else {
										dataPoints.add(new Data<>(generation, stats.getAverage()));
									}
								}
							}

						}

						consumerHashMap.put(0, dataCache);
						lock.unlock();

					}
				});

//				Platform.runLater(() -> {
//					DoubleSummaryStatistics best = r.getSummary(generation);
//					double newMin = best.getMin();
//					bestFitness.addData(generation, newMin);
//
//					if (logScale) {
//						if (newMin < yAxis.getLowerBound() || !axisInit) {
////							int decimalPlaces = numberOfDecimals(newMin);
////							if (newMin > 1) {
////								yAxis.setLowerBound(Math.pow(10, decimalPlaces - 1));
////							} else {
////								if (newMin != 0.0) {
////									yAxis.setLowerBound(Math.pow(10, -decimalPlaces));
////								}
////							}
//							yAxis.setLowerBound(newMin);
//							axisInit = true;
//						}
//					}
//
//					if (displayAverage) {
//						for (int i = 0; i < populationSeries.length / 2d; i++) {
//							DoubleSummaryStatistics subGenerationStats = r.getSummarySubPopulation(generation, i);
//							double yValue = subGenerationStats.getAverage();
//							populationSeries[2 * i].addData(generation, subGenerationStats.getMin());
//							populationSeries[2 * i + 1].addData(generation, yValue);
//						}
//
//					} else {
//						for (int i = 0; i < populationSeries.length; i++) {
//							populationSeries[i].addDataBatch(dataArray.get(i));
//						}
//					}
//					updateAxis(r.getSummary(generation).getMax());
//				});

//				// Sometimes we enqueue data much much quicker than we update the heatmap.
//				// Collect the data and commit everything in one natch
//				Platform.runLater(() -> {
//					DoubleSummaryStatistics best = r.getSummary(generation);
//
//					System.out.println(generation);
//					
//					double newMin = best.getMin();
//					bestFitness.addData(generation, best.getMin());
//
//					if (logScale) {
//						if (newMin < yAxis.getLowerBound() || !axisInit) {
//							int decimalPlaces = numberOfDecimals(newMin);
//							if (newMin > 1) {
//								yAxis.setLowerBound(Math.pow(10, decimalPlaces - 1));
//							} else {
//								if (newMin != 0.0) {
//									yAxis.setLowerBound(Math.pow(10, -decimalPlaces));
//								}
//							}
//							axisInit = true;
//						}
//					}
//
//					if (displayAverage) {
//						for (int i = 0; i < populationSeries.length/2d;i++) {
//							DoubleSummaryStatistics subGenerationStats = r.getSummarySubPopulation(generation, i);
//							double yValue = subGenerationStats.getAverage();
//							populationSeries[2*i].addData(generation, subGenerationStats.getMin());
//							populationSeries[2*i+1].addData(generation, yValue);
//							updateAxis(yValue);
//						}
//
//					} else {
//						List<Individual[]> individuals = r.getGeneration(generation);
//						for (int i = 0; i < populationSeries.length; i++) {
//
//							// Collect to batch request
//							Individual[] individualArr = individuals.get(i);
//							Integer[] generationArr = new Integer[individualArr.length];
//							Double[] fitnessArr = new Double[individualArr.length];
//
//							
//							for (int j = 0; j < individualArr.length; j++) {
//								double fitness = individualArr[j].getFitness();
//								generationArr[j] = generation;
//								fitnessArr[j] = fitness;
//							}
//
//							updateAxis(r.getSummary(generation).getMax());
//							populationSeries[i].addDataBatch(generationArr, fitnessArr);
//						}
//					}
//					chart.requestChartLayout();
//				});
			}
		});
	}

	private void updateAxis(double value) {
		if (value > yAxis.getUpperBound()) {
			if (logScale) {
				// ((LogarithmicNumberAxis) yAxis).setLogarithmizedUpperBound(value);
				yAxis.setUpperBound(value);
			} else {
				yAxis.setUpperBound(value);
			}
		}
	}

	public synchronized void updateVerticalMarker(int markerValue) {
		// TODO ma
		if (valueMarker != null) {
			this.removeValueMarker(valueMarker);
		}
		valueMarker = new ValueMarker<>(markerValue, false, Color.RED, true);
		this.addValueMarker(valueMarker);
	}

	public static int numberOfDecimals(double x) {
		if (x == 0) {
			return 0;
		}
		if (x > 1) {
			return (int) Math.ceil(Math.log(x) / Math.log(10));
		} else {
			return (int) -(Math.floor(Math.log(x) / Math.log(10)));
		}

	}

}
