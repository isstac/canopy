package edu.cmu.sv.isstac.sampling.reinforcement;

import edu.cmu.sv.isstac.sampling.search.SamplingException;

/**
 * @author Kasper Luckow
 *
 */
class RLAnalysisException extends SamplingException {

  private static final long serialVersionUID = 1L;

  public RLAnalysisException(String msg) {
    super(msg);
  }

  public RLAnalysisException(Throwable s) {
    super(s);
  }

  public RLAnalysisException(String msg, Throwable s) {
    super(msg, s);
  }
}
