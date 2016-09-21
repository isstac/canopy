package edu.cmu.sv.isstac.sampling.termination;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public interface TerminationStrategy {
  public boolean terminateBeforeSample(VM vm);
  public boolean terminateAfterSample(VM vm, SamplingResult currentResult);
}
