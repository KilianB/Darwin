package com.github.kilianB.geneticAlgorithm.result;

/**
 * A listener interface used to listen to updates published by the genetic
 * algorithm
 * 
 * @author Kilian
 *
 */
public interface ResultListener {

	/**
	 * The genetic algorithm finished calculating a single generation. The last
	 * generation does not trigger this event.
	 * 
	 * <p>
	 * The method will be executed on the main genetic algorithm thread and block
	 * execution until all event listeners are handled. If long tasks are required
	 * it's up to the user to spawn a new thread. No guarantee is made about the
	 * order the result listeners are invoked.
	 * 
	 * @param r The result object containing information about the state of the
	 *          genetic algorithm
	 * 
	 */
	void intermediateResult(Result r);

	/**
	 * 
	 * This method will be invoked if - A stop criteria was hit - The user manually
	 * requested the ga to halt execution or an error was thrown. The result object
	 * is guaranteed to contain the best individual produced by this ga.
	 * 
	 * <p>
	 * The method will be executed on the main genetic algorithm thread and block
	 * execution until all event listeners are handled. If long tasks are required
	 * it's up to the user to spawn a new thread. No guarantee is made about the
	 * order the result listeners are invoked.
	 * 
	 * <p>
	 * The reason for termination can be checked by querying the result object.
	 * 
	 * @param r The result object containing information about the state of the
	 *          genetic algorithm
	 */
	void finalResult(Result r);
}
