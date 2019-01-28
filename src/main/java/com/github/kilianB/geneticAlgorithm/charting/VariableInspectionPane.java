package com.github.kilianB.geneticAlgorithm.charting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.kilianB.MathUtil;
import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.charting.ChartHelper.VariableIndex;
import com.github.kilianB.geneticAlgorithm.charting.Heatmap.Data;
import com.github.kilianB.geneticAlgorithm.result.Result;
import com.github.kilianB.geneticAlgorithm.result.ResultListener;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXColorPicker;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * @author Kilian
 *
 */
public class VariableInspectionPane extends BorderPane {

	Heatmap heatmap;
	JFXSlider generationSlider;
	Result lastResult;
	String[] variableDescription = null;
	Set<Integer> availableGenerations;

	JFXButton prevButton = new JFXButton("Prev");
	JFXButton playButton = new JFXButton("Start");
	JFXButton nextButton = new JFXButton("Next");
	/**
	 * CSS Class for all section label headers in the heatmap gui
	 */
	private static final String HEADING_CSS = "subHeading";

	public VariableInspectionPane(String[] variableDescription, GeneticAlgorithm ga) {
		setMinWidth(500);
		setMinHeight(300);

		// Add style sheet
		getStylesheets().add(this.getClass().getClassLoader().getResource("Darwin.css").toExternalForm());
		getStyleClass().add("varInspectionRoot");

		this.variableDescription = variableDescription;

		Pane hWrapper = new Pane();
		setCenter(hWrapper);

		heatmap = new Heatmap(hWrapper, 40, 40, ga.getSubPopulationCount());
		hWrapper.getChildren().add(heatmap);

		ObservableList<VariableIndex> obsX = FXCollections.observableArrayList();
		ObservableList<VariableIndex> obsY = FXCollections.observableArrayList();

		obsX.add(new VariableIndex(-1, "Fitness"));
		obsY.add(new VariableIndex(-1, "Fitness"));

		// Right menu
		JFXComboBox<String> heatmapColor = new JFXComboBox<>(
				FXCollections.observableArrayList("Logarithmic", "Linear"));
		heatmapColor.setLabelFloat(true);
		heatmapColor.setPromptText("Heatmap Color");
		heatmapColor.getSelectionModel().select(0);
		heatmapColor.getStyleClass().add(HEADING_CSS);

		VBox box = new VBox(15);
		JFXComboBox<VariableIndex> xAxisVariableBox = new JFXComboBox<>(obsX);
		xAxisVariableBox.setPromptText("X Axis");
		xAxisVariableBox.setLabelFloat(true);

		JFXComboBox<VariableIndex> yAxisVariableBox = new JFXComboBox<>(obsY);
		yAxisVariableBox.setPromptText("Y Axis");
		yAxisVariableBox.setLabelFloat(true);

		//xAxisVariableBox.getSelectionModel().select(0);
		//yAxisVariableBox.getSelectionModel().select(0);
		// InvalidationObjectProperty<Result> lastResult = new

		ga.addResultListener(new ResultListener() {

			boolean init = false;

			@Override
			public void intermediateResult(Result r) {
				// Setup
				if (!init) {
					init = true;
					lastResult = r;
					availableGenerations = r.getAvailableGenerationsSet();
					Platform.runLater(() -> {

						Individual sampleIndividual = r.getBestResult();

						String[] varDescription = null;

						// Var description should be bundled with the individual as they are highly
						// depended on each other. Since we also work with prototypes we can not control
						// (e.g. numeric give
						// the user the opportunity to send a custom string array.

						if (variableDescription != null) {
							varDescription = variableDescription;
						} else if (sampleIndividual instanceof VariableDescriptor) {
							varDescription = ((VariableDescriptor) sampleIndividual).getVariableDescription();
						}

						int varCount = sampleIndividual.getVariableCount();
						for (int i = 0; i < varCount; i++) {

							VariableIndex vIndex = new VariableIndex(i);

							if (varDescription != null && varDescription.length >= i) {
								vIndex.setDescription(varDescription[i]);
							} else {
								if (MathUtil.isNumeric(sampleIndividual.getValue(i))) {
									vIndex.setDescription("Var: " + Integer.toString(i) + " Numeric");

								} else {
									vIndex.setDescription("Var: " + Integer.toString(i) + " Factor");
								}
							}
							obsX.add(vIndex);
							obsY.add(vIndex);
						}
					});
					updateHeatmap(0, 0, true, -1);

				} else {
					updateHeatmapAndSlider(r.getGenerationCount(),
							xAxisVariableBox.getSelectionModel().getSelectedIndex(),
							yAxisVariableBox.getSelectionModel().getSelectedIndex(),
							heatmapColor.getSelectionModel().getSelectedIndex() == 0);
				}
			}

			@Override
			public void finalResult(Result r) {
				Platform.runLater(()->{
					xAxisVariableBox.getSelectionModel().select(0);
					yAxisVariableBox.getSelectionModel().select(0);
					
				});
				updateHeatmapAndSlider(r.getGenerationCount(), 0, 0,
						heatmapColor.getSelectionModel().getSelectedIndex() == 0);
			}

		});

		EventHandler<ActionEvent> resetHeatmap = new EventHandler<>() {
			@Override
			public void handle(ActionEvent event) {
				VariableIndex x = xAxisVariableBox.getSelectionModel().getSelectedItem();
				VariableIndex y = yAxisVariableBox.getSelectionModel().getSelectedItem();

				boolean logScale = heatmapColor.getSelectionModel().getSelectedIndex() == 0;
				// TODO
				resetAndUpdateHeatmap(x != null ? x.getIndex() : -1, y != null ? y.getIndex() : -1, logScale);
			}
		};

		yAxisVariableBox.setOnAction(resetHeatmap);
		xAxisVariableBox.setOnAction(resetHeatmap);
		heatmapColor.setOnAction(resetHeatmap);

		box.getChildren().addAll(xAxisVariableBox, yAxisVariableBox);
		VBox.setMargin(xAxisVariableBox, new Insets(10, 0, 0, 0));

		// Checkbox toggle sub population visibility

		Label heading = new Label("Sub Population Visbilility");
		heading.getStyleClass().add(HEADING_CSS);
		// heading.setPadding(HEADING_INSET);

		box.getChildren().add(heading);
		for (int i = 0; i < ga.getSubPopulationCount(); i++) {
			JFXCheckBox subPopVisibilitity = new JFXCheckBox("Population: " + i);
			subPopVisibilitity.setSelected(true);
			int j = i;
			subPopVisibilitity.setOnAction((event) -> {
				heatmap.setPopulationVisibility(j, subPopVisibilitity.isSelected());
			});
			box.getChildren().add(subPopVisibilitity);
		}

		Label colorHeading = new Label("Heatmap Colors:");
		colorHeading.getStyleClass().add(HEADING_CSS);
		JFXColorPicker startHeatmapColor = new JFXColorPicker(Color.GREEN);
		JFXColorPicker endHeatmapColor = new JFXColorPicker(Color.RED);
		JFXColorPicker noDataColor = new JFXColorPicker(Color.GRAY);

		startHeatmapColor.setMinHeight(23);
		endHeatmapColor.setMinHeight(23);
		noDataColor.setMinHeight(23);

		Label generationHeading = new Label("Generation: All");
		generationHeading.getStyleClass().add(HEADING_CSS);
		generationSlider = new JFXSlider(-2, 0, -2);

		startHeatmapColor.setOnAction((event) -> {
			System.out.println("Start new color: " + startHeatmapColor.getValue());
			heatmap.setNewStartColor(startHeatmapColor.getValue());
		});

		endHeatmapColor.setOnAction((event) -> {
			heatmap.setNewEndColor(endHeatmapColor.getValue());
		});

		noDataColor.setOnAction((event) -> {
			heatmap.setNewNoDataColor(noDataColor.getValue());
		});

		generationSlider.valueProperty().addListener((obs, oldVal, newValue) -> {
			int gen = newValue.intValue();

			String label;
			if (gen == -2) {
				label = "All";
				prevButton.setDisable(true);
			} else if (gen == -1) {
				label = "Initial";
			} else {
				// label = Integer.toString(gen);
				if (availableGenerations.contains(gen)) {
					label = Integer.toString(gen);
				} else {
					// Reset slider not allowed value //TODO be aware of infinite loop
					if (availableGenerations.contains(oldVal.intValue())) {
						generationSlider.setValue((double) oldVal.intValue());
					} else {
						// a probably better approach is to construct a tree set and find the closest
						// valid value
						int dif = gen % lastResult.getGenerationStep();

						if (dif > lastResult.getGenerationStep() / 2d) {
							gen += (lastResult.getGenerationStep() - dif);
						} else {
							gen -= dif;
						}
					}
					return;
				}
			}

			if (gen != -2) {
				if (prevButton.isDisabled()) {
					prevButton.setDisable(false);
				}
			}

			if (gen == lastResult.getGenerationCount()) {
				nextButton.setDisable(true);
			} else if (nextButton.isDisabled()) {
				nextButton.setDisable(false);
			}

			heatmap.setActiveGeneration(gen);
			generationHeading.setText("Generation: " + label);
		});

		box.setMaxWidth(150);

		box.getChildren().addAll(colorHeading, startHeatmapColor, endHeatmapColor, noDataColor, heatmapColor,
				generationHeading, generationSlider, buildButtonSidebar());

		box.setAlignment(Pos.CENTER_LEFT);
		ScrollPane sp = new ScrollPane(box);
		sp.setMaxWidth(155);
		sp.setHbarPolicy(ScrollBarPolicy.NEVER);
		sp.setFitToHeight(true);
		sp.setFitToWidth(true);
		sp.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
		setRight(sp);

	}

