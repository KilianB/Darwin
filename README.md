<img align="left" width=80 height=80  src="https://user-images.githubusercontent.com/9025925/46655150-0c3d7300-cbab-11e8-8a8a-ec0c2cdc4c68.jpg" />


# Darwin

Darwin extends upon the steps outlined in <a href="https://se.mathworks.com/help/gads/genetic-algorithm.html">Mathworks'</a> genetic algorithm, deviating slightly from most common implementation found elsewhere. The reproduction task is subdivided into elite, crossover and mutation children as well as a clone prevention phase. Resulting offspring are not generated in pairs and the algorithm allows full customization and supports n-parental crossover operations.

>The genetic algorithm is a method for solving both constrained and unconstrained optimization problems that is based on natural selection, the process that drives biological evolution. The genetic algorithm repeatedly modifies a population of individual solutions. At each step, the genetic algorithm selects individuals at random from the current population to be parents and uses them to produce the children for the next generation. Over successive generations, the population "evolves" toward an optimal solution. You can apply the genetic algorithm to solve a variety of optimization problems that are not well suited for standard optimization algorithms, including problems in which the objective function is discontinuous, nondifferentiable, stochastic, or highly nonlinear [1]

## Features

__For examples and indepth tutorial please refer to the wiki section.__

- N-parental recombination 
- Support for numerical and categorical data
- Multi threading via sub population and migration
- Visual aid via native charting package
- Full customization for all parts of the 
- Export of generated individuals for further analysis

### Full control 

Please take a look at the wiki for a step by step tutorial. Below you can find optional paramesters to tweak the algorithm in a suited way.

````Java
GeneticAlgorithm.builder().withPrototype(proto) /* withInitialPopulation(Individual[] initialPopulation) */
	.withMaxGenerationCount(/* 200 * number of variables of the fitness function */)
	.withMaxExecutionTime(Long.MAX_VALUE,TimeUnit.MILLISECONDS)
	.withMaxStaleGenerations(-1)
	.withTargetFitness(1e-3)
	//Default settings
	.population()
	.withPopulationCount(20)
	.withEliteFraction(0.05f)
	.withCrossoverFraction(0.8f)
	.advanced()
	.withScalingStrategy(new RankScaling())
	.withSelectionStrategy(new StochasticUniform())
	.withCrossoverStrategy(new ScatteredDiscrete(2))
	.withForceCloneMutation(true,10)
	.withMutationProbability(0.1)
	.withMutationScalingStrategy(MutationScalingStrategy.RICHARD)
	.migration()
	.withMigrationInterval(Integer.MAX_VALUE)
	.withMigrationProcess(new NetworkMigration())
	.withMigrationStrategy(new Elitism(2))
	.withNewSubpopulation(/* subpopulations */)
	//Settings for each individual subpopulation
	.build();
````

## Maven

Soon to be hosted on bintray. For now use jitpack
````XML
<repositories>
   <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
   </repository>
</repositories>

<dependency>
   <groupId>com.github.KilianB</groupId>
   <artifactId>Darwin</artifactId>
   <version>-SNAPSHOT</version>
</dependency>
````

##

![rastriginoverview](https://user-images.githubusercontent.com/9025925/50298536-a9583a00-047f-11e9-9336-ef381a673804.jpg)

<img src="https://user-images.githubusercontent.com/9025925/50223345-39708380-039b-11e9-8373-571230397934.jpg" />


## Useful resources: 
<ul>
<li><a href="https://se.mathworks.com/help/gads/what-is-the-genetic-algorithm.html">[1] What is the genetic algorithm</a></li>
<li><a href="https://se.mathworks.com/help/gads/how-the-genetic-algorithm-works.html">How GA's work?</a></li>
<li><a href="https://se.mathworks.com/help/gads/some-genetic-algorithm-terminology.html">Terminology</a></li>
</ul>
