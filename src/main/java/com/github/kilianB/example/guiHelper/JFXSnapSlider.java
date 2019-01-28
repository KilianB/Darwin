package com.github.kilianB.example.guiHelper;

import java.util.ArrayList;
import java.util.Collections;

import com.github.kilianB.MathUtil;
import com.jfoenix.controls.JFXSlider;
import com.jfoenix.skins.JFXSliderSkin;

import javafx.util.StringConverter;

/**
 * A slider which allows to set the snap values manually
 * 
 * @author Kilian
 *
 */
public class JFXSnapSlider extends JFXSlider {

	ArrayList<Double> validValues = new ArrayList<>();

	public JFXSnapSlider(double... tickValues) {

		for (double d : tickValues) {
			validValues.add(d);
		}
		
		this.setSnapToTicks(true);
		//this.setMajorTickUnit(1);
		
		this.showTickLabelsProperty().set(true);

		sort();

		this.setLabelFormatter(new StringConverter<Double>() {

			@Override
			public String toString(Double object) {
				
				if(validValues.contains(object.doubleValue())) {
					return Integer.toString(object.intValue());
				}
				return "";
			}

			@Override
			public Double fromString(String string) {
				return Double.parseDouble(string);
			}
			
		});
		
	}
	
	public void addTickmark(int newTickMark) {
		validValues.add(Double.valueOf(newTickMark));
		sort();
	}

	private double findValue(double key) {
		int index = Collections.binarySearch(validValues, key);
		
		if(index > 0) {
			//Value found in collection
			return validValues.get(index);
		}else {
			
			
			
			//Find the closest value
			index = -(index+1);
	
			
			if(index <= 1) {
				return validValues.get(0);
			}
			if(index >= validValues.size()) {
				return validValues.get( validValues.size()-1);
			}
			
			//get smaller value
			
			double smaller = validValues.get(index-1);
			//get bigger value
			double bigger = validValues.get(index);
			
			if(Math.abs(key-smaller) < Math.abs(key-bigger)) {
				return smaller;
			}else {
				return bigger;
			}	
		}
	}
	
	private void sort() {
		validValues.sort((o1, o2) -> {
			return o1.compareTo(o2);
		});
	}
	
	public void adjustValue(double newValue) {
		//Find the closest allowed value
		this.setValue(MathUtil.clampNumber(findValue(newValue),this.getMin(), this.getMax()));
	}
	
	class SnapSliderSkin extends JFXSliderSkin{

		public SnapSliderSkin(JFXSlider slider) {
			super(slider);
		
			//This has to be modified to get the correct slider
//			getSkinnable().lookup(selector)
//			NumberAxis numberAxis = new NumberAxis();
//			numberAxis.setAutoRanging(false);
			
			
		}
		
		
		
	}

}
