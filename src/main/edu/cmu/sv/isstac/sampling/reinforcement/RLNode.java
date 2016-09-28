package edu.cmu.sv.isstac.sampling.reinforcement;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.sv.isstac.sampling.reward.Reward;
import edu.cmu.sv.isstac.sampling.structure.Node;
import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 *
 * Wraps a node and gives it the ability to provide probabilities for choices
 */
public class RLNode implements Node {
  private final Node decoratee;

  private Map<Integer, Double> choice2prob = new HashMap<>();

  private final long subdomainSize;

  public RLNode(Node decoratee, long subdomainSize) {
    this.decoratee = decoratee;
    this.subdomainSize = subdomainSize;

    //Set uniform initial probability for all choices
    double prob = 1/(double)decoratee.getTotalChoicesNum();
    for(int choice = 0; choice < this.decoratee.getTotalChoicesNum(); choice++) {
      choice2prob.put(choice, prob);
    }
  }

  public long getSubdomainSize() {
    return this.subdomainSize;
  }

  public double getProbabilitySum(List<Integer> choices) {
    // God, streams are beautiful
    return choices.stream().mapToDouble(choice -> choice2prob.get(choice)).sum();
  }

  public double getChoiceProbability(int choice) {
    return choice2prob.get(choice);
  }

  public void setChoiceProbability(int choice, double probability) {
    choice2prob.put(choice, probability);
  }

  @Override
  public Node getParent() {
    return decoratee.getParent();
  }

  @Override
  public Collection<Node> getChildren() {
    return decoratee.getChildren();
  }

  @Override
  public boolean hasChildForChoice(int choice) {
    return decoratee.hasChildForChoice(choice);
  }

  @Override
  public void addChild(Node child) {
    this.decoratee.addChild(child);
  }

  @Override
  public Node getChild(int choice) {
    return decoratee.getChild(choice);
  }

  @Override
  public int getChoice() {
    return decoratee.getChoice();
  }

  @Override
  public int getTotalChoicesNum() {
    return decoratee.getTotalChoicesNum();
  }

  @Override
  public Reward getReward() {
    return decoratee.getReward();
  }

  @Override
  public long getVisitedNum() {
    return decoratee.getVisitedNum();
  }

  @Override
  public void incVisitedNum(long visitedNum) {
    decoratee.incVisitedNum(visitedNum);
  }

  @Override
  public PathCondition getPathCondition() {
    return decoratee.getPathCondition();
  }
}