	private Node buildButtonSidebar() {

		prevButton.setDisable(true);

		FlowPane buttonFlowPane = new FlowPane(prevButton, playButton, nextButton);

		prevButton.setOnAction((event) -> {
			int value = (int) generationSlider.getValue();

			// if we finished the max may have a different step value;

			int max = lastResult.getGenerationCount();
			if (value == 0 || value == -1) {
				value--;
			} else if (value == max) {
				int dif = value % lastResult.getGenerationStep();
				value -= dif != 0 ? dif : lastResult.getGenerationStep();
			} else {
				value -= lastResult.getGenerationStep();
			}
			generationSlider.setValue(value);
		});

		nextButton.setOnAction((event) -> {

			int value = (int) generationSlider.getValue();
			int max = lastResult.getGenerationCount();
			if (value == -2 || value == -1) {
				value++;
			} else {
				value += lastResult.getGenerationStep();
			}

			if (value > max) {
				value = max;
			}
			generationSlider.setValue(value);
		});

		Timeline t = new Timeline();
		t.setOnFinished((event) -> {
			playButton.setText("Start");
		});

		playButton.setOnAction(event -> {

			if (!t.getStatus().equals(Animation.Status.RUNNING)) {

				// If we are at the very end start from the beginning.
				if ((int) generationSlider.getValue() == lastResult.getGenerationCount()) {
					generationSlider.setValue(0);
				}

				Interpolator inter = new Interpolator() {

					int startOffset = Integer.MIN_VALUE;
					List<Integer> allowedValues = new ArrayList<>(availableGenerations);

					@Override
					protected double curve(double t) {
						// Linear interpolation..
						return t;
					}

					public double interpolate(double startValue, double endValue, double fraction) {
						if (startOffset == Integer.MIN_VALUE) {
							startOffset = allowedValues.indexOf((int) startValue);
						}
						int len = availableGenerations.size();
						int index = (int) Math.round((len - startOffset) * fraction);
						index += startOffset;
						if (index == len) {
							index--;
						} else if (index < 0) {
							index = 0;
						}
						return allowedValues.get(index);
					}

				};

				int generationFrames = (int) (generationSlider.getMax() - generationSlider.getValue());
				KeyValue animateGeneration = new KeyValue(generationSlider.valueProperty(), generationSlider.getMax(),
						inter);

				KeyFrame k = new KeyFrame(Duration.millis(generationFrames * 25), animateGeneration);

				t.getKeyFrames().clear();
				t.getKeyFrames().add(k);
				t.play();
				playButton.setText("Stop");
			} else {
				t.stop();
				playButton.setText("Start");
			}

		});

		return buttonFlowPane;
	}

