package com.github.kilianB.geneticAlgorithm.rng;

import java.util.Random;

/**
 * @author Kilian
 *
 */
public interface RngPool{

	void nextBytes(byte[] bytes);

	/**
	 * @return a randomly generated int
	 */
	int nextInt();

	/**
	 * @param n the upper bound exclusively
	 * @return a randomly generated integer between [0 - n)
	 */
	int nextInt(int n);

	/**
	 * @return a randomly distributed long
	 */
	long nextLong();

	/**
	 * @return a boolean with 50% probability of being true
	 */
	boolean nextBoolean();

	/**
	 * @return a randomly distributed flaot [0-1]
	 */
	float nextFloat();

	/**
	 * @return a randomly distributed double [0-1]
	 */
	double nextDouble();

	/**
	 * Return a gaus distributed number with mean 0 and standard deviation of 1 1
	 */
	double nextGaus();

	/**
	 * The returned rng instance isn't guaranteed to be thread save and might even be used in a 
	 * per thread context. {@link java.lang.ThreadLocal}. Therefore, it is not advised to keep a reference
	 * to this object across thread usage.
	 * @return the rng instance used to calculate the numbers
	 */
	Random getUnderlayingRNG();
	
}