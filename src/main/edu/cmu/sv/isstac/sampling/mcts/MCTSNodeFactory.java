package edu.cmu.sv.isstac.sampling.mcts;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.structure.FinalNode;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import edu.cmu.sv.isstac.sampling.structure.NondeterministicNode;
import edu.cmu.sv.isstac.sampling.structure.PCNode;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;

import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isNondeterministicChoice;
import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isPCNode;

/**
 * @author Kasper Luckow
 *
 */
public class MCTSNodeFactory implements NodeFactory<MCTSNode> {
  private static final Logger logger = JPFLogger.getLogger(MCTSNodeFactory.class.getName());
  
  @Override
  public MCTSNode create(MCTSNode parent, ChoiceGenerator<?> currentCG, int choice) {
    //If there is a shadow node already created, we return it here
    if(parent != null && parent.hasChildForChoice(choice)) {
      MCTSNode nxt = (MCTSNode)parent.getChild(choice);
      return nxt;
    }

    MCTSNode newNode = null;
    if(currentCG == null)
      newNode = new MCTSNode(parent, null, choice);
    else if(isSupportedChoiceGenerator(currentCG))
      newNode = new MCTSNode(parent, currentCG, choice);
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
