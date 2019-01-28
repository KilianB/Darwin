package com.github.kilianB.example.generateString;

import java.util.Arrays;

import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy;

public class TextIndividual extends Individual{

	/**
	 * The goal string used to compute how good our solution is.
	 */
	private final char[] target;

	/**
	 * The current solution of this individual
	 */
	private final char[] variable;

	/**
	 * Restrict this individual to use a-zA-Z letters only. This reduces the search
	 * space. If false use the full range of ASCII 32 - 127
	 * 
	 * @see http://www.asciitable.com/
	 */
	private final static boolean LIMIT_TO_LETTERS = false;

	public TextIndividual(String target, char values[]) {
		this(target.toCharArray(), values);
	}

	public TextIndividual(char target[], char values[]) {
		this.target = target;
		this.variable = values;

	}

	public char getVariable(int index) {
		return variable[index];
	}

	@Override
	public int getVariableCount() {
		return target.length;
	}

	@Override
	public Individual crossover(CrossoverStrategyDiscrete crossoverStrategy, Individual... crossoverParent) {

		// Get an array telling us which parent should deliver which gene
		int[] crossoverVector = crossoverStrategy.getCrossoverVector(crossoverParent);

		// The genes of the new offspring
		char[] newValues = new char[variable.length];

		for (var i = 0; i < variable.length; i++) {
			int parentIndex = crossoverVector[i];
			newValues[i] = crossoverParent[parentIndex].getValue(i);
		}

		return new TextIndividual(target, newValues);
	}

	@Override
	public Individual crossover(CrossoverStrategyFuzzy crossoverStrategy, Individual... crossoverParent) {
		throw new UnsupportedOperationException("Does not support fuzzy crossover");
	}

	public String getResult() {
		return new String(variable);
	}

	@Override
	public Individual mutate(double probability, double scaleFactor) {

		// The scale factor indicates how much mutation is desired. The value usually
		// starts
		// at 1 and linearily scales down to 0 once the algorithm hits maxGenerations
		char[] newValues = new char[variable.length];

		for (int i = 0; i < variable.length; i++) {
			if (RNG.nextDouble() <= probability * scaleFactor) {
				newValues[i] = generateRandomChar();
			} else {
				newValues[i] = variable[i];
			}
		}
		return new TextIndividual(target, newValues);
	}

	@Override
	protected double calculateFitness() {

		/*
		 * A very basic fitness function. We could also use a bit more complex edit
		 * distance function like levenshtein distance if our crossover or mutation
		 * allows for shifting of inputs.
		 */
		int distance = 0;
		for (int i = 0; i < target.length; i++) {
			if (variable[i] != target[i]) {
				distance++;
			}
		}
		return distance;
	}

	@Override
	public String toString() {
		return "TextIndividual [variable=" + new String(variable) + ", fitness=" + calculateFitness() + ", birth="
				+ getBirth() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(target);
		result = prime * result + Arrays.hashCode(variable);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextIndividual other = (TextIndividual) obj;
		if (!Arrays.equals(target, other.target))
			return false;
		if (!Arrays.equals(variable, other.variable))
			return false;
		return true;
	}

	/**
	 * Optional method allowing to output the individual to a csv file for further
	 * investigation.
	 */
	@Override
	public String[] toCSV() {
		return new String[] { new String(variable) };
	}

	/**
	 * Randomly generate a char
	 * @return a random char 
	 */
	public static char generateRandomChar() {
		var RNG = GeneticAlgorithm.RNG;
		if (LIMIT_TO_LETTERS) {
			int randomInt = RNG.nextInt(25);
			if (RNG.nextBoolean()) {
				// capital letter ASCI 65 - 90
				return (char) (randomInt + 65);
			} else {
				// lower case ASCI 97 - 122
				return (char) (randomInt + 97);
			}
		} else {
			int randomInt = RNG.nextInt(95) + 32;
			return (char) (randomInt);
		}
	}

	/**
	 * Create an individual with the given target. We could also implement the individual prorotype
	 * interface here
	 * @param target the target char sequence of the individual
	 * @return a randomly initialized TextIndividual
	 */
	public static Individual createRandomIndividual(String target) {
		int vars = target.length();
		char[] values = new char[vars];

		for (int i = 0; i < vars; i++) {
			values[i] = generateRandomChar();
		}
		return new TextIndividual(target, values);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(int index) {
		return (T) Character.valueOf(variable[index]);
	}

}
