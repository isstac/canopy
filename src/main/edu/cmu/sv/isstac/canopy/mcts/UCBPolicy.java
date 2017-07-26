/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.cmu.sv.isstac.canopy.mcts;

import java.util.ArrayList;
import java.util.Random;

import edu.cmu.sv.isstac.canopy.structure.Node;

/**
 * @author Kasper Luckow
 *
 */
class UCBPolicy implements SelectionPolicy {

  private final Random rng;
  private final double biasParameter;
  
  public UCBPolicy(long seed, double biasParameter) {
    this.rng = new Random(seed);
    this.biasParameter = biasParameter;
  }
  
  @Override
  public MCTSNode selectBestChild(Node currNode, ArrayList<Integer> eligibleChoices) {
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
    
    return (MCTSNode)bestChild;
  }
  
  private double computeUCT(Node parent, Node child) {
    
    // NOTE: the best child is determined in terms of its reward for successful termination
    long q = child.getReward().getSucc();

    long nc = child.getVisitedNum();
    long np = parent.getVisitedNum();

    double exploitation = ((double) q / nc);
    double exploration = this.biasParameter * Math.sqrt(2 * Math.log(np) / nc);

    double uct =  exploitation + exploration;

    return uct;
  }

  @Override
  public int expandChild(Node currNode, ArrayList<Integer> eligibleChoices) {
    int idx = rng.nextInt(eligibleChoices.size());
    return eligibleChoices.get(idx);
  }

}
