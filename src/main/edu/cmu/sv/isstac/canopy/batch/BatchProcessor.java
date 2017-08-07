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

package edu.cmu.sv.isstac.canopy.batch;

import com.google.common.base.Stopwatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.canopy.AnalysisCreationException;
import edu.cmu.sv.isstac.canopy.AnalysisStrategy;
import edu.cmu.sv.isstac.canopy.Options;
import edu.cmu.sv.isstac.canopy.SamplingAnalysis;
import edu.cmu.sv.isstac.canopy.analysis.RewardDataSetGenerator;
import edu.cmu.sv.isstac.canopy.analysis.SampleStatistics;
import edu.cmu.sv.isstac.canopy.exploration.cache.HashingCache;
import edu.cmu.sv.isstac.canopy.exploration.cache.NoCache;
import edu.cmu.sv.isstac.canopy.termination.SampleSizeTerminationStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class BatchProcessor {
  public static final Logger logger = JPFLogger.getLogger(BatchProcessor.class.getName());

  //This one is important: it determines the initial
  //seed for the rng that will generate seeds for the experiments
  //Note that in order to reproduce the results, not only must the seed
  //of course be the same, but also the *order* of the experiments must
  //be the same!
  private static final int DEFAULT_SEED = 12;
  private static final int SAMPLE_SIZE_PER_EXPERIMENT = 2000;//Integer.MAX_VALUE;
  private static final int DEFAULT_ITERATIONS_PER_EXPERIMENT = 50;
  private static final boolean OUTPUT_DATASET = true;

  public static void main(String[] args) throws AnalysisCreationException {
    if(args.length < 2 || args.length > 3) {
      printUsage();
      return;
    }
    int iterations = DEFAULT_ITERATIONS_PER_EXPERIMENT;
    File input = new File(args[0]);
    File outputFolder = null;

    if(args.length == 3) {
      iterations = Integer.parseInt(args[1]);
      outputFolder = new File(args[2]);
    } else if(args.length == 2) {
      outputFolder = new File(args[1]);
    }

    assert outputFolder != null;
    List<Experiment> experiments = createDefaultExperiments();

    Collection<File> jpfConfigs = new ArrayList<>();
    if(input.isFile()) {
      //If input is just a file, this is the only one that will be used for batch run
      jpfConfigs.add(input);

    } else if(input.isDirectory()) {
      //If input is a directory, then load *all* files with .jpf extension
      for(File jpfFile : input.listFiles()) {
        if (!jpfFile.getName().endsWith(".jpf")) {
          logger.info("Skipping file " + jpfFile.getName());
          continue;
        }
        logger.info("Processing jpf file: " + jpfFile.getName());
        jpfConfigs.add(jpfFile);
      }
    }

    performBatchProcessing(jpfConfigs, outputFolder, iterations, experiments, DEFAULT_SEED);
  }

  private static List<Experiment> createDefaultExperiments() {
    List<Experiment> experiments = new ArrayList<>();
    //MCTS: just pruning
//    experiments.add(new BacktrackingDecorator(mctsExp, true, true));
//    experiments.add(new BacktrackingDecorator(mctsExp, false, true));
//    experiments.add(new BacktrackingDecorator(mctsExp, false, false));
//    experiments.add(new BacktrackingDecorator(mctsExp, true, false));

//    Experiment exp = new MCTSExperiment(true, false, false, 5);
//
//    experiments.add(new CachingDecorator(exp, HashingCache.class));
//    experiments.add(new CachingDecorator(exp, NoCache.class));

    /*
     * Pruning
     */
//    experiments.add(new MCTSExperiment(true, false, false, Math.sqrt(2)));
//    experiments.add(new MCTSExperiment(true, false, false, 5));
//    experiments.add(new MCTSExperiment(true, false, false, 10));
//    experiments.add(new MCTSExperiment(true, false, false, 20));
//    experiments.add(new MCTSExperiment(true, false, false, 50));
//    experiments.add(new MCTSExperiment(true, false, false, 100));
//
    experiments.add(new MonteCarloExperiment(true));
//
//    experiments.add(new RLExperiment(true, false, false, 250, 0.5, 0.5));
//    experiments.add(new RLExperiment(true, false, false, 100, 0.5, 0.5));
//    experiments.add(new RLExperiment(true, false, false, 10, 0.5, 0.5));
//    experiments.add(new RLExperiment(true, false, false, 100, 0.1, 0.1));
//    experiments.add(new RLExperiment(true, false, false, 100, 0.9, 0.9));
//    experiments.add(new RLExperiment(true, false, false, 100, 0.9, 0.1));
//    experiments.add(new RLExperiment(true, false, false, 100, 0.1, 0.9));
//    experiments.add(new RLExperiment(true, false, false, 1, 0.1, 0.1));
//    experiments.add(new RLExperiment(true, false, false, 1, 0.5, 0.5));


    /*
     * No pruning
     */
//    experiments.add(new MCTSExperiment(false, false, false, Math.sqrt(2)));
//    experiments.add(new MCTSExperiment(false, false, false, 5));
//    experiments.add(new MCTSExperiment(false, false, false, 10));
//    experiments.add(new MCTSExperiment(false, false, false, 20));
//    experiments.add(new MCTSExperiment(false, false, false, 50));
//    experiments.add(new MCTSExperiment(false, false, false, 100));

    // Monte Carlo experiment
//    experiments.add(new MonteCarloExperiment(false));

    return experiments;
  }

  public static void performBatchProcessing(Collection<File> jpfConfigs, File resultsFolder, int
      iterations, List<Experiment> experiments, long initSeed) throws AnalysisCreationException {
    //Random rng = new Random(initSeed);

    Stopwatch stopwatch = Stopwatch.createStarted();

    for(File jpfFile : jpfConfigs) {
      logger.info("Processing jpf file: " + jpfFile.getName());

      //Seriously messed up ctors for Config class.
      Config conftmp = new Config(new String[] { jpfFile.getAbsolutePath() });
      String targetName = conftmp.getString("target");
      String outputFile = getOutputFile(jpfFile.getName(), resultsFolder);

      for (Experiment experiment : experiments) {

        //To make experiments comparable, we reset the seed. This is maybe not what we want...
        Random rng = new Random(initSeed);
        for (int iteration = 1; iteration <= iterations; iteration++) {
          logger.info("Processing jpf file " + jpfFile.getName() + " Experiment " + experiment
              .getName() + " iteration " + iteration);

          int seed = 608941957;//rng.nextInt();

          Config conf = new Config(new String[] { jpfFile.getAbsolutePath() });
          conf.setProperty(Options.SHOW_LIVE_STATISTICS, Boolean.toString(false));
          conf.setProperty(Options.SHOW_STATISTICS, Boolean.toString(false));

          SamplingAnalysis.Builder analysisBuilder = new SamplingAnalysis.Builder();
          AnalysisStrategy analysisStrategy = experiment.createAnalysisStrategy(conf, seed);

          //Add the statistics reporter
          SampleStatistics statistics = new SampleStatistics();
          analysisBuilder.addEventObserver(statistics);

          //Output dataset
          if(OUTPUT_DATASET) {
            String dataSetFileName = outputFile.replace(".csv", "_dataset.csv");
            RewardDataSetGenerator rwGen = new RewardDataSetGenerator(dataSetFileName);
            analysisBuilder.addEventObserver(rwGen);
          }

          analysisBuilder.addTerminationStrategy(
              new SampleSizeTerminationStrategy(SAMPLE_SIZE_PER_EXPERIMENT));
          SamplingAnalysis analysis =
              analysisBuilder.build(conf, analysisStrategy, experiment.getJPFFactory());

          analysis.run();

          writeStatisticsToFile(statistics, iteration, seed, targetName, experiment.getName(),
              outputFile);
        }
      }
    }

    stopwatch.stop();

    logger.info("Batch processing done. Took " + stopwatch.elapsed(TimeUnit.SECONDS) + "s");
  }

  private static void writeStatisticsToFile(SampleStatistics statistics, int iteration, long seed,
                                            String target, String experimentName, String outputFile) {

    final DecimalFormat doubleFormat = new DecimalFormat("#.##");
    File csvFile = new File(outputFile);
    if(!csvFile.exists()) {
      try {
        csvFile.createNewFile();
      } catch (IOException e) {
        throw new BatchProcessorException(e);
      }

      //write csv header
      try(FileWriter fw = new FileWriter(csvFile, true)) {
        fw.write("Target," +
            "experiment," +
            "iteration," +
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
        throw new BatchProcessorException(e);
      }
    }

    StringBuilder sb = new StringBuilder();
    sb.append(target).append(',')
        .append(experimentName).append(',')
        .append(iteration).append(',')
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

    try(FileWriter fw = new FileWriter(csvFile, true)) {
      fw.write(sb.toString());
    } catch (IOException e) {
      throw new BatchProcessorException(e);
    }
  }

  private static String getOutputFile(String jpfFile, File outputDir) {
    String fileName = jpfFile.substring(0, jpfFile.indexOf(".jpf")) + ".csv";
    return new File(outputDir, fileName).getAbsolutePath();
  }

  private static void printUsage() {
    System.out.println("Usage: <input folder with jpf files> [iterations per jpf file] <output " +
        "folder>");
  }
}
