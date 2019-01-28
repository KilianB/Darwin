package com.github.kilianB.geneticAlgorithm.prototypes;

import static com.github.kilianB.geneticAlgorithm.GeneticAlgorithm.RNG;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.function.Function;

import com.github.kilianB.MathUtil;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy;

public class DoublePrototype implements IndividualPrototype {

	private final double initialRange[][];

	/**
	 * The range of the initial range. max - min
	 */
	private final double rangeOfRange[];
	private final double variableConstraints[][];

	private final Function<double[], Double> fitnessFunction;

	public DoublePrototype(double[][] initialRange, Function<double[], Double> fitnessFunction) {
		this(initialRange, null, fitnessFunction);
	}

	/**
	 * 
	 * @param initialRange
	 * 
	 *                        { {min(x1),max(x1)} {min(x2),max(x2)} }
	 * 
	 *                        [ - )
	 * 
	 * @param constraint
	 * @param fitnessFunction
	 */
	public DoublePrototype(double[][] initialRange, double[][] constraint, Function<double[], Double> fitnessFunction) {

		// Check settings

		// If no constraints are supplied assume unbounded
		if (constraint == null) {
			constraint = new double[initialRange.length][2];
			for (int i = 0; i < initialRange.length; i++) {
				constraint[i] = new double[] { -Double.MAX_VALUE, Double.MAX_VALUE };
			}
		}

		if (initialRange.length != constraint.length) {
			throw new IllegalArgumentException("Initial Range and Constraints have to hold (min,max) for each variable."
					+ " Uneven length detected");
		}

		rangeOfRange = new double[initialRange.length];

		for (int i = 0; i < initialRange.length; i++) {
			double[] minMax = initialRange[i];
			double[] constr = constraint[i];

			if (minMax.length != 2 || constr.length != 2) {
				throw new IllegalArgumentException(
						"Each entry for initial range and constraint must contain 1 value for min and max. Unsupportred lenght"
								+ "detected ");
			}

			rangeOfRange[i] = minMax[1] - minMax[0];

			if (rangeOfRange[i] == Double.NEGATIVE_INFINITY || rangeOfRange[i] == Double.POSITIVE_INFINITY) {
				throw new IllegalArgumentException("Initial Range may only cover a range of Double.MAX_VALUE/2");
			}

			if (minMax[0] < constr[0] || minMax[1] > constr[1]) {
				System.out.println(minMax[0] + " " + constr[0]);
				System.out.println((minMax[0] < constr[0]) + " " + (minMax[1] > constr[1]));

				throw new IllegalArgumentException("The inital range may not be outside of the constraint range");
			}
		}

		// Save Fields
		this.initialRange = initialRange;
		this.variableConstraints = constraint;
		this.fitnessFunction = fitnessFunction;
	}

	@Override
	public Individual createIndividual() {

		double[] values = new double[initialRange.length];

		// Create values
		for (int i = 0; i < initialRange.length; i++) {
			double min = initialRange[i][0];
			double max = initialRange[i][1];
			// Already checked that inital range is not > than Integer.MAXVALUE
			double range = max - min;
			values[i] = range * RNG.nextDouble() + min;
		}
		return new DoubleIndividual(values);
	}

	public class DoubleIndividual extends Individual {

		private final double[] variables;

		public DoubleIndividual(double[] variables) {
			this.variables = variables;
		}

		@Override
		public Individual crossover(CrossoverStrategyFuzzy crossoverStrategy, Individual... crossoverParent) {
			// We assume that both individuals are feasible and valid. No reason to check
			// constraints
			double[] newValues = new double[variables.length];
			double[][] crossoverMatrix = crossoverStrategy.getCrossoverMatrix(crossoverParent);

			for (int i = 0; i < variables.length; i++) {
				double tempValue = 0;

				for (int j = 0; j < crossoverParent.length; j++) {
					tempValue += crossoverMatrix[j][i] * (double) crossoverParent[j].getValue(i);
				}
				newValues[i] = tempValue;
			}
			return new DoubleIndividual(newValues);
		}

		@Override
		public Individual crossover(CrossoverStrategyDiscrete crossoverStrategy, Individual... crossoverParent) {
			double[] newValues = new double[variables.length];
			int[] crossoverVector = crossoverStrategy.getCrossoverVector(crossoverParent);
			for (int i = 0; i < crossoverVector.length; i++) {
				int parentIndex = crossoverVector[i];
				newValues[i] = crossoverParent[parentIndex].getValue(i);
			}
			return new DoubleIndividual(newValues);
		}

