package com.github.kilianB.geneticAlgorithm.prototypes;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import java.util.Arrays;
import java.util.function.Function;

import com.github.kilianB.MathUtil;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy;

public class IntPrototype implements IndividualPrototype{

	private final int initialRange[][];
	
	/**
	 * The range of the initial range.
	 * max - min
	 */
	private final int rangeOfRange[];
	private final int variableConstraints[][];
	
	private final Function<int[],Double> fitnessFunction;

	public IntPrototype(int[][] initialRange, Function<int[],Double> fitnessFunction) {
		this(initialRange,null,fitnessFunction);
	}	
	/**
	 * 
	 * @param initialRange
	 * 
	 * {
	 * 	{min(x1),max(x1)}
	 * 	{min(x2),max(x2)}
	 * }
	 * 
	 * [ - )
	 * 
	 * @param constraint
	 * @param fitnessFunction
	 */
	public IntPrototype(int[][] initialRange, int[][] constraint, Function<int[],Double> fitnessFunction) {
		
		//Check settings
		
		//If no constraints are supplied assume unbounded
		if(constraint == null) {
			constraint = new int[initialRange.length][2];
			for(int i = 0; i < initialRange.length; i++) {
				constraint[i] = new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE};
			}
		}
		
		
		if(initialRange.length != constraint.length) {
			throw new IllegalArgumentException("Initial Range and Constraints have to hold (min,max) for each variable."
					+ " Uneven length detected");
		}
		
		rangeOfRange = new int[initialRange.length];
		
		for(int i = 0; i < initialRange.length; i++) {
			int[] minMax = initialRange[i];
			int[] constr = constraint[i];
			
			if(minMax.length != 2 || constr.length != 2) {
				throw new IllegalArgumentException("Each entry for initial range and constraint must contain 1 value for min and max. Unsupportred lenght"
						+ "detected ");
			}
			
			try {
				rangeOfRange[i] = Math.subtractExact(minMax[1], minMax[0]);
			}catch(ArithmeticException exception) {
				throw new IllegalArgumentException("Initial Range may only cover a range of Integer.MAX_VALUE/2");
			}
			
			if(minMax[0] < constr[0] || minMax[1] > constr[1]) {
				throw new IllegalArgumentException("The inital range may not be outside of the constraint range");
			}
		}
		
		//Save Fields
		this.initialRange = initialRange;
		this.variableConstraints = constraint;
		this.fitnessFunction = fitnessFunction;
	}
	
	
	@Override
	public Individual createIndividual() {
		
		int[] values = new int[initialRange.length];
		
		//Create values
		for(int i = 0; i < initialRange.length; i++) {
			int min = initialRange[i][0];
			int max = initialRange[i][1];	
			//Already checked that inital range is not > than Integer.MAXVALUE
			int range = max - min;
			values[i] = RNG.nextInt(range) + min;
		}
		return new IntIndividual(values);
	}
	
	
	public class IntIndividual extends Individual{
		
		private final int[] variables;

		public IntIndividual(int[] variables) {
			this.variables = variables;		
		}
		
		@Override
		public int getVariableCount() {
			return variables.length;
		}
		

		@Override
		public Individual crossover(CrossoverStrategyFuzzy crossoverStrategy, Individual... crossoverParent) {
			// We assume that both individuals are feasible and valid. No reason to check
			// constraints
			int[] newValues = new int[variables.length];
			double[][] crossoverMatrix = crossoverStrategy.getCrossoverMatrix(crossoverParent);
			
			for (int i = 0; i < variables.length; i++) {
				double tempValue = 0;
				
				for(int j = 0; j < crossoverParent.length; j++) {
					tempValue += crossoverMatrix[j][i] * (int)(crossoverParent[j]).getValue(i);
				}
				newValues[i] = (int) Math.round(tempValue);
			}
			return new IntIndividual(newValues);
		}

		@Override
		public Individual crossover(CrossoverStrategyDiscrete crossoverStrategy, Individual... crossoverParent) {
			// We assume that both individuals are feasible and valid. No reason to check
			// constraints
			int[] newValues = new int[variables.length];
			
			int[] crossoverVector = crossoverStrategy.getCrossoverVector(crossoverParent);
			for(int i = 0; i < crossoverVector.length; i++) {
				int parentIndex = crossoverVector[i];
				newValues[i] = crossoverParent[parentIndex].getValue(i);
			}
			return new IntIndividual(newValues);
		}
		

		@SuppressWarnings("unchecked")
		public <T> T getValue(int i) {
			return (T) Integer.valueOf(variables[i]);
		}

		@Override
		protected double calculateFitness() {
			return fitnessFunction.apply(variables).doubleValue();
		}

		@Override
		public IntIndividual mutate(double probability, double scaleFactor) {
			int[] newValues = new int[variables.length];
			
			for(int i = 0; i < variables.length; i++) {
				//TODO care about integer over/underflow?
				do {
					if(RNG.nextDouble() <= probability) {
						newValues[i] = variables[i] + (int)Math.round(MathUtil.fitGaussian(RNG.nextGaus(), scaleFactor* rangeOfRange[i],0));
					}else {
						newValues[i] = variables[i];
					}
					////TODO can we adjust the slope of gaussian one sided so we don't have to itterate and do a trial and error? ..
				}while(newValues[i] < variableConstraints[i][0] || newValues[i] > variableConstraints[i][1]);
			}
			
			return new IntIndividual(newValues);
		}

		@Override
		public String toString() {
			return "IntIndividual [variables=" + Arrays.toString(variables) + ", fitness=" + getFitness() + "]";
		}
		
		@Override
		public String[] toCSV() {
			
			String[] fields = new String[variables.length + 1];
			
			fields[0] = Double.toString(getFitness());
			
			for(int i = 0; i < variables.length;i++) {
				fields[i+1] = Integer.toString(variables[i]);
			}
			return fields;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + Arrays.hashCode(variables);
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
			IntIndividual other = (IntIndividual) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (!Arrays.equals(variables, other.variables))
				return false;
			return true;
		}

		private IntPrototype getOuterType() {
			return IntPrototype.this;
		}


	}
}
