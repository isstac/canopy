package edu.cmu.sv.isstac.sampling.mcts;

import java.util.ArrayList;

import edu.cmu.sv.isstac.sampling.structure.Node;

/**
 * @author Kasper Luckow
 *
 */
public interface SelectionPolicy {
  
  public MCTSNode selectBestChild(Node currNode, ArrayList<Integer> eligibleChoices);
  public int expandChild(Node currNode, ArrayList<Integer> eligibleChoices);
}
