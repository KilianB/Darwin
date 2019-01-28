package com.github.kilianB.geneticAlgorithm.charting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Utility methods to quickly create javafx charts tracking the genetic
 * algorithms progress
 * 
 * @author Kilian
 *
 */
public class ChartHelper {

	private static Logger LOGGER = Logger.getLogger(ChartHelper.class.getSimpleName());

	/**
	 * Auto increment id to tell apart genetic algorithms if charth helpers are used
	 * concurrently. JavaFX does not allow to pass objects therfore we have to cache
	 * it
	 */
	private static final AtomicInteger ID = new AtomicInteger(0);

	/**
	 * Cache the genetic algorithm and make it accessible to the javafx class
	 */
	private static final Map<Integer, GeneticAlgorithm> ALGORITHM_CACHE = new HashMap<>();

	/**
	 * Keep track of a semaphore to siganlize the algorithm that the gui is ready.
	 */
	private static final Map<Integer, Semaphore> WAIT_UNTIL_INIT = new HashMap<>();

	// Public methods supposed to be called by the user

	/**
	 * Sets the image icon of a javafx scene to the darwin logo
	 * 
	 * @param scene
	 */
	public static void setLogo(Stage stage) {
		stage.getIcons().add(new Image("DarwinLogoSmall.jpg"));
	}

	// Variable Inspection Pane

	/**
	 * Launches a JavaFX application and spawn a variable inspection pane with a
	 * given genetic algorithm. This class makes use of static data structures
	 * therefore it is not recommended to be called by the user.
	 * <p style="color:red;">
	 * <b>For internal use only.</b>
	 * </p>
	 * 
	 * @author Kilian
	 *
	 */
	public static class VariableInspectionPaneLauncher extends Application {
		@Override
		public void start(Stage primaryStage) throws Exception {
			Parameters params = getParameters();
			int id = Integer.parseInt(params.getRaw().get(0));
			GeneticAlgorithm geneticAlgorithm = Objects.requireNonNull(ALGORITHM_CACHE.remove(id),
					"Could not pass genetic algorithm. Null par found.");
			assembleAndShowStage(primaryStage, params.getRaw().get(1), createVariableInspectionPane(geneticAlgorithm),
					geneticAlgorithm, id);
			// Notify the thread that we are ready
		}
	}

