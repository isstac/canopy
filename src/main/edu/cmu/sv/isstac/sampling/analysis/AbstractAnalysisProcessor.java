package edu.cmu.sv.isstac.sampling.analysis;

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
  
  public void sampleDone(long samples, long propagatedReward, long pathVolume, SamplingResult.ResultContainer
      currentBestResult) { }
  
  public void analysisDone(SamplingResult result) { }
  
}
