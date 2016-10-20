package edu.cmu.sv.isstac.sampling.analysis;

import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 *
 */
public abstract class AbstractAnalysisProcessor implements AnalysisEventObserver {

  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward, long pathVolume, SamplingResult.ResultContainer
      currentBestResult, boolean hasBeenExplored) { }

  @Override
  public void analysisDone(SamplingResult result) { }

  @Override
  public void analysisStarted(Search search) { }
}
