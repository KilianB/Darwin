package com.github.kilianB.geneticAlgorithm.charting;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.imageio.ImageIO;

import com.github.kilianB.ArrayUtil;
import com.github.kilianB.MathUtil;
import com.github.kilianB.graphics.ColorUtil;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.PopupWindow.AnchorLocation;

/**
 * A heatmap allowing to plot numerical or categorical X,Y,Z (color key) data.
 * 
 * @author Kilian
 *
 */
public class Heatmap extends Canvas {

	ReadWriteLock rwLock = new ReentrantReadWriteLock();
	Lock rLock = rwLock.readLock();
	Lock wLock = rwLock.writeLock();

	int xSections;
	int ySections;
	int bottomOffset;
	int leftOffset;

	double marginTop = 50;
	double marginBottom = 40;
	double marginLeft = 20;
	double marginRight = 80;

	double gridWidth;
	double gridHeight;
	double gridXOffset;
	double gridYOffset;

	double symbolWidth = 4;
	Font axisFont = new Font(12);

	boolean drawGrid = true;

	boolean headingsDirty = false;

	double rowHeadingWidth = 0;

	ArrayList<String> columnHeadings = new ArrayList<>();
	double columnHeadingHeight = 0;

	private HashMap<Integer, Boolean> populationVisibility = new HashMap<>();
	private Color[] populationColor;

	int colors = 600;

	private Color startColor = Color.GREEN;
	private Color endColor = Color.RED;
	private Color noDataColor = Color.GRAY;
	private Color white50 = new Color(1, 1, 1, 0.5);
	private Color[] heatmapColors = ColorUtil.ColorPalette.getPalette(colors + 1, startColor, endColor);

	// Numerical or categorical?=
	List<Data> numericalData = new ArrayList<>();

	// Pre sort items for quicker access

	// Concurrent Hashmaps ...
	ConcurrentHashMap<Object, Integer> categoricalDataX = new ConcurrentHashMap<>();
	ConcurrentHashMap<Object, Integer> categoricalDataY = new ConcurrentHashMap<>();

	boolean xAxisCategorical = false;
	boolean yAxisCategorical = false;

	// -2 indicating to draw all generation.
	// -1 initial population
	private int activeGeneration = -2;

	public Heatmap(Region parent, int xSections, int ySections, int subPopulations) {
		this.xSections = xSections;
		this.ySections = ySections;

		heatmapColors[0] = noDataColor;

		headingsDirty = true;
		rowHeadingWidth = 19;

		this.heightProperty().bind(parent.heightProperty());
		this.widthProperty().bind(parent.widthProperty());

		for (int i = 0; i < subPopulations; i++) {
			populationVisibility.put(i, true);
		}

		populationColor = ColorUtil.ColorPalette.getPalette(subPopulations);

		this.heightProperty().addListener(obs -> {
			redraw();
		});

		this.widthProperty().addListener(obs -> {
			redraw();
		});

		setupTooltip();

		setupContextMenu();
	}

	/**
	 * Create the tool tip used to display the fitness of a square.
	 */
	private void setupTooltip() {
		DecimalFormat df = new DecimalFormat("##0.00%");

		Tooltip tt = new Tooltip();
		tt.setAnchorLocation(AnchorLocation.CONTENT_BOTTOM_RIGHT);
		tt.setAutoFix(false);

		double offset = 100;

		this.setOnMouseMoved((event) -> {

			int xSec = xAxisCategorical ? categoricalDataX.size() : this.xSections;
			int ySec = yAxisCategorical ? categoricalDataY.size() : this.ySections;

			int xBucket = (int) ((event.getX() - marginLeft - gridXOffset) / (gridWidth / xSec));
			int yBucket = (int) ((event.getY() - marginTop) / (gridHeight / ySec));
			if (!(xBucket < 0 || xBucket >= xSec || yBucket < 0 || yBucket >= ySec)) {
				// Position tooltip
				if (!tt.isShowing()) {
					tt.show(this, event.getScreenX() - offset, event.getScreenY() - offset);
				} else {
					tt.setX(event.getScreenX() - offset);
					tt.setY(event.getScreenY() - offset);
				}

				// Update text
				DoubleSummaryStatistics stat = dat[xBucket][yBucket];
				tt.setText("Occurances: " + stat.getCount() + "\n" + "Average Fitness: " + df.format(stat.getAverage())
						+ "\n" + "Max Fitness: " + df.format(stat.getMax()) + "\n" + "Min Fitness: "
						+ df.format(stat.getMin()));
			} else if (tt.isShowing()) {
				tt.hide();
			}

		});

		this.setOnMouseExited((event) -> {
			if (tt.isShowing()) {
				tt.hide();
			}
		});
	}

