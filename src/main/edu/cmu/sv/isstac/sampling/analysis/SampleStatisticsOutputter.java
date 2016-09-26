package edu.cmu.sv.isstac.sampling.analysis;

import java.io.OutputStream;
import java.io.PrintStream;

import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 */
public class SampleStatisticsOutputter extends SampleStatistics {
  private final SampleStatistics sampleStatistics;
  private final PrintStream output;

  public SampleStatisticsOutputter(SampleStatistics sampleStatistics, PrintStream output) {
    this.sampleStatistics = sampleStatistics;
    this.output = output;
  }

  @Override
  public void analysisDone(SamplingResult result) {
    this.sampleStatistics.analysisDone(result);

    // We output the statistics here
    this.output.println(sampleStatistics.toString());
  }

  @Override
  public void sampleDone(Search searchState, long samples,
                         long propagatedReward, long pathVolume,
                         SamplingResult.ResultContainer currentBestResult) {
    this.sampleStatistics.sampleDone(searchState, samples,
        propagatedReward, pathVolume, currentBestResult);
  }

  @Override
  public void analysisStarted(Search search) {
    this.sampleStatistics.analysisStarted(search);
  }
}
