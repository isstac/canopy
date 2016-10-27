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

  public LiveAnalysisStatistics() {
    chart = new LiveTrackerChart(BUFFER_SIZE);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    chart.setPreferredSize(new Dimension(screenSize.width, 768));
    chart.pack();    
    chart.setVisible(true);
  }
  
  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward,
                         long pathVolume, ResultContainer currentBestResult,
                         boolean hasBeenExplored) {
    // if(!hasBeenExplored) {
       chart.update(samples, propagatedReward, pathVolume);
//     } else {
//       logger.warning("Live tracker chart will not show already explored path---is that what we " +
//           "want?");
//     }
  }
  
  @Override  
  public void analysisDone(SamplingResult result) { }

  @Override
  public void analysisStarted(Search search) { }

}
