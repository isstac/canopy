package edu.cmu.sv.isstac.sampling.analysis;

import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 *
 */
public abstract class AbstractAnalysisProcessor implements AnalysisEventObserver {
  public static AbstractAnalysisProcessor DEFAULT = new AbstractAnalysisProcessor() {
    @Override
    public void analysisDone(SamplingResult result) {
      System.out.println("Made " + result.getNumberOfSamples() + " samples before terminating");
      System.out.println("Max rewards observed based on MCTS policies: ");
      System.out.println(result.toString());
    }

    @Override
    public void analysisStarted(Search search) {
      System.out.println("Analysis started");
    }
  };

  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward, long pathVolume, SamplingResult.ResultContainer
      currentBestResult) { }

  @Override
  public void analysisDone(SamplingResult result) { }

}
