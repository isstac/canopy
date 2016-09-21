package edu.cmu.sv.isstac.sampling.termination;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class AllPathsTerminationStrategy implements TerminationStrategy {

  private final PruningChoicesStrategy pruner;
  private final Path ROOT = new Path(); //empty path
  
  public AllPathsTerminationStrategy(PruningChoicesStrategy pruner) {
    this.pruner = pruner;
  }

  @Override
  public boolean terminateBeforeSample(VM vm) {
    return this.pruner.isPruned(ROOT);
  }

  @Override
  public boolean terminateAfterSample(VM vm, SamplingResult currentResult) {
    return false;
  }
}