	/**
	 * Spawns a JavaFX window plotting variable values in relation to the fitness
	 * value.
	 * 
	 * The variable inspection pane allows to inspect individual variables used by
	 * the supplied genetic algorithm.
	 * <p>
	 * Be aware that the
	 * {@link com.github.kilianB.geneticAlgorithm.Individual#getValue(int)
	 * Individual.getValue(int)} has to be implemented properly in order for this
	 * chart to work.
	 * 
	 * <p>
	 * Results will be printed as soon as the
	 * {@code com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int) caluclate(int)}
	 * method of the genetic algorithm is invoked. Due to the fact of the chart
	 * depending on the event listeners, only generations tracked by the calculate()
	 * call will be recorded in the chart.
	 * <p>
	 * Individuals may choose to implement {@link VariableDescriptor} to further
	 * specify the variable names as well as pooling categorical data to gain a
	 * better overview.
	 * 
	 * @param title the title of the window
	 * @param ga    The genetic algorithm this application shall track
	 */
	public static void displayVarInspectionPane(String title, GeneticAlgorithm ga) {

		int id = ID.getAndIncrement();
		/*
		 * Block until gui is ready. Else we will loose updates
		 */
		Semaphore barrier = new Semaphore(0);
		WAIT_UNTIL_INIT.put(id, barrier);
		ALGORITHM_CACHE.put(id, ga);

		// Application launch blocks indefinitely. But we don't want blocking behavior
		// for this method.
		new Thread(() -> {
			try {
				Application.launch(VariableInspectionPaneLauncher.class, String.valueOf(id), title);
			} catch (IllegalStateException e) {
				// JavaFX already launched.
				Platform.runLater(() -> {
					assembleAndShowStage(new Stage(), title, createVariableInspectionPane(ga), ga, id);
					ALGORITHM_CACHE.remove(id);
					WAIT_UNTIL_INIT.remove(id);
				});
			}
		}, "Inspection Pane Thread").start();

		// Else we can't do much.
		if (!Platform.isFxApplicationThread()) {
			// Gui is ready
			try {
				barrier.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			LOGGER.warning(
					"displayVarInspectionPane should not be called from within the JavaFX Application thread. The pane might miss the first few updates");
		}

	}

	/**
	 * Spawns a JavaFX window plotting variable values in relation to the fitness
	 * value.
	 * 
	 * The variable inspection pane allows to inspect individual variables used by
	 * the supplied genetic algorithm.
	 * <p>
	 * Be aware that the
	 * {@link com.github.kilianB.geneticAlgorithm.Individual#getValue(int)
	 * Individual.getValue(int)} has to be implemented properly in order for this
	 * chart to work.
	 * 
	 * <p>
	 * Results will be printed as soon as the
	 * {@code com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int) caluclate(int)}
	 * method of the genetic algorithm is invoked. Due to the fact of the chart
	 * depending on the event listeners, only generations tracked by the calculate()
	 * call will be recorded in the chart.
	 * <p>
	 * Individuals may choose to implement {@link VariableDescriptor} to further
	 * specify the variable names as well as pooling categorical data to gain a
	 * better overview.
	 * 
	 * @param ga The genetic algorithm this application shall track
	 */
	public static void displayVarInspectionPane(GeneticAlgorithm ga) {
		displayVarInspectionPane("", ga);
	}

	/**
	 * Create a variable inspection pane to inspect individual variables used by the
	 * supplied genetic algorithm. This method returns a node which has to be
	 * embedded into a running JavaFX application.
	 * 
	 * <p>
	 * Due to the pane being based on a canvas the node <b>does not specify</b> a
	 * proper preferred width and height. In order for the node to be displayed the
	 * pref width/height has to be set manually or the node has to be embedded into
	 * a pane which takes the responsibility to layout it's children.
	 * 
	 * <p>
	 * Be aware that the
	 * {@link com.github.kilianB.geneticAlgorithm.Individual#getValue(int)
	 * Individual.getValue(int)} has to be implemented properly in order for this
	 * chart to work.
	 * 
	 * <p>
	 * Results will be printed as soon as the
	 * {@code com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int) caluclate(int)}
	 * method of the genetic algorithm is invoked. Due to the fact of the chart
	 * depending on the event listeners, only generations tracked by the calculate()
	 * call will be recorded in the chart.
	 * 
	 * <p>
	 * To launch a stand alone javafx application call
	 * {@link #displayVarInspectionPane(GeneticAlgorithm)}.
	 * 
	 * <p>
	 * Individuals may choose to implement {@link VariableDescriptor} to further
	 * specify the variable names as well as pooling categorical data to gain a
	 * better overview.
	 * 
	 * @param ga The genetic algorithm whose variables will be plotted.
	 * @return the variable inspection pane
	 */
	public static Node createVariableInspectionPane(GeneticAlgorithm ga) {
		try {
			return createVariableInspectionPane(ga, null);
		}catch(java.lang.ExceptionInInitializerError e) {
			LOGGER.severe("Exception In Initializer error while creation inspection pane. Are you sure that you did not want to call displayVariableInspectionPane?");
			throw e;
		}
	}

	/**
	 * Create a variable inspection pane to inspect individual variables used by the
	 * supplied genetic algorithm. This method returns a node which has to be
	 * embedded into a running JavaFX application.
	 * 
	 * <p>
	 * Due to the pane being based on a canvas the node <b>does not specify</b> a
	 * proper preferred width and height. In order for the node to be displayed the
	 * pref width/height has to be set manually or the node has to be embedded into
	 * a pane which takes the responsibility to layout it's children.
	 * 
	 * <p>
	 * Be aware that the
	 * {@link com.github.kilianB.geneticAlgorithm.Individual#getValue(int)
	 * Individual.getValue(int)} has to be implemented properly in order for this
	 * chart to work.
	 * 
	 * <p>
	 * Results will be printed as soon as the
	 * {@code com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int) caluclate(int)}
	 * method of the genetic algorithm is invoked. Due to the fact of the chart
	 * depending on the event listeners, only generations tracked by the calculate()
	 * call will be recorded in the chart.
	 * 
	 * <p>
	 * To launch a stand alone javafx application call
	 * {@link #displayVarInspectionPane(GeneticAlgorithm)}.
	 * 
	 * <p>
	 * The implementation of {@link VariableDescriptor} is ignored if
	 * variableDescription is supplied.
	 * 
	 * @param ga                  The genetic algorithm whose variables will be
	 *                            plotted.
	 * @param variableDescription string representation used to display the names of
	 *                            the variables
	 * 
	 * 
	 * @return the variable inspection pane
	 */
	public static Node createVariableInspectionPane(GeneticAlgorithm ga, String[] variableDescription) {
		return new VariableInspectionPane(variableDescription, ga);
	}

	/*
	 * Progress pane
	 */

	/**
	 * Launches a JavaFX application and spawn a progress pane with a given genetic
	 * algorithm. This class makes use of static data structures therefore it is not
	 * recommended to be called by the user.
	 * <p style="color:red;">
	 * <b>For internal use only.</b>
	 * </p>
	 * 
	 * @author Kilian
	 *
	 */
	public static class ProgressPaneLauncher extends Application {
		@Override
		public void start(Stage primaryStage) throws Exception {
			Parameters params = getParameters();
			int id = Integer.parseInt(params.getRaw().get(0));
			GeneticAlgorithm geneticAlgorithm = Objects.requireNonNull(ALGORITHM_CACHE.remove(id),
					"Could not pass genetic algorithm. Null par found.");

			boolean displayAverage = params.getRaw().get(2).equals("0") ? true : false;
			String title = params.getRaw().get(3);
			boolean logScale = params.getRaw().get(4).equals("0") ? true : false;

			assembleAndShowStage(primaryStage, params.getRaw().get(1),
					createProgressChart(title, geneticAlgorithm, displayAverage, logScale), geneticAlgorithm, id);
			// Notify the thread that we are ready
		}
	}

	/**
	 * Creates a progress chart which displays the fitness values of the individual
	 * sub populations.
	 * 
	 * <p>
	 * The returned node has to be embedded into a javaFx scene.
	 * 
	 * <p>
	 * Results will be printed as soon as the
	 * {@code com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int) caluclate(int)}
	 * method of the genetic algorithm is invoked. Due to the fact of the chart
	 * depending on the event listeners, only generations tracked by the calculate()
	 * call will be recorded in the chart.
	 * 
	 * 
	 * 
	 * @param title          The title of the window
	 * @param algorithm      the algorithm to track
	 * @param displayAverage Shall only the average fitness be plotted or the
	 *                       fitness of each individual.Be aware that depending on
	 *                       the amount of individuals per generation and the
	 *                       granularity of tracked generations drawing each
	 *                       individual may result in a huge performance penalty
	 * @param logScale       if true the fitness value will be displayed on a log
	 *                       scale
	 * @return the progress chart
	 */
	public static Node createProgressChart(String title, GeneticAlgorithm algorithm, boolean displayAverage,
			boolean logScale) {
		try {
			return new ProgressChart(title, algorithm, displayAverage, logScale);
		}catch(java.lang.ExceptionInInitializerError e) {
			LOGGER.severe("Exception In Initializer error while creation progress chart. Are you sure that you did not want to call displayProgressPane?");
			throw e;
		}
		
		
	}

	/**
	 * Spawns a progress chart which displays the fitness values of the individual
	 * sub populations.
	 * 
	 * <p>
	 * Results will be printed as soon as the
	 * {@code com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#calculate(int) caluclate(int)}
	 * method of the genetic algorithm is invoked. Due to the fact of the chart
	 * depending on the event listeners, only generations tracked by the calculate()
	 * call will be recorded in the chart.
	 * 
	 * 
	 * 
	 * @param title          The title of the window
	 * @param algorithm      the algorithm to track
	 * @param displayAverage Shall only the average fitness be plotted or the
	 *                       fitness of each individual.Be aware that depending on
	 *                       the amount of individuals per generation and the
	 *                       granularity of tracked generations drawing each
	 *                       individual may result in a huge performance penalty
	 * @param logScale       if true the fitness value will be displayed on a log
	 *                       scale
	 */
	public static void displayProgressPane(String title, GeneticAlgorithm ga, boolean displayAverage,
			boolean logScale) {

		int id = ID.getAndIncrement();

		System.out.println("id: " + id);
		/*
		 * Block until gui is ready. Else we will loose updates
		 */
		Semaphore barrier = new Semaphore(0);
		WAIT_UNTIL_INIT.put(id, barrier);
		ALGORITHM_CACHE.put(id, ga);

		// Application launch blocks indefinitely. But we don't want blocking behavior
		// for this method.
		new Thread(() -> {
			try {
				Application.launch(ProgressPaneLauncher.class, String.valueOf(id), title, displayAverage ? "0" : "1",
						title, logScale ? "0" : "1");
			} catch (IllegalStateException e) {
				Platform.runLater(() -> {
					assembleAndShowStage(new Stage(), title, createProgressChart(title, ga, displayAverage, logScale),
							ga, id);
					ALGORITHM_CACHE.remove(id);
					WAIT_UNTIL_INIT.remove(id);
				});
			}
		}, "Inspection Pane Thread").start();

		// Else we can't do much.
		if (!Platform.isFxApplicationThread()) {
			// Gui is ready
			try {
				barrier.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			LOGGER.warning(
					"displayProgressPane should not be called from within the JavaFX Application thread. The pane might miss the first few updates");
		}

	}

	// Private utility method

	/**
	 * Helper class embedding a node into a javafx stage and releasing the genetic
	 * algorithm indicating that the gui is ready for the ga.
	 * 
	 * @param primaryStage     The stage to embed the node into
	 * @param title            the title of the window
	 * @param content          the node to embed
	 * @param geneticAlgorithm the algorithm used to create the node
	 * @param id               the id associated with the request
	 */
	private static void assembleAndShowStage(Stage primaryStage, String title, Node content,
			GeneticAlgorithm geneticAlgorithm, int id) {
		// Enable JVM shutdown on window close
		Platform.setImplicitExit(true);

		BorderPane parent = new BorderPane();
		parent.setId("root");

		Rectangle2D monitorBounds = Screen.getPrimary().getBounds();
		double width = monitorBounds.getWidth() / 1.3;
		double height = monitorBounds.getHeight() / 1.3;
		Scene scene = new Scene(parent, width, height);

		parent.setPrefWidth(width);
		parent.setPrefHeight(height);
		parent.setCenter(content);

		setLogo(primaryStage);

		primaryStage.setScene(scene);
		primaryStage.setTitle(title);
		primaryStage.show();

		// Signalize image is ready
		WAIT_UNTIL_INIT.get(id).release();
	}

	/**
	 * Data class to bind variable indices to a string representation.
	 * 
	 * @author Kilian
	 *
	 */
	public static class VariableIndex {

		int index;
		String description;

		public VariableIndex(int index) {
			this.index = index;
		}

		public VariableIndex(int index, String description) {
			super();
			this.index = index;
			this.description = description;
		}

		public String toString() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * @return the index
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}
	}
}
