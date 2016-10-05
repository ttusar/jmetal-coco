//  CocoProblem.java

import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.solutionType.BinaryRealSolutionType;
import jmetal.encodings.solutionType.RealSolutionType;
import jmetal.util.JMException;

/** 
 * Class representing problem CocoProblem 
 */
public class CocoProblem extends jmetal.core.Problem {   

  public static Problem PROBLEM;

  public CocoProblem (Problem PROBLEM, 
                      int dimension,
                      int numberOfObjectives, 
                      double[] lowerBounds,
                      double[] upperBounds) {

    this.PROBLEM = PROBLEM;

    numberOfVariables_  = dimension;
    numberOfObjectives_ = numberOfObjectives;
    numberOfConstraints_= 0;
    problemName_        = "CocoProblem";
        
    lowerLimit_ = lowerBounds;
    upperLimit_ = upperBounds;
        
    solutionType_ = new RealSolutionType(this) ;   

  }

  /** 
  * Evaluates a solution 
  * @param solution The solution to evaluate
   * @throws JMException 
  */    
  public void evaluate(Solution solution) throws JMException {
    Variable[] gen  = solution.getDecisionVariables();
                
    double [] x = new double[numberOfVariables_];
    double [] y = new double[numberOfObjectives_];
        
    for (int i = 0; i < numberOfVariables_; i++)
      x[i] = gen[i].getValue();

    y = PROBLEM.evaluateFunction(x);

    for (int i = 0; i < numberOfObjectives_; i++)
      solution.setObjective(i,y[i]);        
  } // evaluate   
  
}

