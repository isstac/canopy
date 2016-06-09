package edu.cmu.sv.isstac.sampling.exploration.termination;

import edu.cmu.sv.isstac.sampling.SamplingResult;
import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import edu.cmu.sv.isstac.sampling.mcts.TerminationStrategy;
import edu.cmu.sv.isstac.sampling.structure.Node;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class AllPathsTerminationStrategy implements TerminationStrategy {

  private final PruningChoicesStrategy pruner;
  
  public AllPathsTerminationStrategy(PruningChoicesStrategy pruner) {
    this.pruner = pruner;
  }
  
  @Override
  public boolean terminate(VM vm, Node root, SamplingResult currentResult) {
    return this.pruner.isPruned(root);
  }

}
