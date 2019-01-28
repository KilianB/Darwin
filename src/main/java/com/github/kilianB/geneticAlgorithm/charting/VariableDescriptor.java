package com.github.kilianB.geneticAlgorithm.charting;

import com.github.kilianB.geneticAlgorithm.Individual;

/**
 * An interface providing further information of the variables returned by the
 * {@link com.github.kilianB.geneticAlgorithm.Individual#getValue(int)} method.
 * 
 * @author Kilian
 *
 */
public interface VariableDescriptor {

	/**
	 * Provide a string description for the variables returned by the getValue
	 * method.
	 * 
	 * <p>
	 * The array should have the same length as
	 * {@link Individual#getVariableCount()} and have a title to explain what the
	 * returned variable describes.
	 * 
	 * @return a string array with the variable names
	 */
	String[] getVariableDescription();

	/**
	 * A boolean array indicating if the object returned by getValue should be
	 * grouped by the class instead of equality for charting purposes.
	 * 
	 * <p>
	 * * The array should have the same length as
	 * {@link Individual#getVariableCount()}.
	 * 
	 * <p>
	 * The variable inspection pane groups similar values into buckets. For numeric
	 * values the bucket bounds can be computed, for categorical data the behavior
	 * takes the equality of objects as returned by the
	 * {@link java.lang.Object#equals(Object)} into account.
	 * <p>
	 * Sometimes it can be desirable to group categorical objects based on their
	 * class instead of their equality value.
	 * 
	 * <pre>
	 * 	Given the following construct
	 * 	interface A{}
	 * 	class B implements A{}
	 * 	class C implements A{}
	 * 	
	 * 	The objects by getValue(0) would be grouped into buckets based on if their are an instance of B or C
	 * 
	 * </pre>
	 * 
	 * @return
	 */
	boolean[] getGroupByClasses();
}
