package com.github.kilianB.example.geometries.lineIntersection;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import com.github.kilianB.example.guiHelper.JFXSnapSlider;
import com.github.kilianB.example.guiHelper.ResizableCanvas;
import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.result.Result;
import com.github.kilianB.geneticAlgorithm.result.ResultListener;
import com.github.kilianB.pcg.fast.PcgRSFast;
import com.jfoenix.controls.JFXButton;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LineIntersectionGuiController {

	@FXML
	private BorderPane chartRootBorderPane;

	@FXML
	private GridPane gridPane;

	@FXML
	private Pane drawAreaRoot;

	// @FXML
	// private Pane chartAreaRoot;

	@FXML
	private JFXButton newDataButton;
	
	
	private JFXSnapSlider generationSlider;

	@FXML
	private FlowPane bottomFlowPane;
	
	private ScatterChart<Number, Number> scatterChart;

	private IndividualCanvas drawCanvas;

	int curX = 0;

	ObservableList<Data<Number, Number>> bestFitnessChartData;
	ObservableList<Data<Number, Number>> allFitnessChartData;
	
	@FXML
	public void initialize() {

		drawCanvas = new IndividualCanvas(drawAreaRoot);
		drawAreaRoot.getChildren().add(drawCanvas);

		generationSlider = new JFXSnapSlider(0);
		generationSlider.prefWidthProperty().bind(drawAreaRoot.widthProperty().subtract(newDataButton.widthProperty()));
		generationSlider.setMax(0);
		
		bottomFlowPane.getChildren().add(generationSlider);

		
		generationSlider.valueProperty().addListener((newValue)->{	
			drawCanvas.drawGeneration((int)((DoubleProperty)newValue).get());
		});
			
		XYChart.Series<Number, Number> fitnessSeries = new XYChart.Series<>();
		fitnessSeries.setName("Best Individual");
		bestFitnessChartData = fitnessSeries.getData();
		
		XYChart.Series<Number, Number> allFitnessSeries = new XYChart.Series<>();
		allFitnessSeries.setName("All Individuals");
		allFitnessChartData = allFitnessSeries.getData();
		

		// Setup chart

		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();

		xAxis.setLabel("Generation");
		xAxis.setAnimated(false);
		yAxis.setAnimated(false);
		yAxis.setLabel("Fitness");

		scatterChart = new ScatterChart<Number, Number>(xAxis, yAxis);
		chartRootBorderPane.setCenter(scatterChart);

		scatterChart.getData().add(fitnessSeries);
		scatterChart.getData().add(allFitnessSeries);

		scatterChart.getXAxis();
		// chartCanvas = new ChartCanvas(chartAreaRoot);
		// chartAreaRoot.getChildren().add(chartCanvas);

		newDataButton.setOnAction((event) -> {

			generationSlider.setDisable(true);
			
			int populationSize = 15;
			int linesOnCanvas = 25;

			LineIntersectionIndividual[] initialPopulation = new LineIntersectionIndividual[populationSize];

			GeometryFactory factory = new GeometryFactory();

			var rng = new PcgRSFast();

			
			//Sets a bounded problem
			GeometryFactory fact = new GeometryFactory();
			
			Coordinate[] domainBound = new Coordinate[] {
					new Coordinate(0,0),
					new Coordinate(0,1),
					new Coordinate(1,1),
					new Coordinate(1,0),
					new Coordinate(0,0)
			};
			

			LineIntersectionIndividual.setDomainBounds(fact.createPolygon(domainBound));
			
			//TODO we need to check that the individuals created are valid as well ...
			
			// initial domain 0 - 1
			for (int i = 0; i < populationSize; i++) {

				List<LineString> lines = new ArrayList<>();
				// Create a JTS Line
				for (int j = 0; j < linesOnCanvas; j++) {
					lines.add(factory
							.createLineString(new Coordinate[] { new Coordinate(rng.nextDouble(), rng.nextDouble()),
									new Coordinate(rng.nextDouble(), rng.nextDouble()) }));
				}
				initialPopulation[i] = new LineIntersectionIndividual(lines);
			}

			var ga = GeneticAlgorithm.builder().withInitialPopulation(initialPopulation)
					.withMaxGenerationCount(50000).population()
					.advanced()
					.withForceCloneMutation(false,0)
					.withMutationProbability(0.05)
					//.withCrossoverStrategy(new ScatteredDiscrete(2))
					//.withCrossoverStrategy(new ScatteredFuzzy(2))
					.migration(100)
					.withNewSubpopulations(4)
					//.withScalingStrategy(new AgeScaling())
					.build();
					
			
			//ChartHelper.displayProgressPane("Rastrigin",ga);
			
			System.out.println("Start ga");

			drawCanvas.reset();
			bestFitnessChartData.clear();
			
			generationSlider.setMax(0);
			
			ga.addResultListener(new ResultListener() {
				@Override
				public void intermediateResult(Result r) {
			
					//Update fitness chart
					Platform.runLater(()->{
						
						
						for(Individual ind : r.getGeneration(r.getGenerationCount(),0)) {
							allFitnessChartData.add(new XYChart.Data<Number, Number>(r.getGenerationCount(),ind.getFitness()));
						}
						
						bestFitnessChartData.add(new XYChart.Data<Number, Number>(r.getGenerationCount(),r.getFitness()));
						
						
					});
					
					//Update Best solution individual;
					if(drawCanvas.addGeneration(r.getGenerationCount(),r.getBestResult())) {
						generationSlider.setMax(r.getGenerationCount());
						generationSlider.addTickmark(r.getGenerationCount());
						generationSlider.setValue(r.getGenerationCount());
					}
				}

				@Override
				public void finalResult(Result r) {
					
					drawCanvas.addGeneration(r.getGenerationCount(),r.getBestResult());
					generationSlider.setMax(r.getGenerationCount());
					generationSlider.addTickmark(r.getGenerationCount());
					generationSlider.setValue(r.getGenerationCount());
					
					generationSlider.setDisable(false);
				}
			});

			new Thread(()->{
				ga.calculate(10);
			}).start();

			

		});

	}

	public void afterSetup() {

		scatterChart.setTitle("Fitness");
	}

	class IndividualCanvas extends ResizableCanvas{
		
		private int intersectionWidth = 8;
		
		private Color lineColor = Color.BLACK;
		private Color intersectionColor = Color.RED;
		private Color generationColor = Color.GRAY;
		
		LinkedHashMap<Integer,Individual> individuals = new LinkedHashMap<>();
		HashSet<Individual> duplicateCheck = new HashSet<>();

		//TODO do we really need this kind of overkill in the javafx application thread?
		//If yes are atomic values good enough?
		private volatile boolean busyDrawing = false;
		private boolean redrawNecessary = false;
		private int requestedGeneration;
		ReentrantLock lock = new ReentrantLock();
		
		
		public IndividualCanvas(Region parent) {
			super(parent);
			
			this.getGraphicsContext2D().setStroke(lineColor);
			this.getGraphicsContext2D().setFill(intersectionColor);
			
		}
		


		public boolean addGeneration(int generation,Individual bestIndividual) {
			
			if(duplicateCheck.contains(bestIndividual)) {
				return false;
			}else {
				duplicateCheck.add(bestIndividual);
				individuals.put(generation,bestIndividual);
				return true;
			}
			
			
		}
		
		public void drawGeneration(int generation) {
			
			Platform.runLater(() ->{
				if(busyDrawing) {
					lock.lock();
					redrawNecessary = true;
					requestedGeneration = generation; 
					lock.unlock();
					return;
				}
				
				if(individuals.size() == 0 || !individuals.containsKey(generation)) {
					return;
				}
				busyDrawing = true;
				
				
				var gc = this.getGraphicsContext2D();
				gc.clearRect(0, 0, this.getWidth(), this.getHeight());
				
				//Just draw the best individual
				
				
				var individual = (LineIntersectionIndividual)individuals.get(generation);
				
				List<LineString> lines = individual.getAllLines();
				
				var listIter = lines.listIterator();
			
				ArrayList<Coordinate> intersection = new ArrayList<Coordinate>();
				
				//First draw all lines
				while(listIter.hasNext()) {
					LineString ls = listIter.next();
					listIter.remove();
					var iterator = lines.iterator();
					
					while(iterator.hasNext()) {
						
						LineString l2 = iterator.next();
						
						Geometry intersectionPoint = ls.intersection(l2);
						
						if(!intersectionPoint.isEmpty()) {
							intersection.add(intersectionPoint.getCoordinate());
						}
					}
					
					//Draw all lines
					Coordinate[] coords = ls.getCoordinates();
					gc.strokeLine(coords[0].getX()*getWidth(), coords[0].getY()*getHeight(), coords[1].getX()*getWidth(),coords[1].getY()*getHeight());
				}
				
				listIter = lines.listIterator();
				
				//Draw all intersections
				for(Coordinate c : intersection) {
					gc.fillOval(c.x*getWidth()-(intersectionWidth/2d), c.y*getHeight()-(intersectionWidth/2d),intersectionWidth,intersectionWidth);
				}
				
				//Draw generation label
				
				
				//Expensive
				
				Iterator<Integer> iter = individuals.keySet().iterator();
				String text = "Generation: " + generation;
				while(iter.hasNext()) {
					Integer key = iter.next();
					if(key.equals(Integer.valueOf(generation))) {
						if(iter.hasNext()) {
							text += " - " + iter.next().intValue();
							break;
						}
					}
				}
				
				gc.setFont(Font.font("verdana", 20));
				Color curFillColor = (Color) gc.getFill();
				gc.setFill(generationColor);
				gc.fillText(text, 25, 25);
				gc.setFill(curFillColor);
				
				busyDrawing = false;
				
				if(redrawNecessary) {
					lock.lock();
					
					redrawNecessary = false;
					int genToDraw = requestedGeneration;
					lock.unlock();
					drawGeneration(genToDraw);
					
				}
			});
			
		}
		
		public void reset() {
			individuals = new LinkedHashMap<>();
		}
		
	}
	
}
