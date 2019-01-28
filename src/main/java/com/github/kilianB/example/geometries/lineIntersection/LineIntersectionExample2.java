package com.github.kilianB.example.geometries.lineIntersection;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import com.github.kilianB.geneticAlgorithm.GeneticAlgorithm;
import com.github.kilianB.geneticAlgorithm.charting.ChartHelper;
import com.github.kilianB.pcg.fast.PcgRSFast;

/**
 * @author Kilian
 *
 */
public class LineIntersectionExample2 {

	public static void main(String[] args) {
		int populationSize = 15;
		int linesOnCanvas = 25;

		LineIntersectionIndividual[] initialPopulation = new LineIntersectionIndividual[populationSize];

		GeometryFactory factory = new GeometryFactory();

		var rng = new PcgRSFast();

		
		//Sets a bounded problem
		GeometryFactory fact = new GeometryFactory();
		
		Coordinate[] domainBound = new Coordinate[] {
				new Coordinate(0,0),
				new Coordinate(0,1),
				new Coordinate(1,1),
				new Coordinate(1,0),
				new Coordinate(0,0)
		};
		

		LineIntersectionIndividual.setDomainBounds(fact.createPolygon(domainBound));
		
		//TODO we need to check that the individuals created are valid as well ...
		
		// initial domain 0 - 1
		for (int i = 0; i < populationSize; i++) {

			List<LineString> lines = new ArrayList<>();
			// Create a JTS Line
			for (int j = 0; j < linesOnCanvas; j++) {
				lines.add(factory
						.createLineString(new Coordinate[] { new Coordinate(rng.nextDouble(), rng.nextDouble()),
								new Coordinate(rng.nextDouble(), rng.nextDouble()) }));
			}
			initialPopulation[i] = new LineIntersectionIndividual(lines);
		} 
		
		var ga = GeneticAlgorithm.builder().withInitialPopulation(initialPopulation)
				.withMaxGenerationCount(50000).population()
				.advanced()
				.withForceCloneMutation(false,0)
				.withMutationProbability(0.05)
				//.withCrossoverStrategy(new ScatteredDiscrete(2))
				//.withCrossoverStrategy(new ScatteredFuzzy(2))
				.migration(100)
				.withNewSubpopulations(4)
				//.withScalingStrategy(new AgeScaling())
				.build();
		
		ChartHelper.displayProgressPane("Line Intersection",ga,false,true);
		
		ga.calculate(10);
	}
	
}
