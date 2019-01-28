package com.github.kilianB.example.geometries.lineIntersection;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.distance.DistanceOp;

import com.github.kilianB.MathUtil;
import com.github.kilianB.geneticAlgorithm.Individual;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyDiscrete;
import com.github.kilianB.geneticAlgorithm.crossover.CrossoverStrategyFuzzy;

public class LineIntersectionIndividual extends Individual {

	private static Geometry DOMAIN;
	private static double DIAGONAL_LENGTH;

	List<LineString> lines;

	public LineIntersectionIndividual(List<LineString> lines) {
		this.lines = lines;
	}

	@Override
	public int getVariableCount() {
		return lines.size();
	}

	@Override
	public Individual mutate(double probability, double scaleFactor) {

		GeometryFactory fact = new GeometryFactory();

		// Lets just swap lines and not x and y vectors
		List<LineString> newValue = new ArrayList<>(lines.size());

		for (int i = 0; i < lines.size(); i++) {
			if (RNG.nextDouble() < probability) {

				// Lets decide if we want to change the line or remove it and add a new one
				if (RNG.nextBoolean()) {
					Point start = lines.get(i).getStartPoint();
					Point end = lines.get(i).getEndPoint();

					// We could also write a polygon fitler to modify cordinates

					// Decrease the amount of mutation over the period
					double startX = start.getX() + MathUtil.fitGaussian(RNG.nextGaus(), scaleFactor / 10, 0);
					double startY = start.getY() + MathUtil.fitGaussian(RNG.nextGaus(), scaleFactor / 10, 0);

					double endX = end.getX() + MathUtil.fitGaussian(RNG.nextGaus(), scaleFactor / 10, 0);
					double endY = end.getY() + MathUtil.fitGaussian(RNG.nextGaus(), scaleFactor / 10, 0);

					// Check if the points are within the domain

					Coordinate startCoordinate = new Coordinate(startX, startY);
					Coordinate endCoordinate = new Coordinate(endX, endY);

					Point startPoint = fact.createPoint(startCoordinate);
					Point endPoint = fact.createPoint(endCoordinate);

					// We are outside of our domain. Move the line start to the closests point
					// inside
					// the domain
					if (!DOMAIN.contains(startPoint)) {
						DistanceOp distOp = new DistanceOp(DOMAIN, startPoint);
						startCoordinate = distOp.nearestPoints()[0];
					}
					if (!DOMAIN.contains(endPoint)) {
						DistanceOp distOp = new DistanceOp(DOMAIN, endPoint);
						endCoordinate = distOp.nearestPoints()[0];
					}

					LineString mutatedLine = fact.createLineString(new Coordinate[] { startCoordinate, endCoordinate });

					newValue.add(mutatedLine);
				} else {

					Coordinate startCoordinate = new Coordinate(RNG.nextDouble(), RNG.nextDouble());
					Coordinate endCoordinate = new Coordinate(RNG.nextDouble(), RNG.nextDouble());

					Point startPoint = fact.createPoint(startCoordinate);
					Point endPoint = fact.createPoint(endCoordinate);

					// We are outside of our domain. Move the line start to the closests point
					// inside
					// the domain
					if (!DOMAIN.contains(startPoint)) {
						DistanceOp distOp = new DistanceOp(DOMAIN, startPoint);
						startCoordinate = distOp.nearestPoints()[0];
					}
					if (!DOMAIN.contains(endPoint)) {
						DistanceOp distOp = new DistanceOp(DOMAIN, endPoint);
						endCoordinate = distOp.nearestPoints()[0];
					}

					LineString newLine = fact.createLineString(new Coordinate[] { startCoordinate, endCoordinate });

					newValue.add(newLine);

				}

			} else {
				// Since we don't mutate the value directly we don't need to create a deep clone
				newValue.add(lines.get(i));
			}
		}

		return new LineIntersectionIndividual(newValue);
	}

