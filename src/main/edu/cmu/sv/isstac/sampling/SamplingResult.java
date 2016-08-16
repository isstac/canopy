package edu.cmu.sv.isstac.sampling;

import com.google.common.base.MoreObjects;

import edu.cmu.sv.isstac.sampling.exploration.Path;
import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 *
 */
public class SamplingResult {
  
  // It would be much better with an immutable
  // result state here, but it clutters a bit
  // how reults are updated in MCTSListener
  public static class ResultContainer {
    private long reward = Long.MIN_VALUE;
    private long sampleNumber = Long.MIN_VALUE;
    private Path path = null;
    private PathCondition pc = null;
    
    public ResultContainer() {
      
    }
    
    private ResultContainer(long reward, long sampleNumber,
        Path path, PathCondition pc) {
      this.reward = reward;
      this.sampleNumber = sampleNumber;
      this.path = path;
      this.pc = pc;
    }
    
    public void setReward(long reward) {
      this.reward = reward;
    }
    
    public void setSampleNumber(long sampleNum) {
      this.sampleNumber = sampleNum;
    }
    
    public void setPath(Path path) {
      this.path = path;
    }
    
    public void setPathCondition(PathCondition pc) {
      this.pc = pc;
    }
    
    public long getReward() {
      return this.reward;
    }
    
    public long getSampleNumber() {
      return this.sampleNumber;
    }
    
    public Path getPath() {
      return this.path;
    }
    
    public PathCondition getPathCondition() {
      return this.pc;
    }
    
    public boolean isSet() {
      return this.reward >= 0;
    }
    
    public ResultContainer copy() {
      return new ResultContainer(this.reward, this.sampleNumber, 
          this.path.copy(), this.pc.make_copy());
    }
    
    @Override
    public String toString() {
      return isSet() ? MoreObjects.toStringHelper(this).
          add("reward", reward).
          add("samplesNumber", sampleNumber).
          add("path", path.toString()).
          add("pc", (pc != null) ? pc.toString() : "").
          toString() : "";
    }
  }
  
  private ResultContainer maxSuccState = new ResultContainer();
  private ResultContainer maxFailState = new ResultContainer();
  private ResultContainer maxGreyState = new ResultContainer();
  
  private long numberOfSamples = 0;
  
  public SamplingResult() {
    
  }
  
  private SamplingResult(SamplingResult other) {
    this.maxFailState = other.maxFailState.copy();
    this.maxSuccState = other.maxSuccState.copy();
    this.maxGreyState = other.maxGreyState.copy();
  }
  
  public ResultContainer getMaxSuccResult() {
    return this.maxSuccState;
  }
  
  public ResultContainer getMaxFailResult() {
    return this.maxFailState;
  }
  
  public ResultContainer getMaxGreyResult() {
    return this.maxGreyState;
  }
  
  public void incNumberOfSamples() {
    this.numberOfSamples++;
  }
  
  public long getNumberOfSamples() {
    return this.numberOfSamples;
  }
  
  // Same as copy ctor
  public SamplingResult copy() {
    return new SamplingResult(this);
  }
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).
      add("Max succ state", maxSuccState.toString()).
      add("Max fail state", maxFailState.toString()).
      add("Max grey state", maxGreyState.toString()).toString();
  }
}
