package edu.cmu.sv.isstac.sampling;

import edu.cmu.sv.isstac.sampling.SamplingResult.ResultContainer;

/**
 * @author Kasper Luckow
 *
 */
public interface AnalysisEventObserver {
  public void sampleDone(long samples, long reward, ResultContainer currentBestResult);
  public void analysisDone(SamplingResult result);
}
