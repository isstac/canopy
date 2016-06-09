package edu.cmu.sv.isstac.sampling.mcts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.SamplingResult;
import edu.cmu.sv.isstac.sampling.SamplingResult.ResultContainer;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import edu.cmu.sv.isstac.sampling.exploration.termination.TerminationStrategy;
import edu.cmu.sv.isstac.sampling.montecarlo.MonteCarloAnalysisException;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.structure.DefaultNodeFactory;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
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
class MCTSListener extends PropertyListenerAdapter {
  private enum MCTS_STATE {
    SELECTION {
      @Override
      public String toString() {
        return "Selection";
      }
    },
    SIMULATION {
      @Override
      public String toString() {
        return "Simulation";
      }
    };
  }
  
  private static final Logger logger = JPFLogger.getLogger(MCTSListener.class.getName());
  
  private final ChoicesStrategy choicesStrategy;
  
  private MCTS_STATE mctsState;
  private Node last = null;
  private Node root = null;
  private final NodeFactory nodeFactory;
  
  private final SelectionPolicy selectionPolicy;
  private final SimulationPolicy simulationPolicy;
  
  private final RewardFunction rewardFunction;
  
  private final TerminationStrategy terminationStrategy;
  
  private boolean expandedFlag = false;
  private int expandedChoice = -1;
  
  // Holds the largest rewards found (note: we assume a deterministic system!)
  // for succ, fail and grey. Maybe we only want to keep one of them?
  // In addition it holds various statistics about the exploration
  private SamplingResult result = new SamplingResult();

  // Observers are notified upon termination. We can add more fine grained 
  // events if necessary, e.g. emit event after each sample.
  private Set<AnalysisEventObserver> observers = new HashSet<>();
  
  public MCTSListener(SelectionPolicy selectionPolicy,
      SimulationPolicy simulationPolicy,
      RewardFunction rewardFunction,
      ChoicesStrategy choicesStrategy,
      TerminationStrategy terminationStrategy) {
    this.choicesStrategy = choicesStrategy;
    this.selectionPolicy = selectionPolicy;
    this.simulationPolicy = simulationPolicy;
    this.rewardFunction = rewardFunction;
    this.terminationStrategy = terminationStrategy;
    
    this.mctsState = MCTS_STATE.SELECTION;
    
    //For now we just stick with the default factory
    this.nodeFactory = new DefaultNodeFactory();
  }
  
  public void addEventObserver(AnalysisEventObserver observer) {
    observers.add(observer);
  }
    
  @Override
  public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
    if(this.nodeFactory.isSupportedChoiceGenerator(cg)) {
      if(expandedFlag) {
        assert mctsState == MCTS_STATE.SIMULATION;
        last = this.nodeFactory.create(last, cg, expandedChoice);
        expandedFlag = false;
      }
      
      ArrayList<Integer> eligibleChoices = choicesStrategy.getEligibleChoices(cg);
      if(eligibleChoices.isEmpty()) {
        String msg = "Entered invalid state: No eligible choices";
        logger.severe(msg);
        throw new MonteCarloAnalysisException(msg);
      }
      
      int choice;
      if(mctsState == MCTS_STATE.SELECTION) {
        if(root == null) { // create root
          root = last = this.nodeFactory.create(null, cg, -1); 
        }
        if(isFrontierNode(last)) { // Perform expansion step
          ArrayList<Integer> unexpandedEligibleChoices = getUnexpandedEligibleChoices(last, eligibleChoices);
          choice = expandedChoice = selectionPolicy.expandChild(last, unexpandedEligibleChoices);
          expandedFlag = true;
          mctsState = MCTS_STATE.SIMULATION; // Proceed to simulation step
        } else { // perform selection step
          last = selectionPolicy.selectBestChild(last, eligibleChoices);
          choice = last.getChoice();
        }
      } else if(mctsState == MCTS_STATE.SIMULATION) {
        choice = simulationPolicy.selectChoice(cg, eligibleChoices);
      } else {
        String msg = "Entered invalid MCTS state: " + mctsState;
        logger.severe(msg);
        throw new MCTSAnalysisException(msg);
      }
      cg.select(choice);
    } else {
      String msg = "Unexpected CG: " + cg.getClass().getName();
      logger.severe(msg);
      throw new MCTSAnalysisException(msg);
    }
  }
  
  private ArrayList<Integer> getUnexpandedEligibleChoices(Node n, ArrayList<Integer> eligibleChoices) {
    ArrayList<Integer> unexpandedEligibleChoices = new ArrayList<>();
    Collection<Node> expandedChildren = n.getChildren();
    Set<Integer> childChoices = new HashSet<>();
    
    //Could expose a method in a node to obtain the following
    for(Node child : expandedChildren) {
      childChoices.add(child.getChoice());
    }
    
    for(int eligibleChoice : eligibleChoices) {
      if(!childChoices.contains(eligibleChoice))
        unexpandedEligibleChoices.add(eligibleChoice);
    }
    assert !unexpandedEligibleChoices.isEmpty();
    
    return unexpandedEligibleChoices;
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
    if(expandedFlag) {
      last = this.nodeFactory.create(last, null, expandedChoice);
      expandedFlag = false;
    }
    
    // Compute reward beased on reward function
    long reward = rewardFunction.computeReward(vm);
    logger.finest("Reward computed: " + reward);
    
    result.incNumberOfSamples();
    logger.finest("Sample number: " + result.getNumberOfSamples());
    
    // Perform backup phase
    for(Node n = last; n != null; n = n.getParent()) {
      updater.update(n, reward);
      n.incVisitedNum();
    }
    
    // Check if the reward obtained is greater than
    // previously observed for this event (succ, fail, grey)
    // and update the best result accordingly
    ResultContainer bestResult = updater.getResultStateForEvent();
    if(reward > bestResult.getReward()) {
      bestResult.setReward(reward);
      bestResult.setSampleNumber(result.getNumberOfSamples());
      Path path = new Path(vm.getChoiceGenerator());
      bestResult.setPath(path);
      // Supposedly getPC defensively (deep) copies the current PC 
      PathCondition pc = PathCondition.getPC(vm);
      bestResult.setPathCondition(pc);
    }
    
    if(terminationStrategy.terminate(vm, this.result)) {
      vm.getSearch().terminate();
      
      // Notify observers with termination event
      for(AnalysisEventObserver obs : this.observers) {
        obs.analysisDone(result);
      }
    }
    
    resetExploration();
  }
  
  /**
   * Compute reward for successful termination (succ)
   */
  @Override
  public void stateAdvanced(Search search) {
    if(search.isEndState()) {
      logger.finest("Successful termination.");
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
    logger.finest("Property violation/exception thrown.");
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
    logger.finest("Search constraint hit.");
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
    this.mctsState = MCTS_STATE.SELECTION;
    this.last = this.root;
  }
  
  private static boolean isFrontierNode(Node node) {
    return node.getChildren().size() < node.getTotalChoicesNum();
  }
}
