package edu.cmu.sv.isstac.sampling.analysis;

/**
 * @author Kasper Luckow
 *
 */
public interface AnalysisEventObserver {
  public void sampleDone(long samples, long propagatedReward, long pathVolume, SamplingResult.ResultContainer
      currentBestResult);
  public void analysisDone(SamplingResult result);
}
