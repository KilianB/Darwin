package com.github.kilianB.example.guiHelper;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Region;


/**
 * An extension of {@link Canvas} supporting automatic resizing to the bounds
 * of it's parent
 * @author Kilian
 *
 */
public class ResizableCanvas extends Canvas{

	Region parent;
	Canvas thiz;
	
	
	public ResizableCanvas(Region parent) {
		this.parent = parent;
		this.thiz = this;
		widthProperty().bind(parent.widthProperty());
		heightProperty().bind(parent.heightProperty());
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	public double prefWidth(double height) {
		return getWidth();
	}

	@Override
	public double prefHeight(double width) {
		return getHeight();
	}
	
}
