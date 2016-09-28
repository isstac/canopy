package edu.cmu.sv.isstac.sampling.structure;

/**
 * @author Kasper Luckow
 *
 */
public class NodeCreationException extends Exception {

  private static final long serialVersionUID = 1L;

  public NodeCreationException(String msg) {
    super(msg);
  }

  public NodeCreationException(Throwable s) {
    super(s);
  }

  public NodeCreationException(String msg, Throwable s) {
    super(msg, s);
  }
}
