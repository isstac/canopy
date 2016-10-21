package edu.cmu.sv.isstac.sampling.batch;

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

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.analysis.SampleStatistics;
import edu.cmu.sv.isstac.sampling.termination.SampleSizeTerminationStrategy;
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
  private static final int DEFAULT_SEED = 112119;
  private static final int SAMPLE_SIZE_PER_EXPERIMENT = 2;
  private static final int DEFAULT_ITERATIONS_PER_EXPERIMENT = 1;

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
    //experiments.add(new MCTSExperiment(true, false, false, 0));
    experiments.add(new MCTSExperiment(true, false, false, Math.sqrt(2)));
    experiments.add(new MCTSExperiment(true, false, false, 5));
    experiments.add(new MCTSExperiment(true, false, false, 10));
    experiments.add(new MCTSExperiment(true, false, false, 20));
    experiments.add(new MCTSExperiment(true, false, false, 50));
    experiments.add(new MCTSExperiment(true, false, false, 100));

    // Monte Carlo experiment
    experiments.add(new MonteCarloExperiment());

    //Reinforcement Learning: pruning, reward amplification, 50 samples per opt., epsilon 0.5,
    // history 0.5
    // experiments.add(new RLExperiment(true, true, 50, 0.5, 0.5));

    return experiments;
  }

  public static void performBatchProcessing(Collection<File> jpfConfigs, File resultsFolder, int
      iterations, List<Experiment> experiments, long initSeed) throws AnalysisCreationException {
    Random rng = new Random(initSeed);

    Stopwatch stopwatch = Stopwatch.createStarted();

    for(File jpfFile : jpfConfigs) {
      logger.info("Processing jpf file: " + jpfFile.getName());

      //Seriously messed up ctors for Config class.
      Config conftmp = new Config(new String[] { jpfFile.getAbsolutePath() });
      String targetName = conftmp.getString("target");
      String outputFile = getOutputFile(jpfFile.getName(), resultsFolder);

      for (Experiment experiment : experiments) {
        for (int iteration = 1; iteration <= iterations; iteration++) {
          logger.info("Processing jpf file " + jpfFile.getName() + " Experiment " + experiment
              .getName() + " iteration " + iteration);

          int seed = rng.nextInt();

          Config conf = new Config(new String[] { jpfFile.getAbsolutePath() });
          conf.setProperty(Options.SHOW_LIVE_STATISTICS, Boolean.toString(false));
          conf.setProperty(Options.SHOW_STATISTICS, Boolean.toString(false));

          SamplingAnalysis.Builder analysisBuilder = new SamplingAnalysis.Builder();
          AnalysisStrategy analysisStrategy = experiment.createAnalysisStrategy(conf, seed);

          //Add the statistics reporter
          SampleStatistics statistics = new SampleStatistics();
          analysisBuilder.addEventObserver(statistics);
          analysisBuilder.setTerminationStrategy(new SampleSizeTerminationStrategy(SAMPLE_SIZE_PER_EXPERIMENT));
          SamplingAnalysis analysis = analysisBuilder.build(conf, analysisStrategy, new JPFSamplerFactory());
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
