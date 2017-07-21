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

package edu.cmu.sv.isstac.sampling.complexity;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.analysis.AnalysisFactory;
import edu.cmu.sv.isstac.sampling.analysis.SampleStatistics;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class ComplexityAnalyzer {

  public static Logger logger = JPFLogger.getLogger(ComplexityAnalyzer.class.getName());

  private final AnalysisFactory af;
  private final int min;
  private final int max;
  private final int increment;
  private final boolean visualize;
  private final Config config;
  private ComplexityChart chart = null;
  private final Random seedGen;

  private final File outputFile;

  public ComplexityAnalyzer(AnalysisFactory af, int min, int max, int increment,
                            boolean visualize, Config config) {
    this.af = af;
    this.min = min;
    this.max = max;
    this.increment = increment;
    this.visualize = visualize;
    this.config = config;

    File outputDir = new File(config.getString(Utils.OUTPUT_DIR, Utils.DEFAULT_OUTPUT_DIR));
    if(!outputDir.exists()) {
      outputDir.mkdirs();
    } else if(!outputDir.isDirectory()) {
      throw new ComplexityAnalysisException("config " +
          Utils.OUTPUT_DIR + " must specify a directory");
    }
    this.outputFile = getOutputFile(config, outputDir);

    this.seedGen = new Random(Options.getSeed(config));

    if(visualize) {
      chart = new ComplexityChart();
      //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      chart.setPreferredSize(new Dimension(1024, 768));
      chart.pack();
      chart.setVisible(true);
    }
  }

  public void run() throws AnalysisCreationException {
    for(int inputSize = min; inputSize <= max; inputSize += increment) {
      SamplingAnalysis.Builder analysisBuilder =
          new SamplingAnalysis.Builder();

      //Update target args to new input size
      config.setProperty("target.args", Integer.toString(inputSize));

      //Set new seed
      long seed = seedGen.nextLong();
      config.setProperty(Options.RNG_SEED, Long.toString(seed));

      if(visualize) {
        analysisBuilder.addEventObserver(new ComplexityChartUpdater(chart, inputSize));
      }

      //Add the statistics reporter
      SampleStatistics statistics = new SampleStatistics();
      analysisBuilder.addEventObserver(statistics);

      SamplingAnalysis samplingAnalysis = analysisBuilder.build(config,
          af.createAnalysis(config),
          af.getJPFFactory());

      samplingAnalysis.run();

      writeResultToFile(statistics, seed, config.getTarget(),
          inputSize, config.getString(Utils.ANALYSIS_TYPE));
    }
  }

  private void writeResultToFile(SampleStatistics statistics, long seed, String target,
                                        int inputSize, String analysisName) {

    final DecimalFormat doubleFormat = new DecimalFormat("#.##");

    if(!outputFile.exists()) {
      try {
        outputFile.createNewFile();
      } catch (IOException e) {
        logger.severe(e.getMessage());
        throw new ComplexityAnalysisException(e);
      }

      //write csv header
      try(FileWriter fw = new FileWriter(outputFile, true)) {
        fw.write("Target," +
            "inputSize," +
            "analysis," +
            "minReward," +
            "bestReward," +
            "bestRewardSampleNum," +
            "bestRewardTime[" + statistics.getTimeUnit().toString() + "]," +
            "bestRewardCount," +
            "totalSampleNum/paths," +
            "totalUniqueSampleNum/paths," +
            "totalAnalysisTime[" + statistics.getTimeUnit().toString() + "]," +
            "avgThroughput[#samples/" + statistics.getTimeUnit().toString() + "]," +
            "seed," +
            "rewardMean," +
            "rewardVariance," +
            "rewardStdDev" +
            "\n");
      } catch (IOException e) {
        throw new ComplexityAnalysisException(e);
      }
    }

    StringBuilder sb = new StringBuilder();
    sb.append(target).append(',')
        .append(inputSize).append(',')
        .append(analysisName).append(',')
        .append(statistics.getMinReward()).append(',')
        .append(statistics.getBestReward()).append(',')
        .append(statistics.getBestRewardSampleNum()).append(',')
        .append(statistics.getBestRewardTime()).append(',')
        .append(statistics.getNumberOfBestRewards()).append(',')
        .append(statistics.getTotalSampleNum()).append(',')
        .append(statistics.getUniqueSampleNum()).append(',')
        .append(statistics.getTotalAnalysisTime()).append(',')
        .append(doubleFormat.format(statistics.getAvgThroughput())).append(',')
        .append(seed).append(',')
        .append(doubleFormat.format(statistics.getRewardMean())).append(',')
        .append(doubleFormat.format(statistics.getRewardVariance())).append(',')
        .append(doubleFormat.format(statistics.getRewardStandardDeviation()))
        .append('\n');
    //Append results to file

    try(FileWriter fw = new FileWriter(outputFile, true)) {
      fw.write(sb.toString());
    } catch (IOException e) {
      logger.severe(e.getMessage());
      throw new ComplexityAnalysisException(e);
    }
  }

  private static File getOutputFile(Config config, File outputDir) {
    String fileName = config.getTarget() + "_" + config.get(Utils.ANALYSIS_TYPE) +
        "_complexity.csv";
    return new File(outputDir, fileName);
  }
}
