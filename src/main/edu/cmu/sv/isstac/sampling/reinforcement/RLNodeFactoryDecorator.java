package edu.cmu.sv.isstac.sampling.reinforcement;

import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 * Decorates all nodes with RL capabilities
 */
public class RLNodeFactoryDecorator implements NodeFactory<RLNodeDecorator> {

  private final NodeFactory<Node> decoratee;

  public RLNodeFactoryDecorator(NodeFactory<Node> decoratee) {
    this.decoratee = decoratee;
  }

  @Override
  public RLNodeDecorator create(RLNodeDecorator parent, ChoiceGenerator<?> currentCG, int choice) {
    return new RLNodeDecorator(this.decoratee.create(parent, currentCG, choice));
  }

  @Override
  public boolean isSupportedChoiceGenerator(ChoiceGenerator<?> cg) {
    return false;
  }
}