	private void updateHeatmapAndSlider(int curGeneration, int curVarIndexX, int curVarIndexY, boolean logScale) {
		updateHeatmap(curVarIndexX, curVarIndexY, logScale, curGeneration);
		generationSlider.setMax(curGeneration);
		generationSlider.setSnapToTicks(true);
		generationSlider.setMajorTickUnit(5);
		generationSlider.setBlockIncrement(5);
		generationSlider.setMinorTickCount(0);
	};

	private void updateHeatmap(int curVarIndexX, int curVarIndexY, boolean logScale, int generation) {

		List<Data> dataToAdd = new ArrayList<>();

		boolean wrapX = false;
		boolean wrapY = false;

		Individual sample = lastResult.getBestResult();
		if (sample instanceof VariableDescriptor) {
			wrapX = curVarIndexX < 0 ? false : ((VariableDescriptor) sample).getGroupByClasses()[curVarIndexX];
			wrapY = curVarIndexY < 0 ? false : ((VariableDescriptor) sample).getGroupByClasses()[curVarIndexY];
		}

		for (int popIndex = 0; popIndex < lastResult.getSubPopulationCount(); popIndex++) {
			Individual[] mostRecentGen = lastResult.getGeneration(generation, popIndex);
			for (Individual i : mostRecentGen) {
				Object xValue = curVarIndexX >= 0 ? i.getValue(curVarIndexX) : i.getFitness();
				Object yValue = curVarIndexY >= 0 ? i.getValue(curVarIndexY) : i.getFitness();

				if (wrapX) {
					xValue = new UnifyClassHashWrapper(xValue);
				}
				if (wrapY) {
					yValue = new UnifyClassHashWrapper(yValue);
				}
				dataToAdd.add(new Data(xValue, yValue, logScale ? Math.log10(1 + i.getFitness()) : i.getFitness(),
						popIndex, generation));
			}
		}
		heatmap.addDataBatch(dataToAdd);
	};

