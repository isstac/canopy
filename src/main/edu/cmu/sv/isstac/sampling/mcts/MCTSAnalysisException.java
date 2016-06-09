package edu.cmu.sv.isstac.sampling.mcts;

import edu.cmu.sv.isstac.sampling.SamplingException;

/**
 * @author Kasper Luckow
 *
 */
class MCTSAnalysisException extends SamplingException {

  private static final long serialVersionUID = 1L;
  
  public MCTSAnalysisException(String msg) {
    super(msg);
  }

  public MCTSAnalysisException(Throwable s) {
    super(s);
  }
  
  public MCTSAnalysisException(String msg, Throwable s) {
    super(msg, s);
  }
}
