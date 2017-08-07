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

package edu.cmu.sv.isstac.canopy.sidechannel;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.canopy.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.canopy.analysis.SamplingResult;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class ChannelCapacityListener implements AnalysisEventObserver {

  public static Logger logger = JPFLogger.getLogger(ChannelCapacityListener.class.getName());

  private final Set<Long> observables = new HashSet<>();

  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward, long pathVolume,
                         SamplingResult.ResultContainer currentBestResult,
                         boolean hasBeenExplored) {
    observables.add(propagatedReward);
  }

  @Override
  public void analysisDone(SamplingResult result) {
    logger.info("Timing channel capacity is " + getChannelCapacity() + " bits");
  }

  @Override
  public void analysisStarted(Search search) {

  }

  public double getChannelCapacity() {
    return Math.log(observables.size()) / Math.log(2);
  }
}
