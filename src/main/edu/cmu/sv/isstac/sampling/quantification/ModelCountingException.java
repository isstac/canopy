package edu.cmu.sv.isstac.sampling.quantification;

/**
 * @author Kasper Luckow
 *
 */
public class ModelCountingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ModelCountingException(String msg) {
    super(msg);
  }

  public ModelCountingException(Throwable s) {
    super(s);
  }

  public ModelCountingException(String msg, Throwable s) {
    super(msg, s);
  }
}
