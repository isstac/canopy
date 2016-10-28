package edu.cmu.sv.isstac.sampling.reinforcement;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.structure.DefaultNodeFactory;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeCreationException;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import edu.cmu.sv.isstac.sampling.structure.NondeterministicNode;
import edu.cmu.sv.isstac.sampling.structure.PCNode;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;

import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isNondeterministicChoice;
import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isPCNode;

/**
 * @author Kasper Luckow
 */
public class RLNodeFactory implements NodeFactory<RLNode> {
  private static final Logger LOGGER = JPFLogger.getLogger(RLNodeFactory.class.getName());

  @Override
  public RLNode create(RLNode parent, ChoiceGenerator<?> currentCG, int choice) throws NodeCreationException {
    RLNode newNode;
    //Currentcg is null for final nodes
    if(isSupportedChoiceGenerator(currentCG) || currentCG == null) {
      newNode = new RLNode(parent, currentCG, choice);
    } else {
      String msg = "Cannot create node for choicegenerators of type " + currentCG.getClass().getName();
      LOGGER.severe(msg);
      throw new IllegalStateException(msg);
    }
    if (parent != null)
      parent.addChild(newNode);
    return newNode;
  }

  @Override
  public boolean isSupportedChoiceGenerator(ChoiceGenerator<?> cg) {
    return isPCNode(cg) || isNondeterministicChoice(cg);
  }
}
