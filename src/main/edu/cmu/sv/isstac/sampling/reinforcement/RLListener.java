package edu.cmu.sv.isstac.sampling.reinforcement;

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
import edu.cmu.sv.isstac.sampling.quantification.PathQuantifier;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.structure.DefaultNodeFactory;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
class RLListener extends PropertyListenerAdapter {

  private static final Logger LOGGER = JPFLogger.getLogger(RLListener.class.getName());

  private final ChoicesStrategy choicesStrategy;

  private Node last = null;
  private Node root = null;
  private final NodeFactory nodeFactory;

  private final SimulationPolicy simulationPolicy;

  private final RewardFunction rewardFunction;
  private final PathQuantifier pathQuantifier;

  private final TerminationStrategy terminationStrategy;

  // Holds the largest rewards found (note: we assume a deterministic system!)
  // for succ, fail and grey. Maybe we only want to keep one of them?
  // In addition it holds various statistics about the exploration
  private SamplingResult result = new SamplingResult();

  // Observers are notified upon termination. We can add more fine grained
  // events if necessary, e.g. emit event after each sample.
  private Set<AnalysisEventObserver> observers = new HashSet<>();

  public RLListener(SimulationPolicy simulationPolicy,
                    RewardFunction rewardFunction,
                    PathQuantifier pathQuantifier,
                    ChoicesStrategy choicesStrategy,
                    TerminationStrategy terminationStrategy) {
    this.choicesStrategy = choicesStrategy;
    this.simulationPolicy = simulationPolicy;
    this.rewardFunction = rewardFunction;
    this.pathQuantifier = pathQuantifier;
    this.terminationStrategy = terminationStrategy;
    
    //For now we just stick with the default factory
    this.nodeFactory = new DefaultNodeFactory();
  }
  
  public void addEventObserver(AnalysisEventObserver observer) {
    observers.add(observer);
  }
    
  @Override
  public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
    if(this.nodeFactory.isSupportedChoiceGenerator(cg)) {
      
      // If we expanded a child in the previous CG advancement,
      // we now want to create the node for that child.
      // We can only do that now, because otherwise the CG
      // is not available
//      if(expandedFlag) {
//        assert mctsState == MCTS_STATE.SIMULATION;
//        last = this.nodeFactory.create(last, cg, expandedChoice);
//        expandedFlag = false;
//      }
      
      // Get the eligible choices for this CG
      // based on the exploration strategy (e.g., pruning-based)
      ArrayList<Integer> eligibleChoices = choicesStrategy.getEligibleChoices(cg);
      
      // If empty, we entered an invalid state
      if(eligibleChoices.isEmpty()) {
        String msg = "Entered invalid state: No eligible choices";
        LOGGER.severe(msg);
        throw new RLAnalysisException(msg);
      }
      
      int choice = -1;

      // create root
      if(root == null) {
        root = last = this.nodeFactory.create(null, cg, -1);
      }

      // Select choice according to simulation policy, e.g., randomly
      choice = simulationPolicy.selectChoice(vm, cg, eligibleChoices);
      
      assert choice != -1;
      
      cg.select(choice);
    } else {
      String msg = "Unexpected CG: " + cg.getClass().getName();
      LOGGER.severe(msg);
      throw new RLAnalysisException(msg);
    }
  }
  
  // Can we transition to Java 8, please.
  private interface RewardUpdater {
    public void update(Node n, long reward);
    public ResultContainer getResultStateForEvent();
  }

  private void finishSample(VM vm, RewardUpdater updater) {
    // Create a final node.
    // It marks a leaf in the Monte Carlo Tree AND
    // in the symbolic execution tree, i.e. it will
    // only be created in the event that MCT reaches
    // and actual leaf in the symbolic execution tree
//    if(expandedFlag) {
//      last = this.nodeFactory.create(last, null, expandedChoice);
//      expandedFlag = false;
//    }
    
    // Compute reward based on reward function
    long reward = rewardFunction.computeReward(vm);

    // We compute the volume of the path here.
    // This can e.g. be based on model counting
    // The path volume is used in the backup phase,
    // i.e. the number of times a node has been visited,
    // corresponds to the (additive) volume of the path(s)
    // with that node as origin
    long pathVolume = this.pathQuantifier.quantifyPath(vm);
    assert pathVolume > 0;

    result.incNumberOfSamples();
    long numberOfSamples = result.getNumberOfSamples();

    LOGGER.info("Sample #: " + numberOfSamples + ", reward: " + reward + ", path volume: " + pathVolume);
    
    // Perform backup phase
    for(Node n = last; n != null; n = n.getParent()) {
      updater.update(n, reward);
      n.incVisitedNum(pathVolume);
    }
    
    // Check if the reward obtained is greater than
    // previously observed for this event (succ, fail, grey)
    // and update the best result accordingly
    ResultContainer bestResult = updater.getResultStateForEvent();
    
    // Notify observers with sample done event
    long realReward = reward / pathVolume;
    for(AnalysisEventObserver obs : this.observers) {
      obs.sampleDone(vm.getSearch(), numberOfSamples, realReward, pathVolume, bestResult);
    }

    if(realReward > bestResult.getReward()) {
      
      bestResult.setReward(realReward);
      bestResult.setSampleNumber(result.getNumberOfSamples());
      
      Path path = new Path(vm.getChoiceGenerator());
      bestResult.setPath(path);
      
      // Supposedly getPC defensively (deep) copies the current PC 
      PathCondition pc = PathCondition.getPC(vm);
      bestResult.setPathCondition(pc);
    }
    
    // Check if we should terminate the search
    // based on the result obtained
    if(terminationStrategy.terminate(vm, this.result)) {
      vm.getSearch().terminate();
      
      // Notify observers with termination event
      for(AnalysisEventObserver obs : this.observers) {
        obs.analysisDone(result);
      }
    }
    
    // Reset exploration to drive a new round of sampling
    resetExploration();
  }
  
  /**
   * Compute reward for successful termination (succ)
   */
  @Override
  public void stateAdvanced(Search search) {
    if(search.isEndState()) {
      LOGGER.fine("Successful termination.");
      finishSample(search.getVM(), new RewardUpdater() {
        @Override
        public void update(Node n, long reward) {
          n.getReward().incrementSucc(reward);
        }
        
        @Override
        public ResultContainer getResultStateForEvent() {
          return result.getMaxSuccResult();
        }
      });
    }
  }
  
  /**
   * Compute reward for "failure"
   */
  @Override
  public void exceptionThrown(VM vm, ThreadInfo currentThread, ElementInfo thrownException) {
    LOGGER.fine("Property violation/exception thrown.");
    finishSample(vm, new RewardUpdater() {
      @Override
      public void update(Node n, long reward) {
        n.getReward().incrementFail(reward);
      }
      
      @Override
      public ResultContainer getResultStateForEvent() {
        return result.getMaxFailResult();
      }
    });
  }
  
  /**
   * Compute reward for "grey" termination
   */
  @Override
  public void searchConstraintHit(Search search) {
    LOGGER.fine("Search constraint hit.");
    finishSample(search.getVM(), new RewardUpdater() {
      @Override
      public void update(Node n, long reward) {
        n.getReward().incrementGrey(reward);
      }

      @Override
      public ResultContainer getResultStateForEvent() {
        return result.getMaxGreyResult();
      }
    });
  }

  private void resetExploration() {
    this.last = this.root;
  }
}
