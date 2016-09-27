package edu.cmu.sv.isstac.sampling.reinforcement;

import java.util.ArrayList;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.montecarlo.MonteCarloAnalysisException;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.search.TerminationType;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isNondeterministicChoice;
import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isPCNode;

;

/**
 * @author Kasper Luckow
 *
 */
public class ReinforcementLearningStrategy implements AnalysisStrategy {
  private static final Logger logger = JPFLogger.getLogger(ReinforcementLearningStrategy.class.getName());

  private final int samplesPerOptimization;
  private final double epsilon;
  private final double history;

  public ReinforcementLearningStrategy(int samplesPerOptimization, double epsilon, double history) {
    this.samplesPerOptimization = samplesPerOptimization;
    this.epsilon = epsilon;
    this.history = history;
  }

  @Override
  public void makeStateChoice(VM vm, ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices) {
    if(isPCNode(cg) || isNondeterministicChoice(cg)) {


      cg.select(1);
    } else {
      String msg = "Unexpected CG: " + cg.getClass().getName();
      logger.severe(msg);
      throw new MonteCarloAnalysisException(msg);
    }
  }

  @Override
  public void newSampleStarted(Search samplingSearch) {

  }

  @Override
  public void pathTerminated(TerminationType termType, long reward, long pathVolume, long amplifiedReward, Search searchState) {
    // We don't do anything when a sample terminates
  }
}
