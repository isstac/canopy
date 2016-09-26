package edu.cmu.sv.isstac.sampling.mcts;

import org.apache.commons.collections15.map.HashedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.search.TerminationType;
import edu.cmu.sv.isstac.sampling.structure.DefaultNodeFactory;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
class MCTSStrategy implements AnalysisStrategy {

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

  @FunctionalInterface
  private interface RewardUpdater {
    void update(Node n, long reward);
  }
  private static final Map<TerminationType, RewardUpdater> rewardUpdaters = new HashedMap<>();
  static {
    rewardUpdaters.put(TerminationType.SUCCESS, (n, reward) -> n.getReward().incrementSucc(reward));
    rewardUpdaters.put(TerminationType.ERROR, (n, reward) -> n.getReward().incrementFail(reward));
    rewardUpdaters.put(TerminationType.CONSTRAINT_HIT, (n, reward) -> n.getReward().incrementGrey(reward));
  }

  private static final Logger logger = JPFLogger.getLogger(MCTSStrategy.class.getName());
  
  private MCTS_STATE mctsState;
  private Node last = null;
  private Node root = null;
  private final NodeFactory nodeFactory;
  
  private final SelectionPolicy selectionPolicy;
  private final SimulationPolicy simulationPolicy;
  
  private boolean expandedFlag = false;
  private int expandedChoice = -1;

  public MCTSStrategy(SelectionPolicy selectionPolicy,
                      SimulationPolicy simulationPolicy) {
    this.selectionPolicy = selectionPolicy;
    this.simulationPolicy = simulationPolicy;

    this.mctsState = MCTS_STATE.SELECTION;

    //For now we just stick with the default factory
    this.nodeFactory = new DefaultNodeFactory();
  }

  @Override
  public void makeStateChoice(VM vm, ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices) {
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

  @Override
  public void newSampleStarted(Search samplingSearch) {
    // We don't need to track anything here
  }
}