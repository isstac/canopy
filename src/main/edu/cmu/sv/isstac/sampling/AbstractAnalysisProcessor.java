package edu.cmu.sv.isstac.sampling;

import edu.cmu.sv.isstac.sampling.SamplingResult.ResultContainer;

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
  };
  
  public void sampleDone(long samples, long reward, ResultContainer currentBestResult) { }
  
  public void analysisDone(SamplingResult result) { }
  
}
