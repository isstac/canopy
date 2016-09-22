package edu.cmu.sv.isstac.sampling.termination;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class NeverTerminateStrategy implements TerminationStrategy {
  
  public NeverTerminateStrategy() {  }

  @Override
  public boolean terminate(VM vm, SamplingResult currentResult) {
    return false;
  }
}
