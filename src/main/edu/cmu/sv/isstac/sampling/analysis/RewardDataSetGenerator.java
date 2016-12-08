package edu.cmu.sv.isstac.sampling.analysis;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult.ResultContainer;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 *
 */
public class RewardDataSetGenerator extends AbstractAnalysisProcessor {
  public static final Logger logger = JPFLogger.getLogger(RewardDataSetGenerator.class.getName());

  private final List<Long> rewards = new LinkedList<>();
  private final FileWriter writer;

  public RewardDataSetGenerator(String outputPath) {
    try {
      this.writer = new FileWriter(outputPath);
    } catch (IOException e) {
      throw new AnalysisException(e);
    }
  }

  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward,
                         long pathVolume, ResultContainer currentBestResult,
                         boolean hasBeenExplored) {
    rewards.add(propagatedReward);
  }
  
  @Override  
  public void analysisDone(SamplingResult result) {
    BufferedWriter bw = new BufferedWriter(writer);
    try {
      bw.write("sample,reward\n");

      int sampleNum = 1;
      for(Long reward : rewards) {
        bw.write(sampleNum + "," + reward.longValue() + "\n");
        sampleNum++;
      }

      bw.flush();
      bw.close();
    } catch (IOException e) {
      throw new AnalysisException(e);
    }
  }

  @Override
  public void analysisStarted(Search search) { }

}
