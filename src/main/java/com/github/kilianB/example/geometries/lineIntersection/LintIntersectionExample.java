package com.github.kilianB.example.geometries.lineIntersection;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class LintIntersectionExample extends Application{

	LineIntersectionGuiController controller = new LineIntersectionGuiController();
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		FXMLLoader loader = new FXMLLoader();
		
		loader.setController(controller);
		loader.setLocation(LintIntersectionExample.class.getResource("LineIntersectionGui.fxml"));
	
		Parent parent = loader.load();
		
		Rectangle2D screenBounds = Screen.getPrimary().getBounds();
		double width = screenBounds.getWidth() / 1.35;
		double height = screenBounds.getHeight() / 1.35;
		
		Scene scene = new Scene(parent,width,height);
		
		scene.getStylesheets().add(LintIntersectionExample.class.getResource("chart.css").toExternalForm());
		
		primaryStage.setTitle("Line Intersection Demo");
		
		//Shutdown once window is closed
		Platform.setImplicitExit(true);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		controller.afterSetup();
	}
	
	
	//Lets get the individual going

}
