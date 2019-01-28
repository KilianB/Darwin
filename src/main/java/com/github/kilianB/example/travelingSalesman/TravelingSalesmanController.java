package com.github.kilianB.example.travelingSalesman;

import com.github.kilianB.example.guiHelper.ResizableCanvas;
import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.charting.ChartHelper;
import com.github.kilianB.geneticAlgorithm.result.Result;
import com.github.kilianB.geneticAlgorithm.result.ResultAdapter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class TravelingSalesmanController {

	@FXML
	private BorderPane root;
	
	private AnchorPane canvasRoot;
	
	private Canvas drawCanvas;

	@FXML
	public void initialize() {
		
		canvasRoot = new AnchorPane();
		
		canvasRoot.setMinWidth(600);
		canvasRoot.setMinHeight(600);
		
		drawCanvas = new ResizableCanvas(canvasRoot);
		canvasRoot.getChildren().add(drawCanvas);
		
		root.setLeft(canvasRoot);

		drawCanvas.widthProperty().addListener((obs) -> {
			drawConnections();
		});

		drawCanvas.heightProperty().addListener((obs) -> {
			drawConnections();
		});

	}

	private void drawConnections() {

		if(bestResult == null)
			return;
		
		Platform.runLater(() -> {
			double w = drawCanvas.getWidth();
			double h = drawCanvas.getHeight();
			double xScale = (w - 30) / maxX;
			double yScale = (h - 30) / maxY;

			var gc = drawCanvas.getGraphicsContext2D();

			gc.clearRect(0, 0, w, h);

			// Draw connection

			int from = bestResult.getValue(0);

			for (int i = 0; i < cities.length; i++) {
				int to = bestResult.getValue(i);

				Point2D startPoint = cities[from];
				Point2D endPoint = cities[to];
				gc.strokeLine(startPoint.getX() * xScale + 15, startPoint.getY() * yScale + 15,
						endPoint.getX() * xScale + 15, endPoint.getY() * yScale + 15);
				from = to;
			}

			// Connect start and end
			Point2D startPoint = cities[from];
			Point2D endPoint = cities[(int) bestResult.getValue(0)];
			gc.strokeLine(startPoint.getX() * xScale + 15, startPoint.getY() * yScale + 15,
					endPoint.getX() * xScale + 15, endPoint.getY() * yScale + 15);

			// Draw cities
			for (int i = 0; i < cities.length; i++) {
				Point2D city = cities[i];

				double x = city.getX() * xScale;
				double y = city.getY() * yScale;

				gc.setFill(Color.RED);
				gc.setStroke(Color.BLACK);
				gc.fillOval(x, y, 30, 30);
				gc.strokeOval(x, y, 30, 30);
				gc.setFill(Color.BLACK);
				gc.setTextAlign(TextAlignment.CENTER);
				gc.setTextBaseline(VPos.CENTER);
				gc.fillText(Integer.toString(i), x + 15, y + 15);
			}

		});

	}

	int maxX; 
	int maxY;
	Point2D[] cities;
	Individual bestResult;
	/**
	 * @param i
	 * @param j
	 * @param cities
	 * @param bestResult
	 */
	private void drawConnections(int maxX, int maxY, Point2D[] cities, Individual bestResult) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.cities = cities;
		this.bestResult = bestResult;
		drawConnections();
	}

	/**
	 * @param ga
	 * @param yMax 
	 * @param xMax 
	 * @param cities 
	 */
	public void registerGA(GeneticAlgorithm ga, int xMax, int yMax, Point2D[] cities) {
		this.cities = cities;
		ga.addResultListener(new ResultAdapter() {
			double lastFitness = 0;
			@Override
			public void intermediateResult(Result r) {
				double fitness = r.getBestResult().getFitness();
				if (lastFitness != fitness) {
					drawConnections(xMax, yMax, cities, r.getBestResult());
					lastFitness = fitness;
				}
			}

			@Override
			public void finalResult(Result r) {
				drawConnections(xMax, yMax, cities, r.getBestResult());
			}
		});
		
		Node n = ChartHelper.createProgressChart("",ga,false,false);
		ChartHelper.displayVarInspectionPane(ga);
		
		root.setCenter(n);
	}

}
