package com.github.kilianB.geneticAlgorithm.mutationScaling;

import com.github.kilianB.pcg.fast.PcgRSUFast;

/**
 * @author Kilian
 *
 */
@FunctionalInterface
public interface MutationScalingStrategy {
	/**
	 * Compute the scale factor which will be passed down to the mutation method of
	 * an individual.
	 * 
	 * <p>
	 * The scale factor hints how severe a change in mutation may impact the
	 * individual. For example given a numerical domains, the algorithm usually want
	 * to explore a broader region at the beginning.
	 * <p>
	 * Mutating a value from 2 - 10 may be acceptable. As time progresses less
	 * drastic changes are more appropriate to concentrate on the area found.
	 * <p>
	 * The scale factor usually in the range of [0-1] tells the individual how much
	 * of a change it shall perform. The method takes multiple values which can be
	 * used to compute a scale factor, but are by no means required to be taken into
	 * account
	 * 
	 * @param currentGeneration      The current generation
	 * @param maxGeneration          The maximum number of generations the algo will
	 *                               stop computing due to the max generation
	 *                               constraint
	 * @param currentlyBestFitness   The currently best fitness
	 * @param targetFitness          The fitness the algorithm tries to achieve
	 * @param currentStallGeneration The number of generations the algorithms result
	 *                               did not improve
	 * @return the scale factor
	 */
	double computeScaleFactor(int currentGeneration, int maxGeneration, double currentlyBestFitness,
			double targetFitness, int currentStallGeneration);

	/**
	 * Resets the state of the strategy to be used by a new genetic algorithm.Gets
	 * invoked by
	 * {@link com.github.kilianB.geneticAlgorithm.GeneticAlgorithm#reset()}. By
	 * default this method does nothing and should only be overwritten if the
	 * internal state of the strategy is depended on earlier calls to it'c compute
	 * method.
	 */
	default void reset() {
	};

	/**
	 * The constant scaling strategy always returns a scale value of 1 no matter the
	 * given input. Useful if the scaling value is not used e.g in conjunction with
	 * categorical individuals which do not rely on fuzzy mutation and ignore the
	 * scale value.
	 */
	public static final MutationScalingStrategy CONSTANT = (gen, maxGen, curFitness, tFitness, curStall) -> {
		return 1;
	};

	/**
	 * The scale value is depended on the percent of generations that have passed in
	 * relation to the maximum generations given as a stop criteria. The scale value
	 * scales linearly from 1 at 0 generations to 0 at stopGenerations.
	 */
	public static final MutationScalingStrategy LINEAR_GENERATION = (gen, maxGen, curFitness, tFitness, curStall) -> {
		return 1 - (gen / (double) maxGen);
	};
	/**
	 * The richard curve is a a flipped scurve function (take a look at sigmoid)
	 * ranging from [0.993 - 0.006] depending on the % of generation that have
	 * passed in relation to the maximum generations given as a a stop criteria.
	 */
	public static final MutationScalingStrategy RICHARD = (gen, maxGen, curFitness, tFitness, curStall) -> {

		// y(t) = A + (K-A) / (( C + Q * e^ (-Bt) ) ^ (Y * v));

		// Y = width, height, size, etc ...
		// t = time

		// A = lower asymptote
		// K = =upper asymptote
		// B = growth rate
		// V > 0 =
		// Q = ->
		// C = 1

		// Default -5 - 5
		// -(t/2) -10 - 10
		// -(t/4) -20 - 20
		// -(t/5) -25 - 25

		// Default -5 - 5
		// -(t-5) 0 - 10

		// -(t/4)+5 0 - 40
		// -(t/5)+5 0 - 50

		int A = 1;

		double fac = 15; // Math.E*2
		double exponent = fac - gen / (maxGen / (2 * fac));
		return A - A / (1 + Math.exp(exponent));
	};

	/**
	 * The scale value is a uniform randomly distributed between 0 and 1
	 */
	public static final MutationScalingStrategy RANDOM = (gen, maxGen, curFitness, tFitness, curStall) -> {
		return PcgRSUFast.nextDouble(true, true);
	};

	/**
	 * The scale value returned is described by the ellipse function f(x) = b/a *
	 * sqrt(a^2 - x^2) using only the first quadrant of the cartesian coordinate
	 * system. The ellipse is drawn to have it's y intercept at 1 and it's x
	 * intercept at max generation.
	 */
	public static final MutationScalingStrategy ELLIPSE = (gen, maxGen, curFitness, tFitness, curStall) -> {

		// f(x) = b/a * sqrt(a^2 - x^2)
		// b = upper bound
		// a = right bound
		return (1 / (double) maxGen) * Math.sqrt(maxGen * maxGen - gen * gen);
	};

	// TODO to implement
	@Deprecated
	public static final MutationScalingStrategy STALL_ADAPT = (gen, maxGen, curFitness, tFitness, curStall) -> {
		// TODO do some in depth checking
		double scale = LINEAR_GENERATION.computeScaleFactor(gen, maxGen, curFitness, tFitness, curStall);
		if (curStall > maxGen / 20) {
			scale *= 1.3;
		}
		return scale;
	};

	@Deprecated
	public static final MutationScalingStrategy ASYMPTOT = (gen, maxGen, curFitness, tFitness, curStall) -> {
		return (maxGen / ((double) gen + 1) - 1) / (double) maxGen;
	};
}
