package edu.cmu.sv.isstac.sampling.mcts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.cmu.sv.isstac.sampling.structure.Node;

/**
 * @author Kasper Luckow
 *
 */
public class UCBPolicy implements SelectionPolicy {

  private final Random rng;
  private final double biasParameter;
  
  public UCBPolicy(long seed, double biasParameter) {
    this.rng = new Random(seed);
    this.biasParameter = biasParameter;
  }
  
  public UCBPolicy(double biasParameter) {
    this.rng = new Random();
    this.biasParameter = biasParameter;
  }
  
  @Override
  public Node selectBestChild(Node currNode, ArrayList<Integer> eligibleChoices) {
    double bestUct = Double.NEGATIVE_INFINITY;
    Node bestChild = null;
    
    //From the eligible choices, select the child with the highest UCT value
    for(int choice : eligibleChoices) {
      Node child = currNode.getChild(choice);
      double uct = computeUCT(currNode, child);
      if(uct > bestUct) {
        bestUct = uct;
        bestChild = child;
      }
    }
    assert bestChild != null;
    
    return bestChild;
  }
  
  private double computeUCT(Node parent, Node child) {
    
    // NOTE: the best child is determined in terms of its reward for successful termination
    long q = child.getReward().getSucc();
    long nc = child.getVisitedNum();
    long np = parent.getVisitedNum();
    double uct = ((double)q/nc) + this.biasParameter * Math.sqrt(2 * Math.log(np) / nc);
    return uct;
  }

  @Override
  public int expandChild(Node currNode, ArrayList<Integer> eligibleChoices) {
    int idx = rng.nextInt(eligibleChoices.size());
    return eligibleChoices.get(idx);
  }

}
