package edu.cmu.sv.isstac.sampling.analysis;

import java.io.PrintStream;
import java.util.logging.Logger;

import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class SampleStatisticsOutputter extends SampleStatistics {
  private final PrintStream output;

  public SampleStatisticsOutputter(PrintStream output) {
    this.output = output;
  }

  @Override
  public void analysisDone(SamplingResult result) {
    // We output the statistics here
    this.output.println(super.toString());
  }
}
