package edu.cmu.sv.isstac.sampling.reinforcement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.search.BackPropagator;
import edu.cmu.sv.isstac.sampling.search.TerminationType;
import edu.cmu.sv.isstac.sampling.structure.DefaultNodeFactory;
import edu.cmu.sv.isstac.sampling.structure.NodeCreationException;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

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

  private NodeFactory<RLNode> nodeFactory;

  private RLNode root;
  private RLNode lastNode;
  private int lastChoice = -1;

  private int samplesSinceOptimization = 0;

  private final Random rng;

  // Not happy with mapping entire paths to notes. Essentially we just want a map from a CG to an
  // rl node, but the stateid field of CG's seems not to give us a unique id. *sigh*
  private Map<Path, RLNode> nodes = new HashMap<>();

  public ReinforcementLearningStrategy(int samplesPerOptimization, double epsilon,
                                       double historyWeight, NodeFactory<RLNode> nodeFactory, long seed) {
    this.samplesPerOptimization = samplesPerOptimization;
    this.epsilon = epsilon;
    this.historyWeight = historyWeight;
    this.rng = new Random(seed);

    this.nodeFactory = nodeFactory;
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
     // throw new RLAnalysisException(msg);
    }
  }

  private void performOptimizationStep() {
    logger.info("Performing optimization step");
    // Iterate over all nodes in the tree and update the probabilities for selecting choices
    // (children)
    for(RLNode node : this.nodes.values()) {

      //If the node is ignored, we skip optimizing it
      if(!isIgnored(node)) {

        //Of course, only reinforce nodes that can make choices (i.e. not final nodes)
        if (node.getTotalChoicesNum() > 0) {
          double qualitySum = 0.0;
          double maxQuality = -1.0;
          int maxQualityChoice = -1;

          // Find choice with max quality
          // Assume that choices are 0..TotalChoices
          for (int choice = 0; choice < node.getTotalChoicesNum(); choice++) {
            double quality = node.getChoiceQuality(choice);
            if (quality > maxQuality) {
              maxQuality = quality;
              maxQualityChoice = choice;
            }
            qualitySum += quality;
          }
          if (qualitySum > 0.0) {

            // Assume that choices are 0..TotalChoices
            for (int choice = 0; choice < node.getTotalChoicesNum(); choice++) {
              double updatedProb = 0.0;

              //max quality choice gets the best prob
              if (choice == maxQualityChoice) {
                updatedProb += 1.0 - this.epsilon;
              }
              double quality = node.getChoiceQuality(choice);
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
  }

  private boolean isIgnored(RLNode node) {
    //This is pretty weird, but we need a check like this since nodes a created in the tree
    // *before* spf determines whether the path is infeasible or not. If it is infeasible, and
    // thus this branch will be ignored, the node should not count in the quality computation and
    // furthermore, it will never have its visited count incremented since this takes place only
    // for terminating paths. If the CG optimization is on, then ignored states will never be
    // sampled and therefore---in this case---this check is unnecessary. However if it is off or
    // incremental solving is used (which currently enforces CG optimization to be off), then we
    // will need a check like this to not hit an assertion error when calling getChoiceQuality
    return node.getVisitedNum() == 0;
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
    } else {
      logger.warning("Path has been explored before (Pruning is turned off? If not, this is an " +
          "error). Reinforcement learning does not propagate reward!");
    }

    // reset exploration to drive a new round of sampling
    this.lastNode = this.root;
  }

  private RLNode getNode(ChoiceGenerator<?> cg, RLNode lastNode, int lastChoice) {
    Path path = new Path(cg);
    RLNode node = nodes.get(path);
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

      nodes.put(path, node);
    }
    return node;
  }

  @Override
  public void newSampleStarted(Search samplingSearch) {
    // We don't track anything here
  }
}
