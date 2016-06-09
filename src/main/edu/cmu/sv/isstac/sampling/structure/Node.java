package edu.cmu.sv.isstac.sampling.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.sv.isstac.sampling.reward.Reward;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public abstract class Node {

  private final Node parent;
  private final int choice;
  private final int totalChoicesNum;
  private int visitedNum = 0;
  
  private final Map<Integer, Node> children = new HashMap<>();
  
  private final Reward reward = new Reward();
  
  public Node(Node parent, ChoiceGenerator<?> cg, int choice) {
    this.parent = parent;
    this.choice = choice;
    this.totalChoicesNum = (cg != null) ? cg.getTotalNumberOfChoices() : 0;
  }
  
  public Node getParent() {
    return parent;
  }
  
  public Collection<Node> getChildren() {
    return children.values();
  }
  
  public boolean hasChildForChoice(int choice) {
    return this.children.containsKey(choice);
  }
  
  public void addChild(Node child) {
    this.children.put(child.getChoice(), child);
  }
  
  public Node getChild(int choice) {
    return this.children.get(choice);
  }

  public int getChoice() {
    return choice;
  }

  public int getTotalChoicesNum() {
    return totalChoicesNum;
  }
  
  public Reward getReward() {
    return this.reward;
  }
  
  public int getVisitedNum() {
    return this.visitedNum;
  }
  
  public void incVisitedNum() {
    this.visitedNum++;
  }
}
