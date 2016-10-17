package edu.cmu.sv.isstac.sampling.reward;

import edu.cmu.sv.isstac.sampling.search.SamplingException;

/**
 * @author Kasper Luckow
 *
 */
class RewardComputationException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  
  public RewardComputationException(String msg) {
    super(msg);
  }

  public RewardComputationException(Throwable s) {
    super(s);
  }
  
  public RewardComputationException(String msg, Throwable s) {
    super(msg, s);
  }
}
