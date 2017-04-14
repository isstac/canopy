/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.sv.isstac.sampling.sidechannel;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.analysis.AbstractAnalysisProcessor;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult.ResultContainer;
import edu.cmu.sv.isstac.sampling.termination.GenericLiveChart;
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
                         long pathVolume, ResultContainer currentBestResult,
                         boolean hasBeenExplored) {
    chart.update(samples, listener.getChannelCapacity());
  }
  
  @Override  
  public void analysisDone(SamplingResult result) {

  }

  @Override
  public void analysisStarted(Search search) { }

}
