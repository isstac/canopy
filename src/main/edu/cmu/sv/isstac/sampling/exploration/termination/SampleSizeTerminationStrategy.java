package edu.cmu.sv.isstac.sampling.exploration.termination;

import edu.cmu.sv.isstac.sampling.SamplingResult;
import edu.cmu.sv.isstac.sampling.mcts.TerminationStrategy;
import edu.cmu.sv.isstac.sampling.structure.Node;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class SampleSizeTerminationStrategy implements TerminationStrategy {

  private final int maxSamples;
  private int samples = 0;
  
  public SampleSizeTerminationStrategy(int maxSamples) {
    this.maxSamples = maxSamples;
  }
  
  @Override
  public boolean terminate(VM vm, Node root, SamplingResult currentResult) {
    return ++samples >= maxSamples;
  }
}
