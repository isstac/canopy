package edu.cmu.sv.isstac.sampling.reward;

import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class DepthRewardFunction implements RewardFunction {

  @Override
  public long computeReward(VM vm) {
    return vm.getSearch().getDepth();
  }
}
