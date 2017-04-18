/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.cmu.sv.isstac.sampling.distributed;

import java.util.concurrent.TimeUnit;

/**
 * @author Kasper Luckow
 */
public class WorkerStatistics {
  private final TimeUnit TIMEUNIT;

  //Statistics we keep track of
  private final long bestRewardSampleNum;
  private final long bestRewardTime;
  private final long bestReward;
  private final long totalSampleNum;

  private final long uniqueSampleNum;
  private final double avgThroughput;
  private final long totalAnalysisTime;

  private final double rewardVar;
  private final double rewardStdDev;
  private final double rewardMean;
  private final double rewardMin;


  private final int numberOfBestRewards = 0;

  //POJO for worker statistics
  public WorkerStatistics(TimeUnit timeunit,
                          long bestRewardSampleNum,
                          long bestRewardTime,
                          long bestReward,
                          long totalSampleNum,
                          long uniqueSampleNum,
                          double avgThroughput,
                          long totalAnalysisTime,
                          double rewardVar,
                          double rewardStdDev,
                          double rewardMean,
                          double rewardMin) {
    TIMEUNIT = timeunit;
    this.bestRewardSampleNum = bestRewardSampleNum;
    this.bestRewardTime = bestRewardTime;
    this.bestReward = bestReward;

    this.totalSampleNum = totalSampleNum;
    this.uniqueSampleNum = uniqueSampleNum;
    this.avgThroughput = avgThroughput;
    this.totalAnalysisTime = totalAnalysisTime;
    this.rewardVar = rewardVar;
    this.rewardStdDev = rewardStdDev;
    this.rewardMean = rewardMean;
    this.rewardMin = rewardMin;
  }

  public double getRewardVariance() {
    return rewardVar;
  }

  public double getRewardStandardDeviation() {
    return rewardStdDev;
  }

  public double getRewardMean() {
    return rewardMean;
  }

  public double getMinReward() {
    return rewardMin;
  }

  public int getNumberOfBestRewards() {
    return numberOfBestRewards;
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

  public long getUniqueSampleNum() {
    return uniqueSampleNum;
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
