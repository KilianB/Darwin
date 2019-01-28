package com.github.kilianB.geneticAlgorithm.charting;

import javafx.beans.property.SimpleObjectProperty;

/**
 * An object property allowing programmers to force all Invalidation handlers
 * being called at a requested time.
 * 
 * <p>
 * This is necessary due to {@link javafx.beans.property.SimpleObjectProperty}
 * only calling it's handlers if a new object is the, but not if the internal
 * state of the object changes.
 * 
 * @author Kilian
 *
 */
public class InvalidationObjectProperty<T> extends SimpleObjectProperty<T> {

	/**
	 * Manually invalidate the property. All invalidation handlers will be notified.
	 * Be aware that value change listeners will <b>NOT</b> be called.
	 */
	public void invalidate() {
		fireValueChangedEvent();
	}

	/**
	 * Set a new value to the simple object property. Analogous to the default
	 * implementation of {@link #set(Object)} invalidation and value change
	 * listeners will be notified if the supplied value holds a different reference
	 * than the current object.
	 * <p>
	 * Additionally, if the value points to the same reference as the current value,
	 * an invalidation event will be forwarded to the invalidation handlers.
	 * 
	 * @param newValue The new value
	 */
	public void setAndInvalidate(T newValue) {
		if (this.get() == newValue) {
			fireValueChangedEvent();
		} else {
			set(newValue);
		}
	}

}
