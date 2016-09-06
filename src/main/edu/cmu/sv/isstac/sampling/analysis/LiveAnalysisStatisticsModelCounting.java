package edu.cmu.sv.isstac.sampling.analysis;

import java.awt.*;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult.ResultContainer;

/**
 * @author Kasper Luckow
 *
 */
public class LiveAnalysisStatisticsModelCounting extends AbstractAnalysisProcessor {
  private final int BUFFER_SIZE = 20;

  private LiveTrackerChartModelCounting chart;

  public LiveAnalysisStatisticsModelCounting() {
    chart = new LiveTrackerChartModelCounting(BUFFER_SIZE);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    chart.setPreferredSize(new Dimension(screenSize.width, 768));
    chart.pack();    
    chart.setVisible(true);
  }
  
  @Override
  public void sampleDone(long samples, long propagatedReward, long pathVolume, ResultContainer currentBestResult) {
    chart.update(samples, propagatedReward, pathVolume);
  }
  
  @Override  
  public void analysisDone(SamplingResult result) { }

}