	/**
	 * Create the context menu of the chart
	 */
	private void setupContextMenu() {
		MenuItem saveImage = new MenuItem("Save As Image");

		ContextMenu cMenu = new ContextMenu(saveImage);
		cMenu.setOnAction((event) -> {

			FileChooser saveToDialog = new FileChooser();
			saveToDialog.setTitle("Save Image");
			saveToDialog.getExtensionFilters().addAll(new ExtensionFilter("PNG", "*.png"),
					new ExtensionFilter("JPEG", "*.jpg", ".jpeg", "*.jpe", "*.jfif"));
			File saveTo = saveToDialog.showSaveDialog(this.getScene().getWindow());

			if (saveTo != null) {
				SnapshotParameters param = new SnapshotParameters();
				param.setFill(Color.TRANSPARENT);
				Image snapshot = this.snapshot(param, null);
				String path = saveTo.getAbsolutePath();
				String extension = path.substring(path.lastIndexOf(".") + 1);
				try {
					ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), extension, saveTo);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		this.setOnContextMenuRequested((event) -> {
			cMenu.show(this, event.getScreenX(), event.getScreenY());
		});

		this.setOnMouseClicked((event) -> {
			if (event.getButton().equals(MouseButton.PRIMARY)) {
				if (cMenu.isShowing()) {
					cMenu.hide();
				}
			}
		});

	}

	// Fitness is normalized to 0 - 1 with 1 being the lowest possible fitness and 0
	// being the highest
	static double maxFitness = -Double.MAX_VALUE;
	static double minFitness = Double.MAX_VALUE;

	// X
	double minXValue = Double.MAX_VALUE;
	double maxXValue = -Double.MAX_VALUE;
	double valueRangeX = maxXValue - minXValue;

	// Y
	double minYValue = Double.MAX_VALUE;
	double maxYValue = -Double.MAX_VALUE;
	double valueRangeY = maxYValue - minYValue;

	double minXFitnessIndex = 0;
	double minYFitnessIndex = 0;

	DoubleSummaryStatistics[][] dat;

	private void addData(Data d) {

		if (!(d.xValue instanceof Number)) {
			xAxisCategorical = true;
		}

		if (!(d.yValue instanceof Number)) {
			yAxisCategorical = true;
		}

		numericalData.add(d);
		if (d.fitness > maxFitness) {
			// We need to recompute everything!
			maxFitness = d.fitness;
		}

		boolean newFitnessFound = false;

		if (d.fitness < minFitness) {
			minFitness = d.fitness;
			newFitnessFound = true;
		}

		if (xAxisCategorical) {
			categoricalDataX.putIfAbsent(d.xValue, categoricalDataX.size());

			if (newFitnessFound) {
				minXFitnessIndex = categoricalDataX.get(d.xValue);
			}

		} else {

			double xValue = ((Number) d.xValue).doubleValue();

			if (xValue > maxXValue) {
				maxXValue = xValue;
			}
			if (xValue < minXValue) {
				minXValue = xValue;
			}

			if (newFitnessFound) {
				minXFitnessIndex = xValue;
			}

			valueRangeX = maxXValue - minXValue;
		}

		if (yAxisCategorical) {
			categoricalDataY.putIfAbsent(d.yValue, categoricalDataY.size());

			if (newFitnessFound) {
				minYFitnessIndex = categoricalDataY.get(d.yValue);
			}

		} else {

			double yValue = ((Number) d.yValue).doubleValue();

			if (yValue > maxYValue) {
				maxYValue = yValue;
			}
			if (yValue < minYValue) {
				minYValue = yValue;
			}

			if (newFitnessFound) {
				minYFitnessIndex = yValue;
			}

			valueRangeY = maxYValue - minYValue;
		}
	}

	public void addDataBatch(List<Data> d) {
		addDataBatch(d.toArray(new Data[d.size()]));
	}

	public void addDataBatch(Data... d) {
		wLock.lock();
		for (Data dat : d) {
			addData(dat);
		}

		// Compute new headings

		for (int i = 0; i <= ySections; i++) {
			// Vertical
			// Coordinate to value
			double yPercentage = ((double) i) / ySections;

			String label = null;

			if (yAxisCategorical) {

				for (var entry : categoricalDataY.entrySet()) {
					if (entry.getValue().equals(i)) {
						label = entry.getKey().toString();
						break;
					}
				}
			} else {
				double yVal = (yPercentage * (maxYValue - minYValue)) + minYValue;
				label = String.format("%.2f", yVal);
			}

			double stringWidth = computeStringBounds(label, axisFont).getWidth();
			if (stringWidth > rowHeadingWidth) {
				rowHeadingWidth = stringWidth;
			}
		}

		wLock.unlock();
		redraw();

	}

	AtomicBoolean redrawRequested = new AtomicBoolean(false);
	AtomicBoolean redrawInProgress = new AtomicBoolean(false);

	/**
	 * Request a redraw of the current canvas. Call when ever underlaying data
	 * changes. Redraws will be handled subsequently. If a redraw operation is
	 * currently in progress it
	 * 
	 * 
	 */
	public void redraw() {

		if (redrawInProgress.compareAndSet(false, true)) {
			Platform.runLater(() -> {
				// Looking inside a javafx thread? TODO
				rLock.lock();
				try {
					var gc = this.getGraphicsContext2D();

					gc.setFont(axisFont);

					double w = this.getWidth();
					double h = this.getHeight();

					gc.clearRect(0, 0, w, h);

					double pad = 15; // Left margin between label and grid

					gridXOffset = rowHeadingWidth + pad;
					gridYOffset = pad;
					gridWidth = w - gridXOffset - marginRight - marginLeft;
					gridHeight = h - gridYOffset - marginTop - marginBottom;

					// Draw gridlines

					int xSections = xAxisCategorical ? categoricalDataX.size() : this.xSections;
					int ySections = yAxisCategorical ? categoricalDataY.size() : this.ySections;

					double sectionWidth = gridWidth / xSections;

					double sectionHeight = gridHeight / ySections;

					// Draw the individual numericalData points.

					// calculate the x and y position for the datapoint.

					// Get the percentage

					dat = new DoubleSummaryStatistics[xSections][ySections];
					ArrayUtil.fillArrayMulti(dat, () -> {
						return new DoubleSummaryStatistics();
					});
					// Data

					// Sort it into buckets of the sections
					gc.setFill(Color.BLACK);
					gc.setStroke(Color.BLACK);

					// Get a copy of numericalData so we can still modify and itterate
					List<Data> data = new ArrayList<>(this.numericalData);

					// If not categorical.
					for (Data d : data) {
						if (activeGeneration == -2 || d.generation == activeGeneration) {
							int xBucket;
							int yBucket;

							if (xAxisCategorical) {
								xBucket = categoricalDataX.get(d.xValue);
							} else {
								double xValue = ((Number) d.xValue).doubleValue();
								double xPercentage = (xValue - minXValue) / valueRangeX;
								xBucket = computeXBucket(xPercentage);
							}

							if (yAxisCategorical) {
								yBucket = categoricalDataY.get(d.yValue);
							} else {
								double yValue = ((Number) d.yValue).doubleValue();
								double yPercentage = (yValue - minYValue) / valueRangeY;// d.getNormalizedFitness();
								yBucket = computeYBucket(yPercentage);
							}
							dat[xBucket][yBucket].accept(1 - d.getNormalizedFitness());
						}
					}

					// Draw the heatmap
					for (int x = 0; x < xSections; x++) {

						double xCoord = marginLeft + gridXOffset + sectionWidth * x; // 0.5 anti aliasing
						for (int y = 0; y < ySections; y++) {
							double yCoord = marginTop + sectionHeight * y;
							DoubleSummaryStatistics sum = dat[x][y];

							// System.out.println("x: " + x + " y: " +y + " " + sum.getAverage());
							// 0 - 1

							// If we have an entry even if it's the worst fitness still regard it.
							int cIndex = (int) Math.round(sum.getAverage() * colors);
							// int cIndex = (int) (Math.log10(sum.getAverage()*9+1)*300);

							if (cIndex == 0 && sum.getCount() > 0) {
								cIndex = 1;
							}

							gc.setFill(heatmapColors[cIndex]);
							gc.fillRect(xCoord, yCoord, sectionWidth, sectionHeight);
						}
					}

					/// Draw grid
					gc.setFill(Color.BLACK);

					double nextStringBounds = -Double.MAX_VALUE;
					for (int i = 0; i <= xSections; i++) {
						// Vertical
						double x = marginLeft + gridXOffset + sectionWidth * i; // 0.5 anti aliasing

						// Not entirely clean but we don't care about half a pixel difference. If this
						// ever becomes an issue do rounding.
						x = x - MathUtil.getFractionalPart(x) + 0.5;

						// Draw label
						if (drawGrid) {
							gc.strokeLine(x, marginTop, x, gridHeight + marginTop);
						}

						double y = gridHeight + marginTop + pad;

						// Coordinate to value

						double xPercentage = ((double) i) / xSections;

						String label = null;

						if (xAxisCategorical) {

							gc.setTextAlign(TextAlignment.CENTER);
							// Move x to the half

							for (var entry : categoricalDataX.entrySet()) {
								if (entry.getValue().equals(i)) {
									label = entry.getKey().toString();
									break;
								}
							}
							x += sectionWidth / 2;
							gc.translate(x, y);
							gc.rotate(10);
							gc.fillText(label, 0, 0);
							gc.rotate(-10);
							gc.translate(-x, -y);
							// Always print labels for categorical data
						} else {
							gc.setTextAlign(TextAlignment.LEFT);
							if (x > nextStringBounds) {
								double xVal = (xPercentage * (maxXValue - minXValue)) + minXValue;
								label = String.format("%.2f", xVal);
								gc.fillText(label, x, y);
								nextStringBounds = x + (computeStringBounds(label, axisFont).getWidth()) * 1.3;
							}
						}
					}

					for (int i = 0; i <= ySections; i++) {
						// Horizontal
						double x = marginLeft + gridXOffset;
						double xEnd = x + gridWidth;
						double y = marginTop + sectionHeight * i;

						if (drawGrid)
							gc.strokeLine(x, y, xEnd, y);

						// Label markers for X

						String label = null;
						if (yAxisCategorical) {
							y += sectionHeight / 2;
							gc.setTextAlign(TextAlignment.LEFT);
							for (var entry : categoricalDataY.entrySet()) {
								if (entry.getValue().equals(i)) {
									label = entry.getKey().toString();
									break;
								}
							}
						} else {
							gc.setTextAlign(TextAlignment.LEFT);
							double yPercentage = ((double) i) / ySections;
							double yVal = (yPercentage * (maxYValue - minYValue)) + minYValue;
							label = String.format("%.2f", yVal);
						}
						gc.fillText(label, x - rowHeadingWidth - pad, y);
					}

					// Draw numericalData points
					for (Data d : data) {
						if (populationVisibility.get(d.populationIndex)) {
							if (activeGeneration == -2 || d.generation == activeGeneration) {

								double x = xValueToPixel(d.xValue);
								double y = yValueToPixel(d.yValue) + pad;

								gc.setFill(populationColor[d.populationIndex]);
								gc.fillOval(x - symbolWidth / 2, y - symbolWidth / 2, symbolWidth, symbolWidth);
								gc.strokeOval(x - symbolWidth / 2, y - symbolWidth / 2, symbolWidth, symbolWidth);
							}
						}
					}

					// Draw legend
					double gridBottom = gridYOffset + gridHeight;

					double x = gridWidth + gridXOffset + marginLeft + pad / 2;
					gc.setFill(Color.BLACK);
					gc.fillText("Fitness\n 100%", x, gridBottom - heatmapColors.length - 20);

					for (int i = (int) gridBottom, j = 0; j < heatmapColors.length; i--, j++) {

						// CARE ABOUT LOG SCALE
						gc.setFill(heatmapColors[j]);
						gc.fillRect(x, i, 20, 2);

					}

					// Draw marker
					gc.setFill(Color.BLACK);

					if (xAxisCategorical) {
						// The index of the section which contains the best fitness
						x = xPercentageToCanvasX(minXFitnessIndex / categoricalDataX.size());
//						System.out.println(minXFitnessIndex + " " + categoricalDataX.size() + " " + x);
					} else {
						// The actually x value pointing to the mininmum value
						x = xValueToPixel(minXFitnessIndex);
					}

					gc.strokeLine(x, marginTop, x, gridHeight + marginTop);
					gc.setTextAlign(TextAlignment.CENTER);
					gc.fillText("(X): " + String.format("%.4f", minXFitnessIndex), x, marginTop - pad);
					
					double y;

					if (yAxisCategorical) {
						y = yPercentageToCanvasY(minYFitnessIndex);
					} else {
						y = yValueToPixel(minYFitnessIndex) + pad;
					}

					gc.strokeLine(gridXOffset + marginLeft, y, gridXOffset + gridWidth + marginLeft, y);
					gc.setTextAlign(TextAlignment.LEFT);
					gc.setTextBaseline(VPos.CENTER);

					gc.setStroke(Color.WHITE);
					gc.setLineWidth(4);
					String label = "(Y): " + String.format("%.4f", minYFitnessIndex);

					gc.setFill(white50);
					Bounds stringBounds = computeStringBounds(label, gc.getFont());
					gc.fillRect(gridWidth + gridXOffset + marginLeft + pad / 2, y - stringBounds.getHeight() / 2,
							stringBounds.getWidth(), stringBounds.getHeight() + 3);

					gc.strokeText(label, gridWidth + gridXOffset + marginLeft + pad / 2, y);

					gc.setLineWidth(1);
					gc.setFill(Color.BLACK);
					gc.fillText(label, gridWidth + gridXOffset + marginLeft + pad / 2, y);

					System.out.println("X: "  + x + " Y: " + y);
					
					redrawInProgress.set(false);
				} finally {
					rLock.unlock();
				}
				if (redrawRequested.compareAndSet(true, false)) {
					Platform.runLater(() -> {
						redraw();
					});
				}
			});
		} else {
			redrawRequested.set(true);
		}

	}

	/**
	 * @param xPercentage
	 * @return
	 */
	private int computeXBucket(double xPercentage) {
		// xPercentage *= 100; // 0 - 100
		int bucket = (int) (xPercentage * (xSections));
		return bucket == xSections ? bucket - 1 : bucket;
	}

	private int computeYBucket(double yPercentage) {
		int bucket = (int) (yPercentage * (ySections));
		return bucket == ySections ? bucket - 1 : bucket;
	}

	private double xValueToPixel(Object xValue) {
		double xPercentage;
		if (xAxisCategorical) {
			// Something else
			double section = categoricalDataX.get(xValue);
			xPercentage = section / categoricalDataX.size();

			// Move it a half section to the right to center it.
			xPercentage += (1 / (double) categoricalDataX.size()) / 2;

		} else {
			xPercentage = (((Number) xValue).doubleValue() - minXValue) / valueRangeX;
		}
		return xPercentageToCanvasX(xPercentage);
	}

	private double xPercentageToCanvasX(double xPercentage) {
		return gridWidth * xPercentage + gridXOffset + marginLeft;
	}

	private double yValueToPixel(Object yValue) {
		double yPercentage;
		if (yAxisCategorical) {
			// Something else
			double section = categoricalDataY.get(yValue);
			yPercentage = section / categoricalDataY.size();
			// Move it a half section to the right to center it.
			yPercentage += (1 / (double) categoricalDataY.size()) / 2;

		} else {
			yPercentage = (((Number) yValue).doubleValue() - minYValue) / valueRangeY;
		}
		return yPercentageToCanvasY(yPercentage);
	}

	private double yPercentageToCanvasY(double yPercentage) {
		// TODO margin left?
		return gridHeight * yPercentage + gridYOffset + marginLeft;
	}

	public void setActiveGeneration(int newGenToDraw) {
		this.activeGeneration = newGenToDraw;
		redraw();
	}

	// Don't we want to pass the entire individual?/ Result object instead?
	public static class Data {

		// Normal heatmap
		private Object xValue;
		private Object yValue;
		private double fitness;

		// Extended for genetic algorithm specials!. TODO distinguish these classes
		private int populationIndex;
		private int generation;

		/**
		 * @param value2
		 * @param fitness
		 */
		public Data(Object xValue, Object yValue, double fitness, int populationIndex, int generation) {
			this.xValue = xValue;
			this.yValue = yValue;
			this.fitness = fitness;
			this.populationIndex = populationIndex;
			this.generation = generation;
		}

		public double getNormalizedFitness() {
			// TODO range. 0 isn't always reached
			return (fitness - minFitness) / (maxFitness - minFitness);
		}
	}

	public Bounds computeStringBounds(String text, Font f) {
		Text t = new Text(text);
		t.setFont(f);
		Bounds tb = t.getBoundsInLocal();
		Rectangle stencil = new Rectangle(tb.getMinX(), tb.getMinY(), tb.getWidth(), tb.getHeight());
		Shape intersection = Shape.intersect(t, stencil);
		return intersection.getBoundsInLocal();
	}

	/**
	 * 
	 */
	public void resetData() {
		wLock.lock();
		this.numericalData.clear();
		maxFitness = -Double.MAX_VALUE;
		minFitness = Double.MAX_VALUE;

		minXValue = Double.MAX_VALUE;
		maxXValue = -Double.MAX_VALUE;
		valueRangeX = maxXValue - minXValue;

		minYValue = Double.MAX_VALUE;
		maxYValue = -Double.MAX_VALUE;
		valueRangeY = maxYValue - minYValue;

		categoricalDataX.clear();
		categoricalDataY.clear();

		xAxisCategorical = false;
		yAxisCategorical = false;

		this.rowHeadingWidth = 0;
		this.headingsDirty = true;
		wLock.unlock();
		redraw();
	}

	/**
	 * @param i
	 * @param selected
	 */
	public void setPopulationVisibility(int i, boolean selected) {
		if (populationVisibility.replace(i, selected) != selected) {
			redraw();
		}
	}

	/**
	 * @param value
	 */
	public void setNewStartColor(Color value) {
		startColor = value;
		recomputeColor();
	}

	/**
	 * @param value
	 */
	public void setNewEndColor(Color value) {
		endColor = value;
		recomputeColor();
	}

	/**
	 * @param value
	 */
	public void setNewNoDataColor(Color value) {
		noDataColor = value;
		recomputeColor();
	}

	private void recomputeColor() {
		heatmapColors = ColorUtil.ColorPalette.getPalette(colors + 1, startColor, endColor);
		heatmapColors[0] = noDataColor;
		redraw();
	}

}
