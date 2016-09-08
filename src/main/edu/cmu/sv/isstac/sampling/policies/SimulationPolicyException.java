package edu.cmu.sv.isstac.sampling.policies;

import edu.cmu.sv.isstac.sampling.SamplingException;

/**
 * @author Kasper Luckow
 *
 */
public class SimulationPolicyException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public SimulationPolicyException(String msg) {
    super(msg);
  }

  public SimulationPolicyException(Throwable s) {
    super(s);
  }

  public SimulationPolicyException(String msg, Throwable s) {
    super(msg, s);
  }
}
