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

package edu.cmu.sv.isstac.sampling.analysis;

import java.awt.*;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult.ResultContainer;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 *
 */
public class LiveAnalysisStatistics extends AbstractAnalysisProcessor {
  public static final Logger logger = JPFLogger.getLogger(LiveAnalysisStatistics.class.getName());

  private final int BUFFER_SIZE = 20;
  private LiveTrackerChart chart;

  public LiveAnalysisStatistics(long budget) {
    chart = new LiveTrackerChart(BUFFER_SIZE, budget);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    chart.setPreferredSize(new Dimension(screenSize.width, 768));
    chart.pack();
    chart.setVisible(true);
  }

  public LiveAnalysisStatistics() {
    this(-1);
  }

  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward,
                         long pathVolume, ResultContainer currentBestResult,
                         boolean hasBeenExplored) {
    chart.update(samples, propagatedReward, pathVolume, hasBeenExplored);
  }
  
  @Override  
  public void analysisDone(SamplingResult result) {
    chart.flush();
  }

  @Override
  public void analysisStarted(Search search) { }

}
