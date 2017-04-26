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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.AnalysisException;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.AnalysisFactory;
import edu.cmu.sv.isstac.sampling.analysis.SampleStatistics;
import edu.cmu.sv.isstac.sampling.exploration.Path;
import edu.cmu.sv.isstac.sampling.search.SamplingAnalysisListener;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class SamplingWorker {

  public static Logger logger = JPFLogger.getLogger(SamplingWorker.class.getName());
  private SamplingAnalysis samplingAnalysis;
  private SampleStatistics statistics;

  public WorkerResult runAnalysis(Path frontierNode, Config config) throws AnalysisCreationException {
    //disable livetracker chart
    config.setProperty(Options.SHOW_LIVE_STATISTICS, "false");

    //Enforce collection of statistics (needed for getStatus to be useful)
    config.setProperty(Options.SHOW_STATISTICS, "true");
    
    AnalysisFactory af = getAnalysisFactory(config);

    SamplingAnalysis.Builder analysisBuilder =
        new SamplingAnalysis.Builder();

    analysisBuilder.setFrontierNode(frontierNode);

    samplingAnalysis = analysisBuilder.build(config,
        af.createAnalysis(config),
        af.getJPFFactory());

    //This is not the most clean way of obtaining the statistics listener

    this.statistics = this.getStatisticsObj(this.samplingAnalysis);

    samplingAnalysis.run();

    return new WorkerResult(this.getStatus());
  }

  public WorkerStatistics getStatus() {
    if(this.statistics == null) {
      return new WorkerStatistics(TimeUnit.SECONDS, 0, 0,0,0,0,0,0,0,0,0,0);
    }

    WorkerStatistics workerStatistics = new WorkerStatistics(
        this.statistics.getTimeUnit(),
        this.statistics.getBestRewardSampleNum(),
        this.statistics.getBestRewardTime(),
        this.statistics.getBestReward(),
        this.statistics.getTotalSampleNum(),
        this.statistics.getUniqueSampleNum(),
        this.statistics.getAvgThroughput(),
        this.statistics.getTotalAnalysisTime(),
        this.statistics.getRewardVariance(),
        this.statistics.getRewardStandardDeviation(),
        this.statistics.getRewardMean(),
        this.statistics.getMinReward());

    return workerStatistics;
  }

  //Ugly. Fix
  private SampleStatistics getStatisticsObj(SamplingAnalysis analysis) {
    SamplingAnalysisListener listener = samplingAnalysis.getJPF()
        .getListenerOfType(SamplingAnalysisListener.class);
    Collection<AnalysisEventObserver> observers = listener.getEventObservers();

    for(AnalysisEventObserver obs : observers) {
      if(obs instanceof SampleStatistics) {
        return (SampleStatistics) obs;
      }
    }
    return null;
  }

  private AnalysisFactory getAnalysisFactory(Config config) {
    switch(config.getString(Utils.ANALYSIS_TYPE)) {
      case "mcts":
        return AnalysisFactory.mctsFactory;
      case "mc":
        return AnalysisFactory.mcFactory;
      case "rl":
        return AnalysisFactory.rlFactory;
      case "exhaustive":
        return AnalysisFactory.exhaustiveFactory;
      default:
        throw new AnalysisException("Config " + Utils
            .ANALYSIS_TYPE + " must be one of: mcts, mc, rl, exhaustive");
    }
  }
}
