package edu.cmu.sv.isstac.sampling.structure;

import java.util.logging.Logger;

import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import static edu.cmu.sv.isstac.sampling.structure.CGClassification.*;

/**
 * @author Kasper Luckow
 *
 */
public class DefaultNodeFactory implements NodeFactory<Node> {
  private static final Logger logger = JPFLogger.getLogger(DefaultNodeFactory.class.getName());
  
  @Override
  public Node create(Node parent, ChoiceGenerator<?> currentCG, int choice) {
    Node newNode = null;
    if(currentCG == null)
      newNode = new FinalNode(parent, choice);
    else if(isPCNode(currentCG))
      newNode = new PCNode(parent, currentCG, choice);
    else if(isNondeterministicChoice(currentCG))
      newNode = new NondeterministicNode(parent, currentCG, choice);
    else {
      String msg = "Cannot create node for choicegenerators of type " + currentCG.getClass().getName();
      logger.severe(msg);
      throw new IllegalStateException(msg);
    }
    if(parent != null)
      parent.addChild(newNode);
    return newNode;
  }
  
  @Override
  public boolean isSupportedChoiceGenerator(ChoiceGenerator<?> cg) {
    return isPCNode(cg) || isNondeterministicChoice(cg);
  }
}
