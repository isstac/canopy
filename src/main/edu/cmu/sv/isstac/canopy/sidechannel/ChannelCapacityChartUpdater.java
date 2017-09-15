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

import java.util.logging.Logger;

import edu.cmu.sv.isstac.canopy.analysis.AbstractAnalysisProcessor;
import edu.cmu.sv.isstac.canopy.analysis.GenericLiveChart;
import edu.cmu.sv.isstac.canopy.analysis.SamplingResult;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 *
 */
public class ChannelCapacityChartUpdater extends AbstractAnalysisProcessor {

  public static final Logger logger = JPFLogger.getLogger(ChannelCapacityChartUpdater.class.getName());
  private GenericLiveChart chart;
  private final ChannelCapacityListener listener;

  public ChannelCapacityChartUpdater(GenericLiveChart chart, ChannelCapacityListener listener) {
    this.chart = chart;
    this.listener = listener;
  }

  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward,
                         long pathVolume, SamplingResult.ResultContainer currentBestResult,
                         boolean hasBeenExplored) {
    chart.update(samples, listener.getChannelCapacity());
  }
  
  @Override  
  public void analysisDone(SamplingResult result) {

  }

  @Override
  public void analysisStarted(Search search) { }

}
