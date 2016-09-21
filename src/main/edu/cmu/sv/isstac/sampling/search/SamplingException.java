package edu.cmu.sv.isstac.sampling.search;

/**
 * @author Kasper Luckow
 *
 */
public class SamplingException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;
  
  public SamplingException(String msg) {
    super(msg);
  }

  public SamplingException(Throwable s) {
    super(s);
  }
  
  public SamplingException(String msg, Throwable s) {
    super(msg, s);
  }
}
