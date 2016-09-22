package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.search.SamplingException;

/**
 * @author Kasper Luckow
 *
 */
class BatchProcessorException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public BatchProcessorException(String msg) {
    super(msg);
  }

  public BatchProcessorException(Throwable s) {
    super(s);
  }

  public BatchProcessorException(String msg, Throwable s) {
    super(msg, s);
  }
}
