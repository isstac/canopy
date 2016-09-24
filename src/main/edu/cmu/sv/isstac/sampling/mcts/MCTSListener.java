package edu.cmu.sv.isstac.sampling.mcts;

import org.apache.commons.collections15.map.HashedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.analysis.AbstractAnalysisProcessor;
import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.MCTSEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult.ResultContainer;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import edu.cmu.sv.isstac.sampling.quantification.PathQuantifier;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.search.SamplingListener;
import edu.cmu.sv.isstac.sampling.search.TerminationType;
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
class MCTSListener extends SamplingListener {

  public void addEventObserver(AnalysisEventObserver aDefault) {
    observers.add(aDefault);
  }

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
  
  private boolean expandedFlag = false;
  private int expandedChoice = -1;
  
  public MCTSListener(SelectionPolicy selectionPolicy,
                      SimulationPolicy simulationPolicy,
                      RewardFunction rewardFunction,
                      PathQuantifier pathQuantifier,
                      ChoicesStrategy choicesStrategy,
                      TerminationStrategy terminationStrategy) {
    super(rewardFunction, pathQuantifier, terminationStrategy);
    this.choicesStrategy = choicesStrategy;
    this.selectionPolicy = selectionPolicy;
    this.simulationPolicy = simulationPolicy;
    
    this.mctsState = MCTS_STATE.SELECTION;
    
    //For now we just stick with the default factory
    this.nodeFactory = new DefaultNodeFactory();
  }


  @Override
  public void newState(VM vm, ChoiceGenerator<?> cg) {
    if(this.nodeFactory.isSupportedChoiceGenerator(cg)) {
      
      // If we expanded a child in the previous CG advancement,
      // we now want to create the node for that child.
      // We can only do that now, because otherwise the CG
      // is not available
      if(expandedFlag) {
        assert mctsState == MCTS_STATE.SIMULATION;
        last = this.nodeFactory.create(last, cg, expandedChoice);
        expandedFlag = false;
      }

      // Get the eligible choices for this CG
      // based on the exploration strategy (e.g., pruning-based)
      ArrayList<Integer> eligibleChoices = choicesStrategy.getEligibleChoices(cg);
      
      // If empty, we entered an invalid state
      if(eligibleChoices.isEmpty()) {
        String msg = "Entered invalid state: No eligible choices";
        logger.severe(msg);
        throw new MCTSAnalysisException(msg);
      }
      
      int choice = -1;
      
      // Check if we are currently in the Selection phase of MCTS
      if(mctsState == MCTS_STATE.SELECTION) {
        
        // create root
        if(root == null) { 
          root = last = this.nodeFactory.create(null, cg, -1); 
        }
        
        // Check if node is a "frontier", i.e. it has eligible, unexpanded children
        // In this case, we perform the expansion step of MCTS
        if(isFrontierNode(last, eligibleChoices)) {
          ArrayList<Integer> unexpandedEligibleChoices = getUnexpandedEligibleChoices(last, eligibleChoices);
          
          // Select the unexpanded children according to our selection policy, e.g. randomly
          choice = expandedChoice = selectionPolicy.expandChild(last, unexpandedEligibleChoices);
          expandedFlag = true;
          
          // After expansion, we proceed to simulation step of MCTS
          mctsState = MCTS_STATE.SIMULATION; 
        } else {
          
          // If it was not a frontier node, we perform the selection step of MCTS
          // A node is selected based on the selection policy, e.g., classic UCB
          last = selectionPolicy.selectBestChild(last, eligibleChoices);
          choice = last.getChoice();
        }
      } else if(mctsState == MCTS_STATE.SIMULATION) {
        
        // Select choice according to simulation policy, e.g., randomly
        choice = simulationPolicy.selectChoice(vm, cg, eligibleChoices);
      } else {
        String msg = "Entered invalid MCTS state: " + mctsState;
        logger.severe(msg);
        throw new MCTSAnalysisException(msg);
      }
      
      assert choice != -1;
      
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
    
    // We only select the unexpanded children
    // that are eligible for selection, e.g.,
    // not pruned.
    for(int eligibleChoice : eligibleChoices) {
      if(!childChoices.contains(eligibleChoice))
        unexpandedEligibleChoices.add(eligibleChoice);
    }
    
    // We have hit an illegal state if there
    // are no choices that can be expanded
    if(unexpandedEligibleChoices.isEmpty()) {
      String msg = "No eligible, unexpanded children possible";
      logger.severe(msg);
      throw new MCTSAnalysisException(new IllegalStateException(msg));
    }
    
    return unexpandedEligibleChoices;
  }

  // Can we transition to Java 8, please.
  private interface RewardUpdater {
    public void update(Node n, long reward);
  }
  private static final Map<TerminationType, RewardUpdater> rewardUpdaters = new HashedMap<>();
  static {
    rewardUpdaters.put(TerminationType.SUCCESS, new RewardUpdater() {
      @Override
      public void update(Node n, long reward) {
        n.getReward().incrementSucc(reward);
      }
    });
    rewardUpdaters.put(TerminationType.ERROR, new RewardUpdater() {
      @Override
      public void update(Node n, long reward) {
        n.getReward().incrementFail(reward);
      }
    });
    rewardUpdaters.put(TerminationType.CONSTRAINT_HIT, new RewardUpdater() {
      @Override
      public void update(Node n, long reward) {
        n.getReward().incrementGrey(reward);
      }
    });
  }

  @Override
  public void pathTerminated(TerminationType termType, long reward, long pathVolume, long amplifiedReward, Search searchState) {
    // Create a final node.
    // It marks a leaf in the Monte Carlo Tree AND
    // in the symbolic execution tree, i.e. it will
    // only be created in the event that MCT reaches
    // and actual leaf in the symbolic execution tree
    if(expandedFlag) {
      last = this.nodeFactory.create(last, null, expandedChoice);
      expandedFlag = false;
    }
    RewardUpdater updater = rewardUpdaters.get(termType);
    // Perform backup phase
    for(Node n = last; n != null; n = n.getParent()) {
      updater.update(n, amplifiedReward);
      n.incVisitedNum(pathVolume);
    }

    // Reset exploration to drive a new round of sampling
    this.mctsState = MCTS_STATE.SELECTION;
    this.last = this.root;
  }
  
  private boolean isFrontierNode(Node node, Collection<Integer> eligibleChoices) {
    for(int eligibleChoice : eligibleChoices) {
      if(!node.hasChildForChoice(eligibleChoice))
        return true;
    }
    return false;
  }
}
