package com.github.kilianB.geneticAlgorithm.prototypes;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import java.util.Arrays;
import java.util.function.Function;

import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy;

public class BooleanPrototype implements IndividualPrototype{


	private int variableCount;
	Function<boolean[], Double> fitnessFunction;
	
	public BooleanPrototype(Function<boolean[], Double> fitnessFunction, int variableCount) {	
		this.fitnessFunction = fitnessFunction;
		this.variableCount = variableCount;
	}
	
	@Override
	public Individual createIndividual() {
		
		boolean[] randomVars = new boolean[variableCount];
		
		for(int i = 0; i < variableCount; i++) {
			randomVars[i] = RNG.nextBoolean();
		}
		
		return new BooleanIndividual(randomVars);
	}
	
	
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
	}



	public class BooleanIndividual extends Individual{
		//Do we use bitsets double[] or BigInteger ?
		//private BitSet values; 
		//b arrays are faster for reasonable sizes. 
	 	private final boolean values[];
		
		public BooleanIndividual(String binaryString) {
			
			values = new boolean[binaryString.length()];
			
			//BigInteger bInt = new BigInteger(binaryString,2);
			for(int i = 0; i < values.length; i++) {
				char c = binaryString.charAt(i);
				if(c == '1') {
					values[i] = true;
				}else if(c == '0') {
					values[i] = false;
				}else {
					throw new IllegalArgumentException("Binary string may only contain 0 and 1. Actual: "
							+ binaryString);
				}
			}
		}
		
		public BooleanIndividual(boolean[] newValues) {
			this.values = newValues;
		}
		
	 	@Override
		public int getVariableCount() {
			return values.length;
		}

		@Override
		public Individual crossover(CrossoverStrategyFuzzy crossoverStrategy, Individual... crossoverParent) {
			//Booleans are discrete values by definition. We can't use fuzzy strategies
			throw new UnsupportedOperationException("Individual does not support fuzzy crossover");
		}

		@Override
		public Individual crossover(CrossoverStrategyDiscrete crossoverStrategy, Individual... crossoverParent) {
			boolean[] newValues = new boolean[values.length];
			int[] crossoverVector = crossoverStrategy.getCrossoverVector(crossoverParent);
			
	 		for(int i = 0; i < crossoverVector.length; i++) {
				int parentIndex = crossoverVector[i];
				newValues[i] = crossoverParent[parentIndex].getValue(i);
			}
			return new BooleanIndividual(newValues);
		}
	 	
		
	 	//TODO why 
	 	
	 	@Override
		public Individual mutate(double probability, double scaleFactor) {
			
			boolean[] newValues = new boolean[values.length];
			
			for(int i = 0; i < values.length; i++) {
				if(RNG.nextDouble() <= probability) {
					newValues[i] = !values[i];
				}else {
					newValues[i] = values[i];
				}
			}			
			return new BooleanIndividual(newValues);
		}
	 	@Override
		protected double calculateFitness() {
			return fitnessFunction.apply(values);
		}
	 	
	 	@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + Arrays.hashCode(values);
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
			BooleanIndividual other = (BooleanIndividual) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (!Arrays.equals(values, other.values))
				return false;
			return true;
		}

		@Override
		public String[] toCSV() {
			String[] s = new String[values.length];
			for(int i = 0; i < values.length; i++) {
				s[i] = Boolean.toString(values[i]);
			}
			return s;
		}
	 	@SuppressWarnings("unchecked")
		@Override
		public <T> T getValue(int index) {
			//Allow the user to access the object without casting.
			return (T) Boolean.valueOf(values[index]);
		}
	 	
	 	

		@Override
		public String toString() {
			return "BooleanIndividual [values=" + Arrays.toString(values) + ", fitness=" + calculateFitness()
					+ "]";
		}

		private BooleanPrototype getOuterType() {
			return BooleanPrototype.this;
		}

	
	 }
	
}
