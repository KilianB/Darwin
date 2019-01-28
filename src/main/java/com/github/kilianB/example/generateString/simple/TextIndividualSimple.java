package com.github.kilianB.example.generateString.simple;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.SimpleIndividual;

/**
 * @author Kilian
 *
 */
public class TextIndividualSimple extends SimpleIndividual {

	private final char[] target;

	public TextIndividualSimple(char[] target, Object currentGenes) {
		super(currentGenes);
		this.target = target;
	}
	
	@Override
	protected Individual createIndividual(Object newGenes) {
		return new TextIndividualSimple(target, newGenes);
	}

	@Override
	public int getVariableCount() {
		return target.length;
	}

	@Override
	protected double calculateFitness() {
		/*
		 * Return a fitness value. 0 for an optimal solution.
		 */
		int fitness = 0; 
		for(int i = 0; i < getVariableCount(); i++) {
			if(target[i]!=(char)this.getValue(i)) {
				fitness++;
			}
		}
		return fitness;
	}

	@Override
	protected Object mutateValue(int index, Object value, double scaleFactor) {
		//Return a random int between 32 and 126
		return (char)(RNG.nextInt(95)+32);
	}
}
