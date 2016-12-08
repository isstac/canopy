package edu.cmu.sv.isstac.sampling.analysis;

/**
 * @author Kasper Luckow
 *
 */
class AnalysisException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public AnalysisException(String msg) {
    super(msg);
  }

  public AnalysisException(Throwable s) {
    super(s);
  }

  public AnalysisException(String msg, Throwable s) {
    super(msg, s);
  }
}
