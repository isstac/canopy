package edu.cmu.sv.isstac.sampling.montecarlo;

import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isNondeterministicChoice;
import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isPCNode;

import java.util.ArrayList;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.PathQuantifier;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.search.SamplingListener;
import edu.cmu.sv.isstac.sampling.search.TerminationType;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;;

/**
 * @author Kasper Luckow
 *
 */
public class MonteCarloListener extends SamplingListener {
 private static final Logger logger = JPFLogger.getLogger(MonteCarloListener.class.getName());

  private final SimulationPolicy simulationPolicy;
  
  public MonteCarloListener(RewardFunction rewardFunction,
                            PathQuantifier pathQuantifier,
                            TerminationStrategy terminationStrategy,
                            SimulationPolicy simulationPolicy,
                            ChoicesStrategy choicesStrategy) {
    super(rewardFunction, pathQuantifier, terminationStrategy, choicesStrategy);
    this.simulationPolicy = simulationPolicy;
  }

  @Override
  public void newState(VM vm, ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices) {
    if(isPCNode(cg) || isNondeterministicChoice(cg)) {

      // If empty, we entered an invalid state
      if(eligibleChoices.isEmpty()) {
        String msg = "Entered invalid state: No eligible choices";
        logger.severe(msg);
        throw new MonteCarloAnalysisException(msg);
      }

      // Select a choice according to the simulation
      // strategy, e.g., randomized selection
      int choice = simulationPolicy.selectChoice(vm, cg, eligibleChoices);
      cg.select(choice);
    } else {
      String msg = "Unexpected CG: " + cg.getClass().getName();
      logger.severe(msg);
      throw new MonteCarloAnalysisException(msg);
    }
  }

  @Override
  public void pathTerminated(TerminationType termType, long reward, long pathVolume, long amplifiedReward, Search searchState) {
    // We don't do anything when a sample terminates
  }
}
