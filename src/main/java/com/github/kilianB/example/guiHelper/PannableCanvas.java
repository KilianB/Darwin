package com.github.kilianB.example.guiHelper;

import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.github.kilianB.MathUtil;
import com.github.kilianB.example.imageRaster.geneticAlgo.MosaicIndividual;
import com.github.kilianB.example.imageRaster.gui.ImageColor;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;

public class PannableCanvas extends Canvas{

	//Emulate panning 
	Region parent;
	Canvas thiz;
	
	private int sections;
	private MosaicIndividual currentResult;
	/*
	 * Emulate translation without settings
	 * the translate property to allow for clipping
	 * and custom zoom features.
	 */
	
	//Maybe use atomic values?
	volatile double scale = 1;
	volatile double xOffset = 0;
	volatile double yOffset = 0;
	
	
	public PannableCanvas(Region parent) {
		this.parent = parent;
		this.thiz = this;
		widthProperty().bind(parent.widthProperty());
		heightProperty().bind(parent.heightProperty());
		
		
		//Rectangle clippingNode = new Rectangle(0,0,0,0);
		//clippingNode.widthProperty().bind(this.widthProperty());
		//clippingNode.heightProperty().bind(this.heightProperty());
		//this.setClip(clippingNode);
		
		this.setCache(true);
		this.setCacheHint(CacheHint.SPEED);
		
		widthProperty()
		.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			draw();
		});
		heightProperty()
		.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
			draw();
		});
		
		
		this.addEventHandler(MouseEvent.ANY,new EventHandler<>() {
			double x = 0;
			double y = 0;

			double x1 = 0; 
			double y1 = 0;
			
			@Override
			public void handle(MouseEvent event) {
				var type = event.getEventType();
				if(type.equals(MOUSE_PRESSED)) {
					x = event.getX() - xOffset;
					y = event.getY() - yOffset;
					
					x1 = event.getX();
					y1 = event.getY();
					
				}else if(type.equals(MouseEvent.MOUSE_DRAGGED)) {
					xOffset =  (event.getX() - x);
					yOffset =  (event.getY() - y);
					//thiz.setTranslateX(thiz.getTranslateX() + event.getX() - x);
					//thiz.setTranslateY(thiz.getTranslateY() + event.getY() - y);
					draw();
				}
			}
		});
		

		this.addEventHandler(ScrollEvent.ANY,new EventHandler<>() {
			double scaleFactor = 0.001;
			final double minScale = 0.05;
			final double maxScale = 14;
			
			@Override
			public void handle(ScrollEvent event) {

				
//				double minXOld = Math.max(0, xOffset);
//				double maxXOld = Math.min(thiz.getWidth(),thiz.getWidth()*scale + xOffset);
//				double oldCenterX = (minXOld + maxXOld)/2;
//			
				/*FIX: Normalize the scale value
				 * 
				 * Scaling is not a linear function.
				 * A scaling step from 0.3 to 0.15 (delta 0.15) halfs the 
				 * perceived size of the image. A change from 1 to 1.15 on the
				 * other hand is a minimal step. Multiply by the current value to account
				 * for this factor.
				 */
				scale = MathUtil.clampNumber(scale + (event.getDeltaY()*scaleFactor*scale)
						, minScale, maxScale);
				
//				//New Center
//				double minXNew = Math.max(0, xOffset);
//				double maxXNew = Math.min(thiz.getWidth(),thiz.getWidth()*scale + xOffset);
//				double newCenterX = (minXNew + maxXNew)/2;
//			

//				thiz.setScaleX(scale);
//				thiz.setScaleY(scale);
				draw();
			}
		});
	}
	
	
	HashMap<String,Image> cachedImages = new HashMap<>();
	
	public void setIndividual(MosaicIndividual currentResult, int sections) {
		this.sections = sections;
		this.currentResult = currentResult;
		
		//The image will be drawn on the canvas load them into memory
		
		ImageColor[][] imagesToCache = currentResult.getVariable();
		
		ArrayList<String> imagePath = new ArrayList<String>();
		
		for(var arr : imagesToCache) {
			for(var imageColor : arr) {
				imagePath.add(imageColor.imgPath);
			}
		}
		
		Set<String> alreadyCachedImages = cachedImages.keySet();
		
		//Remove all images which we don't need anymore
		//TODO if image loading is incredibly expensive and takes place often
		//consider making the tradeoff of ram vs. loadtime
		alreadyCachedImages.retainAll(imagePath);

		//Update
		 imagePath.removeAll(alreadyCachedImages);
		 
		for(var stillNeedsToBeLoaded : imagePath) {
			cachedImages.put(stillNeedsToBeLoaded, new Image(stillNeedsToBeLoaded));
		}
		
		
		draw();
	}
	
	
	public void reset() {
		xOffset = 0; 
		yOffset = 0; 
		scale = 1; 
		draw();
	}
	
	public void draw() {
		
		//Set clipping bounds
		
		
		if(currentResult != null) {
		
			var graphics = this.getGraphicsContext2D();
	
			//Real values
			graphics.clearRect(0, 0, this.getWidth(),this.getHeight());
		
		
			//Get which artificial pixel are visible
			double minX = Math.max(0, xOffset);
			double maxX = Math.min(getWidth(),getWidth()*scale + xOffset);
			
			double minY = Math.max(0, yOffset);
			double maxY = Math.min(getHeight(),getHeight()*scale + yOffset);
			
//			
//			graphics.drawImage(imageToDraw, 0, 0, imageToDraw.getWidth(), 
//					imageToDraw.getHeight(), xOffset, yOffset, this.getWidth()*scale, this.getHeight()*scale);
//			
			
			// Draw sections

			double width = getWidth();
			double height = getHeight();
			
			double wPerSection = (width / sections) * scale;
			double hPerSection = (height / sections) * scale;

			// Do we want to work with a canvas or simply x times y image views?
			for (int xSection = 0; xSection < sections; xSection++) {
				
				//calculate the x position value
				double x = (int) (xSection * wPerSection) + xOffset;
				
				//Add one tile grace to avoid visible clipping
				if(x < (minX - wPerSection)) {
					continue;
				}
				if(x > maxX) {
					//As x is increasing this condition will never! be true;
					return;
				}
				
				
				for (int ySection = 0; ySection < sections; ySection++) {
					
					double y = wPerSection * ySection + yOffset;
					
					//Add one tile grace to avoid visible clipping
					if(y < (minY - hPerSection) || y > maxY) {
						continue;
					}
		
					// Image img = new Image(individual.getImageUrl(x, y));
					// graphics.drawImage(img,0, 0, img.getWidth(), img.getHeight(), x *
					// wPerSection, y * hPerSection, wPerSection, hPerSection);
					//graphics.setFill(currentResult.getImageColor((int)xSection, (int)ySection).c);
					
					//While ceil isn't mathematically correct it prevents some artifacts produced 
					//by scaling
					//graphics.fillRect(x, y, Math.ceil(wPerSection), Math.ceil(hPerSection));
					
					
		
					String imagePath = currentResult.getImageColor((int)xSection, (int)ySection).imgPath;
					Image img = cachedImages.get(imagePath);
					
					graphics.drawImage(img, 0, 0, img.getWidth(), img.getHeight(),x, y, Math.ceil(wPerSection), Math.ceil(hPerSection));
					
				
					
					
				}
			}

			
		}
	}
	
	
}
