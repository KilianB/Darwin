package com.github.kilianB.example.guiHelper;

import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.github.kilianB.ArrayUtil;
import com.github.kilianB.MathUtil;
import com.github.kilianB.example.imageRaster.geneticAlgo.MosaicIndividual;
import com.github.kilianB.example.imageRaster.gui.CircularLinkedHashMap;
import com.github.kilianB.example.imageRaster.gui.ImageColor;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class PannableImageViews extends Pane {

	// Emulate panning
	Region parent;

	private int sections;
	private MosaicIndividual currentResult;
	/*
	 * Emulate translation without settings
	 * the translate property to allow for clipping
	 * and custom zoom features.
	 */

	// Maybe use atomic values?
	volatile double scale = 1;
	//volatile double xOffset = 0;
	//volatile double yOffset = 0;
	
	SimpleDoubleProperty xOffset = new SimpleDoubleProperty(0);
	SimpleDoubleProperty yOffset = new SimpleDoubleProperty(0);

	public PannableImageViews(Region parent) {

		this.parent = parent;

		// this.widthProperty().
		// widthProperty().bind(parent.widthProperty());
		// heightProperty().bind(parent.heightProperty());

		this.prefWidthProperty().bind(parent.widthProperty());;
		this.prefHeightProperty().bind(parent.heightProperty());;
		
		//Clipping does NOT! affect fps performance
//		Rectangle clippingNode = new Rectangle(0, 0, 0, 0);
//		clippingNode.widthProperty().bind(this.widthProperty());
//		clippingNode.heightProperty().bind(this.heightProperty());
//		this.setClip(clippingNode);

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

		this.addEventHandler(MouseEvent.ANY, new EventHandler<>() {
			double x = 0;
			double y = 0;
			@Override
			public void handle(MouseEvent event) {
				var type = event.getEventType();
				if (type.equals(MOUSE_PRESSED)) {
					x = event.getX() - xOffset.get();
					y = event.getY() - yOffset.get();

				} else if (type.equals(MouseEvent.MOUSE_DRAGGED)) {
					xOffset.set(event.getX() - x);
					yOffset.set(event.getY() - y);
					draw();
				}
			}
		});

		this.addEventHandler(ScrollEvent.ANY, new EventHandler<>() {
			private double scaleFactor = 0.001;
			private final double MIN_SCALE_FACTOR = 0.05;
			private final double MAX_SCALE_FACTOR = 100;

			@Override
			public void handle(ScrollEvent event) {

				
//				double minX = Math.max(0, xOffset.get());
//				double maxX = Math.min(getWidth(),(getWidth()*scale+xOffset.get()));
//				
//				double minY = Math.max(0, yOffset.get());
//				double maxY = Math.min(getHeight(),(getHeight())*scale + yOffset.get());
//				
				
				/*FIX: Normalize the scale value
				 * 
				 * Scaling is not a linear function.
				 * A scaling step from 0.3 to 0.15 (delta 0.15) halfs the 
				 * perceived size of the image. A change from 1 to 1.15 on the
				 * other hand is a minimal step. Multiply by the current value to account
				 * for this factor.
				 */
				scale = MathUtil.clampNumber(scale + (event.getDeltaY() * scaleFactor * scale), MIN_SCALE_FACTOR, MAX_SCALE_FACTOR);

				// //New Center
				// double minXNew = Math.max(0, xOffset);
				// double maxXNew = Math.min(thiz.getWidth(),thiz.getWidth()*scale + xOffset);
				// double newCenterX = (minXNew + maxXNew)/2;
				//

				// thiz.setScaleX(scale);
				// thiz.setScaleY(scale);
				draw();
			}
		});
	}

	
	HashMap<String, Image> cachedImagesLQ = new HashMap<>();
	HashMap<String, Image> cachedImagesMQ = new HashMap<>();
	CircularLinkedHashMap<String, Image> cachedImagesHQ = new CircularLinkedHashMap<>(15);
	CircularLinkedHashMap<String, Image> cachedImagesMaxQuality = new CircularLinkedHashMap<>(4);
	
	ImageColor[][] imagesToCache;
	
	ArrayList<ImageView> imageViews = new ArrayList<>();
	
	
	public void setIndividual(MosaicIndividual currentResult, int sections) {
		this.sections = sections;
		this.currentResult = currentResult;
		
		double width = parent.getWidth();
		double height = parent.getHeight() ;
		
		double wPerSection = (width / sections) * scale;
		double hPerSection = (height / sections) * scale;
		

		// The image will be drawn on the canvas load them into memory

		imagesToCache = currentResult.getVariable();

		ArrayList<String> imagePath = new ArrayList<String>();

		for (var arr : imagesToCache) {
			for (var imageColor : arr) {
				imagePath.add(imageColor.imgPath);
			}
		}

		//Or can we cache the medium quality images on the fly?
		
		Set<String> alreadyCachedImagesHQ = cachedImagesHQ.keySet();
		Set<String> alreadyCachedImagesLQ = cachedImagesLQ.keySet();
		Set<String> alreadyCachedImagesMQ = cachedImagesMQ.keySet();

		
		
		// Remove all images which we don't need anymore
		// TODO if image loading is incredibly expensive and takes place often
		// consider making the tradeoff of ram vs. loadtime
		alreadyCachedImagesHQ.retainAll(imagePath);
		alreadyCachedImagesLQ.retainAll(imagePath);
		alreadyCachedImagesMQ.retainAll(imagePath);
		
		// Update
		imagePath.removeAll(alreadyCachedImagesLQ);

		
		//TODO multi thread
		
		//Can we speed this up by multi threading instead of background loading?
		//Javafx throttles image loading to 4 at a time to prevent heap issues. but is this 
		//still an issue for for java 10+ where heap is artificially increased as needed?
		//ArrayList<Future<Void>> imageLoadingTasks = new ArrayList<>();
		//ExecutorService pool =  Executors.newCachedThreadPool();
		
		//Bottleneck 29 secs by 40 decreased to 27 by parallel

		//TODO concurrent hashmaps?
		for (var stillNeedsToBeLoaded : imagePath) {
			//Load high quality pictures on the fly
			//cachedImages.put(stillNeedsToBeLoaded, new Image(stillNeedsToBeLoaded));
			
			cachedImagesLQ.put(stillNeedsToBeLoaded, new Image(stillNeedsToBeLoaded,wPerSection,hPerSection,false,false,true));
			cachedImagesMQ.put(stillNeedsToBeLoaded, new Image(stillNeedsToBeLoaded,wPerSection*4,hPerSection*4,false,false,true));
//			imageLoadingTasks.add(
//					pool.submit(()->{
//						
//						return null;
//					})
//			);
//			imageLoadingTasks.add(
//					pool.submit(()->{
//						
//						return null;
//					})
//			);
		}

		//Wait for all images to be loaded
//		for(var task : imageLoadingTasks) {
//			try {
//				task.get();
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//			}
//		}

		//Create the image views and lay them out
		
		int imageCount = sections*sections;
		
		for(int i = imageViews.size(); i < imageCount; i++) {
			imageViews.add(new ImageView());
		}
		
		for(int i = imageViews.size(); i > imageCount; i--) {
			imageViews.remove(i);
		}
		
		Platform.runLater(()->{
			this.getChildren().clear();
			this.getChildren().addAll(imageViews);
			//layout
			
	
			
			for(int xSection = 0; xSection < sections; xSection++) {
				double x = (int) (xSection * wPerSection);
				for(int ySection = 0; ySection < sections; ySection++) {
					double y = wPerSection * ySection;
					int index = ArrayUtil.twoDimtoOneDim(xSection, ySection, sections);
					
					ImageView imgView = imageViews.get(index);
					//TODO!
					imgView.setPreserveRatio(false);
					imgView.translateXProperty().bind(xOffset);
					imgView.translateYProperty().bind(yOffset);
					imgView.setLayoutX(x);
					imgView.setLayoutY(y);
					imgView.setFitHeight(Math.ceil(hPerSection));
					imgView.setFitWidth(Math.ceil(wPerSection));
					imgView.setImage(cachedImagesLQ.get(imagesToCache[xSection][ySection].imgPath));
					imgView.setSmooth(false);
					imgView.setCache(true);
					imgView.setCacheHint(CacheHint.SPEED);
				}
			}
		});
	}

	public void reset() {
		xOffset.set(0);;
		yOffset.set(0);;
		scale = 1;
		draw();
	}
	
	public void draw(/*boolean scaleUpdate*/) {
		
		double width = parent.getWidth();
		double height = parent.getHeight() ;
		
		double wPerSection = (width / sections) * scale;
		double hPerSection = (height / sections) * scale;
		
		//Allow 1 grid grave period for smooth transition
		double minX = Math.max(0, xOffset.get()) - wPerSection;
		double maxX = Math.min(getWidth(),(getWidth()*scale+xOffset.get()));
		
		double minY = Math.max(0, yOffset.get()) - hPerSection;
		double maxY = Math.min(getHeight(),(getHeight())*scale + yOffset.get());
		
		
		int imagesShownOnScreen = (int) Math.ceil((width/wPerSection * height/hPerSection));
		
		if(currentResult != null) {
			for(int xSection = 0; xSection < sections; xSection++) {
				double x = (int) (xSection * wPerSection);
				for(int ySection = 0; ySection < sections; ySection++) {
					double y = wPerSection * ySection;
					int index = ArrayUtil.twoDimtoOneDim(xSection, ySection, sections);
					
					ImageView imgView = imageViews.get(index);
					
					double translatedX = imgView.getTranslateX() + x;
					double translatedY = imgView.getTranslateY() + y;
					
					//Is not visible anyore
					if(translatedX < minX || translatedX > maxX || 
							translatedY < minY || translatedY > maxY) {
						imgView.setVisible(false);
					}else {
						//Does this force redrawing if already true? If yes check beforehand
						imgView.setVisible(true);
						
						
						String imageToLoad  = imagesToCache[xSection][ySection].imgPath;
						
						if(scale > 7) {
							
							if(scale > 40 && imagesShownOnScreen == 1) {
								
								if(cachedImagesMaxQuality.containsKey(imageToLoad)) {
									imgView.setImage(cachedImagesMaxQuality.get(imageToLoad));
								}else {
									
									//TODO dangerous?
									Image maxQImage = new Image(imageToLoad);
									cachedImagesMaxQuality.put(imageToLoad, maxQImage);
									imgView.setImage(maxQImage);
								}
							}else {
								//Do we need to dispose the hq images again to save heap?
								//Load high quality image on the fly?
								
								if(cachedImagesHQ.containsKey(imageToLoad)) {
									imgView.setImage(cachedImagesHQ.get(imageToLoad));
								}else {
									
									Image hqImage = new Image(imageToLoad,wPerSection,hPerSection,false,false);
									cachedImagesHQ.put(imageToLoad, hqImage);
									//TODO check if imagevies set image takes performance even if it's 
									//set to the same image
									imgView.setImage(hqImage);
								}
							}
						}else if(scale > 4) {
							imgView.setImage(cachedImagesMQ.get(imageToLoad));
						}else {
							imgView.setImage(cachedImagesLQ.get(imageToLoad));
						}
					}
					
					imgView.setLayoutX(x);
					imgView.setLayoutY(y);
					imgView.setFitHeight(Math.ceil(hPerSection));
					imgView.setFitWidth(Math.ceil(wPerSection));
					
					
					
					//Multiple ideas to increase fps. Alter image view if they are outside of view
					//1. Disable image
					//2. Set image to null
					//3. Set visible
					//4. Set Opacity
					
					
				}
			}
		}
		
		
	}
	
	

}