	private void resetAndUpdateHeatmap(int curVarIndexX, int curVarIndexY, boolean logScale) {

		// This is a longer operation do it outside the FX Application thread
		new Thread(() -> {

			heatmap.resetData();

			boolean wrapX = false;
			boolean wrapY = false;

			Individual sample = lastResult.getBestResult();
			if (sample instanceof VariableDescriptor) {
				wrapX = curVarIndexX < 0 ? false : ((VariableDescriptor) sample).getGroupByClasses()[curVarIndexX];
				wrapY = curVarIndexY < 0 ? false : ((VariableDescriptor) sample).getGroupByClasses()[curVarIndexY];
			}

			List<Data> dataToAdd = new ArrayList<>();

			List<Integer> generations = lastResult.getAvailableGenerations();

			for (int generation : generations) {
				for (int popIndex = 0; popIndex < lastResult.getSubPopulationCount(); popIndex++) {
					Individual[] mostRecentGen = lastResult.getGeneration(generation, popIndex);
					for (Individual i : mostRecentGen) {
						Object xValue = curVarIndexX >= 0 ? i.getValue(curVarIndexX) : i.getFitness();
						Object yValue = curVarIndexY >= 0 ? i.getValue(curVarIndexY) : i.getFitness();

						if (wrapX) {
							xValue = new UnifyClassHashWrapper(xValue);
						}
						if (wrapY) {
							yValue = new UnifyClassHashWrapper(yValue);
						}

						dataToAdd.add(new Data(xValue, yValue,
								logScale ? Math.log10(1 + i.getFitness()) : i.getFitness(), popIndex, generation));
					}
				}
			}
			heatmap.addDataBatch(dataToAdd);
		}).start();

	};
}
