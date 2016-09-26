package edu.cmu.sv.isstac.sampling;

/**
 * @author Kasper Luckow
 *
 */
public class AnalysisCreationException extends Exception {

  private static final long serialVersionUID = 1L;

  public AnalysisCreationException(String msg) {
    super(msg);
  }

  public AnalysisCreationException(Throwable s) {
    super(s);
  }

  public AnalysisCreationException(String msg, Throwable s) {
    super(msg, s);
  }
}
