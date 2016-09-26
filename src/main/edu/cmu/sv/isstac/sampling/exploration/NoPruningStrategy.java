package edu.cmu.sv.isstac.sampling.exploration;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 * Will never prune anything
 */
public class NoPruningStrategy implements PruningStrategy {

  @Override
  public boolean isPruned(Path p) {
    return false;
  }

  @Override
  public boolean isFullyPruned() {
    return false;
  }

  @Override
  public void reset() {

  }

  @Override
  public void performPruning(ChoiceGenerator<?> cg) {
    //Do nothing
  }
}
