package edu.cmu.sv.isstac.sampling.termination;

import edu.cmu.sv.isstac.sampling.SamplingResult;
import edu.cmu.sv.isstac.sampling.structure.Node;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public interface TerminationStrategy {
  public boolean terminate(VM vm, SamplingResult currentResult);
}
