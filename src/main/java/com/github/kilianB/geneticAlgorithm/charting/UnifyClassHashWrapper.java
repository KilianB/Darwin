package com.github.kilianB.geneticAlgorithm.charting;

/**
 * Convenience wrapper to unify the hashcode of supplied objects based on their
 * class rather than their individual implementation of hashcode. This allows 
 * to pool similar objects based on their classes in hash collections.
 * 
 * @author Kilian
 *
 */
class UnifyClassHashWrapper {

	private Object objectToWrap;

	public UnifyClassHashWrapper(Object objectToWrap) {
		this.objectToWrap = objectToWrap;
	}

	@Override
	public int hashCode() {
		return objectToWrap.getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}

	// This will pick one of the equal objects as to String.
	public String toString() {
		return objectToWrap.getClass().getSimpleName();
	}
}