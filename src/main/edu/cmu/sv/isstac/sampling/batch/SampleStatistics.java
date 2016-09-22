package edu.cmu.sv.isstac.sampling.batch;

import com.google.common.base.Stopwatch;

import java.util.concurrent.TimeUnit;

import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 */
public class SampleStatistics implements AnalysisEventObserver {

  private static final TimeUnit TIMEUNIT = TimeUnit.SECONDS;

  private Stopwatch stopwatch;

  //Statistics we keep track of
  private long bestRewardSampleNum = 0;
  private long bestRewardTime = 0;
  private long bestReward = -1;
  private long totalSampleNum = 0;
  private double avgThroughput = 0.0;
  private long totalAnalysisTime = 0;

  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward, long pathVolume, SamplingResult.ResultContainer currentBestResult) {
    if(propagatedReward > bestReward) {
      bestReward = propagatedReward;
      bestRewardSampleNum = samples;
      bestRewardTime = stopwatch.elapsed(TIMEUNIT);
    }
  }

  @Override
  public void analysisDone(SamplingResult result) {
    long totalAnalysisTimeMS = stopwatch.elapsed(TimeUnit.MILLISECONDS);
    totalAnalysisTime = TIMEUNIT.convert(totalAnalysisTimeMS, TimeUnit.MILLISECONDS);
    stopwatch.stop();
    totalSampleNum = result.getNumberOfSamples();
    if(totalAnalysisTimeMS > 0) {
      long msToS = TIMEUNIT.toMillis(1);
      avgThroughput = (totalSampleNum / (double)totalAnalysisTimeMS) * msToS;
    }

  }

  @Override
  public void analysisStarted(Search search) {
    this.stopwatch = Stopwatch.createStarted();
  }

  public long getBestRewardSampleNum() {
    return bestRewardSampleNum;
  }

  public long getBestRewardTime() {
    return bestRewardTime;
  }

  public long getBestReward() {
    return bestReward;
  }

  public long getTotalSampleNum() {
    return totalSampleNum;
  }

  public double getAvgThroughput() {
    return avgThroughput;
  }

  public long getTotalAnalysisTime() {
    return totalAnalysisTime;
  }

  public TimeUnit getTimeUnit() {
    return TIMEUNIT;
  }
}
