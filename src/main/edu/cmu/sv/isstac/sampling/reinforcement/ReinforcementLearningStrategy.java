package edu.cmu.sv.isstac.sampling.reinforcement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.search.BackPropagator;
import edu.cmu.sv.isstac.sampling.search.TerminationType;
import edu.cmu.sv.isstac.sampling.structure.DefaultNodeFactory;
import edu.cmu.sv.isstac.sampling.structure.NodeCreationException;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isPCNode;

;

/**
 * @author Kasper Luckow
 *
 */
public class ReinforcementLearningStrategy implements AnalysisStrategy {
  private static final Logger logger = JPFLogger.getLogger(ReinforcementLearningStrategy.class.getName());

  // Params for reinforcement learning
  private final int samplesPerOptimization;
  private final double epsilon;
  private final double history;

  private RLNodeFactoryDecorator nodeFactory;

  private RLNode root;
  private RLNode lastNode;
  private int lastChoice = -1;

  private final Random rng;

  private Map<Integer, RLNode> nodes = new HashMap<>();

  public ReinforcementLearningStrategy(int samplesPerOptimization, double epsilon,
                                       double history, SPFModelCounter modelCounter, long seed) {
    this.samplesPerOptimization = samplesPerOptimization;
    this.epsilon = epsilon;
    this.history = history;
    this.rng = new Random(seed);

    this.nodeFactory = new RLNodeFactoryDecorator(new DefaultNodeFactory(), modelCounter);
  }

  @Override
  public void makeStateChoice(VM vm, ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices) {
    //TODO: Later expand this to support nondeterministic choices too
    if(isPCNode(cg)) {

      RLNode node = getNode(cg, lastNode, lastChoice);


      // We make a choice by flipping a coin. Choices are made according to their respective
      // probabilities of selection which is adjusted at scheduler improvement (i.e. after
      // samplesPerOptimization samples have been drawn)
      double probSumForChoices = node.getProbabilitySum(eligibleChoices);
      double rand = this.rng.nextDouble() * probSumForChoices;
      int selectedChoice = -1;
      double accumulatedProb = 0.0;
      for(int i = 0; i < eligibleChoices.size(); i++) {
        int choice = eligibleChoices.get(i);
        accumulatedProb += node.getChoiceProbability(choice);
        if(rand <= accumulatedProb) {
          selectedChoice = choice;
          break;
        }
      }
      assert selectedChoice != -1;

      lastNode = node;
      lastChoice = selectedChoice;
      cg.select(lastChoice);
    } else {
      String msg = "Unexpected CG: " + cg.getClass().getName();
      logger.severe(msg);
      throw new RLAnalysisException(msg);
    }
  }

  private RLNode getNode(ChoiceGenerator<?> cg, RLNode lastNode, int lastChoice) {
    RLNode node = nodes.get(cg.getStateId());
    if(node == null) {
      try {

        //TODO: maybe we don't need to keep track of the root
        if(root == null) {
          node = root = nodeFactory.create(null, cg, -1);
        } else {
          node = this.nodeFactory.create(lastNode, cg, lastChoice);
        }
      } catch (NodeCreationException e) {
        String msg = "Could not create root node";
        logger.severe(msg);
        throw new RLAnalysisException(msg);
      }
    }
    return node;
  }

  @Override
  public void newSampleStarted(Search samplingSearch) {

  }

  @Override
  public void pathTerminated(TerminationType termType, long reward, long pathVolume,
                             long amplifiedReward, Search searchState) {
    //TODO: slightly messy way of making the final node
    if(!lastNode.hasChildForChoice(lastChoice)) {
      try {
        lastNode = nodeFactory.create(lastNode, null, lastChoice);
      } catch (NodeCreationException e) {
        String msg = "Could not create final node";
        logger.severe(msg);
        throw new RLAnalysisException(msg);
      }
    } else {
      lastNode = (RLNode)lastNode.getChild(lastChoice);
    }

    // perform backpropagation of the rewards
    // NOTE: here we are **ACCUMULATING** rewards
    // Maybe we should take MAX reward of children instead
    // These are two different approaches
    BackPropagator.cumulativeRewardPropagation(lastNode, amplifiedReward, pathVolume, termType);

    // reset exploration to drive a new round of sampling
    this.lastNode = this.root;
  }
}
