package edu.cmu.sv.isstac.sampling.termination;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult.ResultContainer;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class RewardBoundedTermination implements TerminationStrategy {

  public static enum EVENT {
    SUCC, FAIL,GREY;
  }
  
  private final EVENT targetEvent;
  private final long rewardBound;
  
  public RewardBoundedTermination(long rewardBound, EVENT eventType) {
    this.targetEvent = eventType;
    this.rewardBound = rewardBound;
  }

  @Override
  public boolean terminate(VM vm, SamplingResult currentResult) {
    ResultContainer res = null;
    switch(targetEvent) {
    case SUCC:
      res = currentResult.getMaxSuccResult();
      break;
    case FAIL:
      res = currentResult.getMaxFailResult();
      break;
    case GREY:
      res = currentResult.getMaxFailResult();
      break;
      default:
        throw new IllegalStateException("Did not recognize event type " + targetEvent);
    }
    return res.getReward() >= rewardBound;
  }
}
