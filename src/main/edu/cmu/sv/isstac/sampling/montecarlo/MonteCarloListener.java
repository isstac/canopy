package edu.cmu.sv.isstac.sampling.montecarlo;

import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isNondeterministicChoice;
import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isPCNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult.ResultContainer;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;;

/**
 * @author Kasper Luckow
 *
 */
public class MonteCarloListener extends PropertyListenerAdapter {
 private static final Logger logger = JPFLogger.getLogger(MonteCarloListener.class.getName());
  
  private final ChoicesStrategy choicesStrategy;
  
  private final SimulationPolicy simulationPolicy;
  
  private final RewardFunction rewardFunction;
  
  private TerminationStrategy terminationStrategy;
  
  private SamplingResult result = new SamplingResult();

  private Set<AnalysisEventObserver> observers = new HashSet<>();
  
  public MonteCarloListener(SimulationPolicy simulationPolicy,
      RewardFunction rewardFunction,
      ChoicesStrategy choicesStrategy,
      TerminationStrategy terminationStrategy) {
    this.choicesStrategy = choicesStrategy;
    this.simulationPolicy = simulationPolicy;
    this.rewardFunction = rewardFunction;
    this.terminationStrategy = terminationStrategy;
  }
  
  public void addEventObserver(AnalysisEventObserver observer) {
    observers.add(observer);
  }

  @Override
  public void searchStarted(Search search) {
    for(AnalysisEventObserver obs : this.observers) {
      obs.analysisStarted(search);
    }
  }

  @Override
  public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
    if(isPCNode(cg) || isNondeterministicChoice(cg)) {
      
      // Get the eligible choices for this CG
      // based on the exploration strategy (e.g., pruning-based)
      ArrayList<Integer> eligibleChoices = choicesStrategy.getEligibleChoices(cg);
      
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
  
  private void finishSample(VM vm, ResultContainer currentBestResult) {

    // Compute reward based on reward function
    long reward = rewardFunction.computeReward(vm);
    logger.info("Reward computed: " + reward);
    
    result.incNumberOfSamples();
    long numberOfSamples = result.getNumberOfSamples();
    logger.info("Sample number: " + numberOfSamples);
    
    // Notify observers with sample done event
    for(AnalysisEventObserver obs : this.observers) {
      obs.sampleDone(vm.getSearch(), numberOfSamples, reward, 1, currentBestResult);
    }
    
    // Check if the reward obtained is greater than
    // previously observed for this event (succ, fail, grey)
    // and update the best result accordingly
    if(reward > currentBestResult.getReward()) {
      currentBestResult.setReward(reward);
      currentBestResult.setSampleNumber(result.getNumberOfSamples());
      Path path = new Path(vm.getChoiceGenerator());
      currentBestResult.setPath(path);
      
      // Supposedly getPC defensively (deep) copies the current PC 
      PathCondition pc = PathCondition.getPC(vm);
      currentBestResult.setPathCondition(pc);
    }
    // Check if we should terminate the search
    // based on the result obtained
    // searchFinished will be called later
    if(terminationStrategy.terminate(vm, this.result)) {
      vm.getSearch().terminate();
    }
  }

  @Override
  public void searchFinished(Search search) {
    notifyTermination(search.getVM());
  }

  private void notifyTermination(VM vm) {
    // Notify observers with termination event
    for(AnalysisEventObserver obs : this.observers) {
      obs.analysisDone(result);
    }
  }
  
  /**
   * Compute reward for successful termination (succ)
   */
  @Override
  public void stateAdvanced(Search search) {
    if(search.isEndState()) {
      logger.fine("Successful termination.");
      finishSample(search.getVM(), this.result.getMaxSuccResult());
    }
  }
  
  /**
   * Compute reward for "failure"
   */
  @Override
  public void exceptionThrown(VM vm, ThreadInfo currentThread, ElementInfo thrownException) {
    logger.fine("Property violation/exception thrown.");
    finishSample(vm, this.result.getMaxFailResult());
  }
  
  /**
   * Compute reward for "grey" termination
   */
  @Override
  public void searchConstraintHit(Search search) {
    logger.fine("Search constraint hit.");
    finishSample(search.getVM(), this.result.getMaxGreyResult());
  }

  public void setTerminationStrategy(TerminationStrategy terminationStrategy) {
    this.terminationStrategy = terminationStrategy;
  }
}
