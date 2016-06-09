package edu.cmu.sv.isstac.sampling.montecarlo;

import edu.cmu.sv.isstac.sampling.SamplingException;

/**
 * @author Kasper Luckow
 *
 */
public class MonteCarloAnalysisException extends SamplingException {
  private static final long serialVersionUID = 1L;
  
  public MonteCarloAnalysisException(String msg) {
    super(msg);
  }

  public MonteCarloAnalysisException(Throwable s) {
    super(s);
  }
  
  public MonteCarloAnalysisException(String msg, Throwable s) {
    super(msg, s);
  }
}