	@Override
	protected double calculateFitness() {

		// Speed up compu

		// Max number of crossovers given n lines
//		double fitness = (Math.pow(lines.size(),2)-lines.size()) ;
//		
//		for(var line : lines) {
//			for(var line1 : lines) {
//				if(line != line1 && line.intersects(line1)) {
//					fitness--;
//				}
//			}
//		}
		// fitness /= 2;

		// while maximizing the length of the lines?

		// Max number of crossovers given n lines
		double fitness = (Math.pow(lines.size(), 2) - lines.size()) * DIAGONAL_LENGTH;

		for (var line : lines) {
			int crossoverCount = 0;
			for (var line1 : lines) {
				if (line != line1 && line.intersects(line1)) {
					crossoverCount++;
				}
			}
			fitness -= line.getLength() * crossoverCount;
		}

//
//		//This will always be higher > 0
//		double fitness = DIAGONAL_LENGTH*lines.size();
//		
//		outer:
//		for(var line : lines) {
//			for(var line1 : lines) {
//				if(line != line1 && line.intersects(line1)) {
//					continue outer;
//				}
//			}
//			//It's a valid line.
//			fitness -= line.getLength();
//		}
//		
		return fitness;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(int index) {
		return (T) lines.get(index);
	}

	public List<LineString> getAllLines() {
		// Convenience method to access variables for easier drawing
		return new ArrayList<LineString>(lines);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lines == null) ? 0 : lines.hashCode());
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
		LineIntersectionIndividual other = (LineIntersectionIndividual) obj;
		if (lines == null) {
			if (other.lines != null)
				return false;
		} else if (!lines.equals(other.lines))
			return false;
		return true;
	}

	@Override
	public String[] toCSV() {
		// Not yet TODO
		return null;
	}

	public static void setDomainBounds(Polygon polygon) {
		DOMAIN = polygon;

		// Bounding boxes is a rectangle
		Geometry boundingBox = polygon.getEnvelope();

		assert (boundingBox.isRectangle());

		System.out.println("bounding box: " + boundingBox);

		Coordinate[] c = boundingBox.getCoordinates();
		// Calculate the longest diagonal of the domain
		DIAGONAL_LENGTH = c[0].distance(c[2]);

		System.out.println("Set domain: " + DOMAIN + " " + DIAGONAL_LENGTH);

	}

	@Override
	public Individual crossover(CrossoverStrategyFuzzy crossoverStrategy, Individual... crossoverParent) {

		List<LineString> newValue = new ArrayList<>(lines.size());

		double[][] crossoverMatrix = crossoverStrategy.getCrossoverMatrix(crossoverParent);

		GeometryFactory fact = new GeometryFactory();

		for (int variable = 0; variable < lines.size(); variable++) {

			double newX1 = 0;
			double newY1 = 0;
			double newX2 = 0;
			double newY2 = 0;

			for (int parent = 0; parent < crossoverParent.length; parent++) {

				double scaleFactor = crossoverMatrix[parent][variable];
				LineString line = crossoverParent[parent].getValue(variable);
				newX1 += ((line.getCoordinateN(0).x) * scaleFactor);
				newY1 += ((line.getCoordinateN(0).y) * scaleFactor);
				newX2 += ((line.getCoordinateN(1).x) * scaleFactor);
				newY2 += ((line.getCoordinateN(1).y) * scaleFactor);
			}
			newValue.add(fact
					.createLineString(new Coordinate[] { new Coordinate(newX1, newY1), new Coordinate(newX2, newY2) }));

		}

		return new LineIntersectionIndividual(newValue);

	}

	@Override
	public Individual crossover(CrossoverStrategyDiscrete crossoverStrategy, Individual... crossoverParent) {
		List<LineString> newValue = new ArrayList<>(lines.size());
		// Lets just swap lines and not x and y vectors
		int[] crossoverVector = crossoverStrategy.getCrossoverVector(crossoverParent);

		for (int i = 0; i < lines.size(); i++) {
			newValue.add(crossoverParent[crossoverVector[i]].getValue(i));
		}
		return new LineIntersectionIndividual(newValue);
	}

}
