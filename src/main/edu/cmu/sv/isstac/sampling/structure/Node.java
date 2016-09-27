package edu.cmu.sv.isstac.sampling.structure;

import java.util.Collection;

import edu.cmu.sv.isstac.sampling.reward.Reward;
import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 */
public interface Node {
  Node getParent();

  Collection<Node> getChildren();

  boolean hasChildForChoice(int choice);

  void addChild(Node child);

  Node getChild(int choice);

  int getChoice();

  int getTotalChoicesNum();

  Reward getReward();

  long getVisitedNum();

  void incVisitedNum(long visitedNum);

  PathCondition getPathCondition();
}
