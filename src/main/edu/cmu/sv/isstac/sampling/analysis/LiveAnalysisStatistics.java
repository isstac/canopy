package edu.cmu.sv.isstac.sampling.analysis;

import java.awt.Dimension;
import java.awt.Toolkit;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult.ResultContainer;

/**
 * @author Kasper Luckow
 *
 */
public class LiveAnalysisStatistics extends AbstractAnalysisProcessor {
  private final int BUFFER_SIZE = 20;
  
  private LiveTrackerChart chart;
  
  public LiveAnalysisStatistics() {
    chart = new LiveTrackerChart(BUFFER_SIZE);
    
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    chart.setPreferredSize(new java.awt.Dimension(screenSize.width, 768));
    chart.pack();    
    chart.setVisible(true);
  }
  
  @Override
  public void sampleDone(long samples, long propagatedReward, long pathVolume, ResultContainer currentBestResult) {
    chart.update(samples, propagatedReward);
  }
  
  @Override  
  public void analysisDone(SamplingResult result) { }

}