		@Override
		public int getVariableCount() {
			return variables.length;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T getValue(int i) {
			return (T) Double.valueOf(variables[i]);
		}

		@Override
		protected double calculateFitness() {
			return fitnessFunction.apply(variables).doubleValue();
		}

		@Override
		public DoubleIndividual mutate(double probability, double scaleFactor) {
			double[] newValues = new double[variables.length];
			double gaus = RNG.nextGaus();
			for (int i = 0; i < variables.length; i++) {
				do {
					if (RNG.nextDouble() <= probability) {
						newValues[i] = variables[i]
								+ MathUtil.fitGaussian(RNG.nextGaus(), scaleFactor * rangeOfRange[i], 0);
					} else {
						newValues[i] = variables[i];
					}

					//// TODO can we adjust the slope of gaussian one sided so we don't have to
					//// itterate and do a trial and error? ..
				} while (newValues[i] < variableConstraints[i][0] || newValues[i] > variableConstraints[i][1]);
			}

			return new DoubleIndividual(newValues);
		}

		@Override
		public String toString() {
			// Debug
			// return "DoubleIndividual@"+System.identityHashCode(this);
			//ArrayUtil.toString(variables,24)
			return "DoubleIndividual [variables=" + arrayString() + ", fitness=" + getFitness() + "]";
		}
		
		DecimalFormat df = new DecimalFormat(" 0.0000000000000000E00 ;-0.0000000000000000E00 ");
		private String arrayString() {
			StringBuilder sb = new StringBuilder();
			for(double d : variables) {
				sb.append(df.format(d));
			}
			
			return sb.toString();
		}

		@Override
		public String[] toCSV() {

			String[] fields = new String[variables.length + 1];

			fields[0] = Double.toString(getFitness());

			for (int i = 0; i < variables.length; i++) {
				fields[i + 1] = Double.toString(variables[i]);
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
			DoubleIndividual other = (DoubleIndividual) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (!Arrays.equals(variables, other.variables))
				return false;
			return true;
		}

		private DoublePrototype getOuterType() {
			return DoublePrototype.this;
		}

//		@Override
//		public Individual eliteCrossover(Individual[] eliteParents) {
//			int variables = eliteParents[0].getVariableCount();
//		
//			double averageVariable[] = new double[variables];
//			double variableSum[] = new double[variables];
//			
//			
//			//1. calculate the average vector
//			for(int i = 0; i < variables; i++) {
//				
//				//double variableSum = 0;
//				
//				//Avoid over or underflow
//				for(Individual individual : eliteParents) {
//					DoubleIndividual indi = (DoubleIndividual) individual;
//					//Assume propotional scaling for this
//					variableSum[i] += (indi.getValue(i) * (1/individual.getFitness())) / eliteParents.length;
//				}
//				
//				for(int j = 0; j < eliteParents.length; j++) {
//					//Assume propotional scaling for this
//					DoubleIndividual ind = (DoubleIndividual) eliteParents[j];
//					averageVariable[i] = (ind.getValue(i)*ind.getFitness()) / variableSum[i];
//				}
//			}
//			
//			int[] parentVector = new int[variables];
//			
//			//Calculate delta for each parent and find the lowest (modus)
//			for(int i = 0; i < variables; i++) {
//				
//				double lowestDelta = Double.MAX_VALUE;
//				int bestParentIndex = -1;
//				
//				for(int j = 0; j < eliteParents.length; j++) {
//					DoubleIndividual ind = (DoubleIndividual) eliteParents[j];
//					double delta =  Math.abs((averageVariable[i] - ind.getValue(i)));
//					if(delta < lowestDelta) {
//						lowestDelta = delta;
//						bestParentIndex = j;
//					}
//				}
//				parentVector[i] = bestParentIndex;
//			}
//			
//			double newValues[]  = new double[variables];
//			
//			
//			for(int i = 0; i < variables; i++) {
//				newValues[i] = ((DoubleIndividual)eliteParents[parentVector[i]]).getValue(i);
//			}
//			
//			//Construct new individual
//			DoubleIndividual individual = new DoubleIndividual(newValues);
//			System.out.println(individual);
//			return individual;
//		}

	}

}
