package edu.cmu.sv.isstac.sampling.exploration;

import gov.nasa.jpf.vm.*;

/**
 * @author Kasper Luckow
 */
public interface PruningStrategy {
  boolean isFullyPruned();
  void reset();
  void performPruning(gov.nasa.jpf.vm.Path path, ChoiceGenerator<?> cg);
}
