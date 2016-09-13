package edu.cmu.sv.isstac.sampling.analysis;

import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 *
 */
public interface AnalysisEventObserver {
  public void sampleDone(Search searchState, long samples, long propagatedReward, long pathVolume,
                         SamplingResult.ResultContainer currentBestResult);

  public void analysisDone(SamplingResult result);
}
