import jmetal.operators.crossover.Crossover;
import jmetal.operators.crossover.CrossoverFactory;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.MutationFactory;
import jmetal.operators.selection.Selection;
import jmetal.operators.selection.SelectionFactory;
import jmetal.metaheuristics.nsgaII.NSGAII;
import jmetal.util.JMException;
import jmetal.core.Algorithm;
import java.util.Random;
import java.util.HashMap;

/**
 * An example of benchmarking random search on a COCO suite. 
 *
 * Set the parameter budget to suit your needs.
 */
public class Experiment {

	public static final int POPULATION_SIZE = 100;

	/**
	 * The maximal budget for evaluations done by an optimization algorithm equals 
	 * dimension * budget.
	 * Increase the budget multiplier value gradually to see how it affects the runtime.
	 */
	public static int budget = 100;
	
	/**
	 * The maximal number of independent restarts allowed for an algorithm that restarts itself. 
	 */
	public static final int INDEPENDENT_RESTARTS = 10000;
	
	/**
	 * The problem to be optimized (needed in order to simplify the interface between the optimization
	 * algorithm and the COCO platform).
	 */
	public static Problem PROBLEM;

	/**
	 * The main method initializes the random number generator and calls the example experiment on the
	 * bi-objective suite.
	 */
	public static void main(String[] args) {
		
		String algorithm = "";
		HashMap<String, String> arguments = new HashMap<String, String>();

		if (args.length%2==0) {
			for (int i=0; i<args.length; i+=2) {
				arguments.put(args[i], args[i+1]);
			}
		} else {
			System.out.println("Usage:");
			System.out.println("\t-alg <value>");
			System.out.println("\t-budget <value>");
			System.exit(1);
		}

		algorithm = arguments.get("-alg");
		if (algorithm == null) {
			System.out.println("WARNING: Algorithm is NULL. Use -alg <value>");
			System.exit(1);
		}

		String sbudget = arguments.get("-budget");
		if (sbudget != null) {
			try {
               budget = Integer.parseInt(sbudget);
               System.out.println("budget="+budget);
            } catch (NumberFormatException e) {
                System.err.println("Could not parse parameter \"budget\", \"" + sbudget + "\" is not an integer!");
                System.exit(1);
            }
		} else {
			System.out.println("WARNING: Budget is NULL. (use -budget <value>)");
			System.exit(1);
		}

		/* Change the log level to "warning" to get less output */
		CocoJNI.cocoSetLogLevel("info");

		// System.out.println("Running the example experiment... (might take time, be patient)");
		// System.out.flush();
		
		/* Call the example experiment */
		experiment("bbob-biobj", "bbob-biobj", algorithm);

		/* Uncomment the line below to run the same example experiment on the bbob suite
	  	Experiment("bbob", "bbob", randomGenerator); */

		// System.out.println("Done!");
		// System.out.flush();

		return;
	}
	
	/**
	 * A simple example of benchmarking random search on a suite with instances from 2016.
	 *
	 * @param suiteName Name of the suite (use "bbob" for the single-objective and "bbob-biobj" for the
	 * bi-objective suite).
	 * @param observerName Name of the observer (use "bbob" for the single-objective and "bbob-biobj" for the
	 * bi-objective observer).
	 */
	public static void experiment(String suiteName, String observerName, String algorithm) {
		try {

			final String algorithmName = algorithm;

			/* Set some options for the observer. See documentation for other options. */
			final String observerOptions = 
					  "result_folder: " + algorithmName + "_on_" + suiteName + " " 
					+ "algorithm_name: " + algorithmName + " "
					+ "algorithm_info: \"A jMetal format algorithm\"";
					
			/* Initialize the suite and observer */
			Suite suite = new Suite(suiteName, "year: 2016", "dimensions: 2,3,5,10,20,40");
			Observer observer = new Observer(observerName, observerOptions);
			Benchmark benchmark = new Benchmark(suite, observer);

			/* Initialize timing */
			Timing timing = new Timing();

			/* Iterate over all problems in the suite */
			while ((PROBLEM = benchmark.getNextProblem()) != null) {

				int dimension = PROBLEM.getDimension();

				/* Run the algorithm at least once */
				for (int run = 1; run <= 1 + INDEPENDENT_RESTARTS; run++) {

					long evaluationsDone = PROBLEM.getEvaluations();
					long evaluationsRemaining = (long) (dimension * budget) - evaluationsDone;

					/* Break the loop if the target was hit or there are no more remaining evaluations */
					if (PROBLEM.isFinalTargetHit() || (evaluationsRemaining <= 0))
						break;

					/* Call the optimization algorithm for the remaining number of evaluations */
					runAlgorithm(algorithmName, PROBLEM,
							       dimension,
							       PROBLEM.getNumberOfObjectives(),
							       PROBLEM.getSmallestValuesOfInterest(),
							       PROBLEM.getLargestValuesOfInterest(),
							       evaluationsRemaining);

					/* Break the loop if the algorithm performed no evaluations or an unexpected thing happened */
					if (PROBLEM.getEvaluations() == evaluationsDone) {
						System.out.println("WARNING: Budget has not been exhausted (" + evaluationsDone + "/"
								+ dimension * budget + " evaluations done)!\n");
						break;
					} else if (PROBLEM.getEvaluations() < evaluationsDone)
						System.out.println("ERROR: Something unexpected happened - function evaluations were decreased!");
				}

				/* Keep track of time */
				timing.timeProblem(PROBLEM);
			}

			/* Output the timing data */
			timing.output();

			benchmark.finalizeBenchmark();

		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/** 
	 * A simple random search algorithm that can be used for single- as well as multi-objective 
	 * optimization.
	 */
	public static void runAlgorithm(String algorithmName, Problem PROBLEM, 
			                          int dimension, 
			                          int numberOfObjectives, 
			                          double[] lowerBounds,
			                          double[] upperBounds, 
			                          long maxBudget) {

		jmetal.core.Problem problem = new CocoProblem (PROBLEM, dimension, numberOfObjectives, 
                    lowerBounds, upperBounds) ;

		Algorithm algorithm = null; 
		int iterations = ((int) maxBudget/POPULATION_SIZE);

		try {
		
			if (algorithmName.equals("NSGAII")) {
				algorithm = new NSGAII(problem) ;

				int populationSize_              = 100   ;
			    int maxEvaluations_              = 25000 ;
			    double mutationProbability_         = 1.0/problem.getNumberOfVariables() ;
			    double crossoverProbability_        = 0.9   ;
			    double mutationDistributionIndex_   = 20.0  ;
			    double crossoverDistributionIndex_  = 20.0  ;
			    // Algorithm parameters
			    algorithm.setInputParameter("populationSize",populationSize_);
			    algorithm.setInputParameter("maxEvaluations",maxEvaluations_);

			    // Mutation and Crossover for Real codification
			    HashMap parameters = new HashMap() ;
			    parameters.put("probability", crossoverProbability_) ;
			    parameters.put("distributionIndex", crossoverDistributionIndex_) ;
			    Crossover crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);

			    parameters = new HashMap() ;
			    parameters.put("probability", mutationProbability_) ;
			    parameters.put("distributionIndex", mutationDistributionIndex_) ;
			    Mutation mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

			    // Selection Operator
			    parameters = null ;
			    Selection selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters) ;

			    // Add the operators to the algorithm
			    algorithm.addOperator("crossover",crossover);
			    algorithm.addOperator("mutation",mutation);
			    algorithm.addOperator("selection",selection);

			} else {
				throw new ClassNotFoundException(algorithmName);
			}

			algorithm.execute();
		} catch (JMException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}		

	}
}
