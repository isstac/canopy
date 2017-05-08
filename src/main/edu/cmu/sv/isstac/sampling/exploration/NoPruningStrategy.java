package edu.cmu.sv.isstac.sampling.exploration;

import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.Path;

/**
 * @author Kasper Luckow
 * Will never prune anything
 */
public class NoPruningStrategy implements PruningStrategy {

  @Override
  public boolean isFullyPruned() {
    return false;
  }

  @Override
  public void reset() {

  }

  @Override
  public void performPruning(Path path, ChoiceGenerator<?> cg) {

  }
}
