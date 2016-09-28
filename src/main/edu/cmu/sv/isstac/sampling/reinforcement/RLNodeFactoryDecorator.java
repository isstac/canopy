package edu.cmu.sv.isstac.sampling.reinforcement;

import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeCreationException;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.utils.BigRational;

/**
 * @author Kasper Luckow
 * Decorates all nodes with RL capabilities
 */
public class RLNodeFactoryDecorator implements NodeFactory<RLNode> {

  private final NodeFactory<Node> decoratee;
  private final SPFModelCounter modelCounter;

  public RLNodeFactoryDecorator(NodeFactory<Node> decoratee, SPFModelCounter modelCounter) {
    this.decoratee = decoratee;
    this.modelCounter = modelCounter;
  }

  @Override
  public RLNode create(RLNode parent, ChoiceGenerator<?> currentCG, int choice)
      throws NodeCreationException {
    assert currentCG instanceof PCChoiceGenerator;

    PCChoiceGenerator pccg = (PCChoiceGenerator)currentCG;
    long subdomainSize;
    try {
      subdomainSize = this.modelCounter.countPointsOfPC(pccg.getCurrentPC()).longValue();
    } catch (AnalysisException e) {
      throw new NodeCreationException(e);
    }

    throw new RuntimeException("check if pc is correct here");
    //return new RLNode(this.decoratee.create(parent, currentCG, choice), subdomainSize);
  }

  @Override
  public boolean isSupportedChoiceGenerator(ChoiceGenerator<?> cg) {
    return false;
  }
}
