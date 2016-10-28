package edu.cmu.sv.isstac.sampling.reinforcement;

import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeCreationException;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import modelcounting.analysis.exceptions.AnalysisException;
import modelcounting.utils.BigRational;

/**
 * @author Kasper Luckow
 * Decorates all RL nodes with subdomain (mc) computation.
 * TODO: Don't like the use of the type parameter here. Should be fixed after experiments
 */
public class RLNodeFactoryMCDecorator implements NodeFactory<RLNode> {

  private final RLNodeFactory decoratee = new RLNodeFactory();
  private final SPFModelCounter modelCounter;

  public RLNodeFactoryMCDecorator(SPFModelCounter modelCounter) {
    this.modelCounter = modelCounter;
  }

  @Override
  public RLNode create(RLNode parent, ChoiceGenerator<?> currentCG, int choice)
      throws NodeCreationException {
    if(currentCG != null) {
      PCChoiceGenerator pccg = currentCG.
          getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
      PathCondition pc;
      if (pccg != null) {
        pc = pccg.getCurrentPC();
      } else {
        pc = new PathCondition();
      }
      long subdomainSize;
      try {
        subdomainSize = this.modelCounter.countPointsOfPC(pc).longValue();
      } catch (AnalysisException e) {
        throw new NodeCreationException(e);
      }
      return new MCRLNode(parent, currentCG, choice, subdomainSize);
    } else {
      // This should only happen when the node we are creating is a final node
      // in which case the domain size does not matter anyway
      return new MCRLNode(parent, currentCG, choice, -1);
    }
  }

  @Override
  public boolean isSupportedChoiceGenerator(ChoiceGenerator<?> cg) {
    return this.decoratee.isSupportedChoiceGenerator(cg);
  }
}
