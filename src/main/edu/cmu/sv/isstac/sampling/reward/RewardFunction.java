package edu.cmu.sv.isstac.sampling.reward;

import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public interface RewardFunction {
  public long computeReward(VM vm);
}
