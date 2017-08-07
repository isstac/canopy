/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
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

package edu.cmu.sv.isstac.canopy.analysis;

import com.google.common.base.MoreObjects;

import edu.cmu.sv.isstac.canopy.exploration.Path;
import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 *
 */
public class SamplingResult {

  // It would be much better with an immutable
  // result state here, but it clutters a bit
  // how results are updated in MCTSStrategy
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
