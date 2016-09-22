package edu.cmu.sv.isstac.sampling.analysis;

import java.awt.Dimension;
import java.awt.Toolkit;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult.ResultContainer;
import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 * Either remove this class or make a factory that creates charts base don whether model counting
 * is used or not
 */
@Deprecated
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
  public void sampleDone(Search searchState, long samples, long propagatedReward, long pathVolume, ResultContainer currentBestResult) {
    chart.update(samples, propagatedReward);
  }
  
  @Override  
  public void analysisDone(SamplingResult result) { }

  @Override
  public void analysisStarted(Search search) {

  }

}
