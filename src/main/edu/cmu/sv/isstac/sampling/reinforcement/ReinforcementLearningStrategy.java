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

/**
 * @author Kasper Luckow
 *
 */
public class ReinforcementLearningStrategy implements AnalysisStrategy {
  private static final Logger logger = JPFLogger.getLogger(ReinforcementLearningStrategy.class.getName());

  // Params for reinforcement learning
  private final int samplesPerOptimization;
  private final double epsilon;
  private final double historyWeight;

  private RLNodeFactoryDecorator nodeFactory;

  private RLNode root;
  private RLNode lastNode;
  private int lastChoice = -1;

  private int samplesSinceOptimization = 0;

  private final Random rng;

  private Map<Integer, RLNode> nodes = new HashMap<>();

  public ReinforcementLearningStrategy(int samplesPerOptimization, double epsilon,
                                       double historyWeight, SPFModelCounter modelCounter, long seed) {
    this.samplesPerOptimization = samplesPerOptimization;
    this.epsilon = epsilon;
    this.historyWeight = historyWeight;
    this.rng = new Random(seed);

    this.nodeFactory = new RLNodeFactoryDecorator(new DefaultNodeFactory(), modelCounter);
  }

  @Override
  public void makeStateChoice(VM vm, ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices) {
    //TODO: Later expand this to support nondeterministic choices too
    if(this.nodeFactory.isSupportedChoiceGenerator(cg)) {

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

      logger.fine("Reinforcement learning policy selected choice " + lastChoice +
          "for condition at line " + cg.getInsn().getLineNumber());
      cg.select(lastChoice);
    } else {
      String msg = "Unexpected CG: " + cg.getClass().getName();
      logger.severe(msg);
      throw new RLAnalysisException(msg);
    }
  }

  private double getChoiceQuality(RLNode parent, int choice) {
    long subDomainParent = parent.getSubdomainSize();
    assert subDomainParent > 0;

    double quality;
    // This is important:
    // If the parent does *not* have a child node for a particular choice (i.e. the subtree
    // rooted at the choice has never been sampled), then the quality for that choice is the initial
    // probability of being selected i.e. 1/numChoices. This is the same in jpf-reliability
    if(parent.hasChildForChoice(choice)) {
      RLNode child = (RLNode)parent.getChild(choice);
      logger.warning("assuming succ reward *ONLY* for quality calculation (might want to make " +
          "this optional)");
      quality = child.getReward().getSucc() / (double)subDomainParent;
    } else {
      quality = 1 / (double)parent.getTotalChoicesNum();
    }
    return quality;
  }

  private void performOptimizationStep() {

    // Iterate over all nodes in the tree and update the probabilities for selecting choices
    // (children)
    for(RLNode node : this.nodes.values()) {

      //Of course, only reinforce nodes that can make choices (i.e. not final nodes)
      if(node.getTotalChoicesNum() > 0) {
        double qualitySum = 0.0;
        double maxQuality = -1.0;
        int maxQualityChoice = -1;

        // Find choice with max quality
        // Assume that choices are 0..TotalChoices
        for(int choice = 0; choice < node.getTotalChoicesNum(); choice++) {
          double quality = getChoiceQuality(node, choice);
          if(quality > maxQuality) {
            maxQuality = quality;
            maxQualityChoice = choice;
          }
          qualitySum += quality;
        }
        if(qualitySum > 0.0) {

          // Assume that choices are 0..TotalChoices
          for(int choice = 0; choice < node.getTotalChoicesNum(); choice++) {

            double updatedProb = 0.0;

            //max quality choice gets the best prob
            if(choice == maxQualityChoice) {
              updatedProb += 1.0 - this.epsilon;
            }
            double quality = getChoiceQuality(node, choice);
            updatedProb += this.epsilon * (quality / qualitySum);

            double oldProb = node.getChoiceProbability(choice);
            double newProb = (this.historyWeight * oldProb)
                + ((1 - this.historyWeight) * updatedProb);
            node.setChoiceProbability(choice, newProb);
          }
        } else {
          String msg = "Quality sum must be positive";
          logger.severe(msg);
          throw new RLAnalysisException(msg);
        }
      }
    }
  }

  @Override
  public void pathTerminated(TerminationType termType, long reward,
                             long pathVolume, long amplifiedReward,
                             Search searchState, boolean hasBeenExploredBefore) {

    // If this path has been seen before (e.g. if pruning was not used), then we don't perform
    // back progation of rewards!
    if(!hasBeenExploredBefore) {
      //TODO: slightly messy way of making the final node
      if (!lastNode.hasChildForChoice(lastChoice)) {
        try {
          lastNode = nodeFactory.create(lastNode, null, lastChoice);
        } catch (NodeCreationException e) {
          String msg = "Could not create final node";
          logger.severe(msg);
          throw new RLAnalysisException(msg);
        }
      } else {
        lastNode = (RLNode) lastNode.getChild(lastChoice);
      }

      // perform backpropagation of the rewards
      // NOTE: here we are **ACCUMULATING** rewards
      // Maybe we should take MAX reward of children instead
      // These are two different approaches
      // Another important note here is the samplesSinceOptimization is *only* updated for unique
      // paths i.e. if we sample the same path multiple times (which can happen if pruning is not
      // enabled, in which case hasBeenExploredBefore can be true), we currently don't use that
      // information for anything. This may not be what we want, e.g., maybe we would like to
      // increment samplesSinceOptimization regardless of whether it has been seen before or not
      BackPropagator.cumulativeRewardPropagation(lastNode, amplifiedReward, pathVolume, termType);

      samplesSinceOptimization++;

      // We perform optimization (or reinforcement) of choices after samplesPerOptimization
      if (samplesSinceOptimization >= this.samplesPerOptimization) {
        performOptimizationStep();
        samplesSinceOptimization = 0;
      }
    }

    // reset exploration to drive a new round of sampling
    this.lastNode = this.root;
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
    // We don't track anything here
  }
}
