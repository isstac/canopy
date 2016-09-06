package edu.cmu.sv.isstac.sampling.analysis;

/**
 * @author Kasper Luckow
 */
public class SampleStatistics {

  private final long sampleNum;
  private final long reward;

  public SampleStatistics(long sampleNum, long reward) {
    this.sampleNum = sampleNum;
    this.reward = reward;
  }

  public long getSampleNum() {
    return sampleNum;
  }

  public long getReward() {
    return reward;
  }
}
