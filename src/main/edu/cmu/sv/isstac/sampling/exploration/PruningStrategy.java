package edu.cmu.sv.isstac.sampling.exploration;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 */
public interface PruningStrategy {
  boolean isPruned(Path p);
  boolean isFullyPruned();
  void performPruning(ChoiceGenerator<?> cg);
}
