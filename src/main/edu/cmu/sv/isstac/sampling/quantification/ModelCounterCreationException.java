package edu.cmu.sv.isstac.sampling.quantification;

/**
 * @author Kasper Luckow
 *
 */
public class ModelCounterCreationException extends Exception {

  private static final long serialVersionUID = 1L;

  public ModelCounterCreationException(String msg) {
    super(msg);
  }

  public ModelCounterCreationException(Throwable s) {
    super(s);
  }

  public ModelCounterCreationException(String msg, Throwable s) {
    super(msg, s);
  }
}
