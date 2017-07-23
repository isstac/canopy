package edu.cmu.sv.isstac.sampling.batch;

import com.google.common.base.Stopwatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.analysis.RewardDataSetGenerator;
import edu.cmu.sv.isstac.sampling.analysis.SampleStatistics;
import edu.cmu.sv.isstac.sampling.exploration.cache.NoCache;
import edu.cmu.sv.isstac.sampling.termination.SampleSizeTerminationStrategy;
import edu.cmu.sv.isstac.sampling.termination.TimeBoundedTerminationStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class BatchProcessorIncreasingSize {
  public static final Logger logger = JPFLogger.getLogger(BatchProcessorIncreasingSize.class.getName());

  //This one is important: it determines the initial
  //seed for the rng that will generate seeds for the experiments
  //Note that in order to reproduce the results, not only must the seed
  //of course be the same, but also the *order* of the experiments must
  //be the same!
  private static final int DEFAULT_SEED;
  private static final int SAMPLE_SIZE_PER_EXPERIMENT = 2000;//Integer.MAX_VALUE;
  private static final int DEFAULT_ITERATIONS_PER_EXPERIMENT = 5;

  // For increasing input size experiment
  private static int defaultStartInputSize = 1;
  private static int defaultEndInputSize = 30;
  private static int defaultIncrement = 1;
  private static final int exhaustiveTimeBound = 10; //seconds

  private static final boolean OUTPUT_DATASET = false;
  private static Map<String, Experiment> str2exp = new HashMap<>();

  static {
    DEFAULT_SEED  = new Random().nextInt();

    str2exp.put("mcts2pnc", new CachingDecorator(new MCTSExperiment(true, false, false, Math
        .sqrt(2)), NoCache.class));
    str2exp.put("mcts5pnc", new CachingDecorator(new MCTSExperiment(true, false, false, 5),
        NoCache.class));
    str2exp.put("mcts10pnc", new CachingDecorator(new MCTSExperiment(true, false, false, 10), NoCache.class));
    str2exp.put("mcts20pnc", new CachingDecorator(new MCTSExperiment(true, false, false, 20), NoCache.class));
    str2exp.put("mcts50pnc", new CachingDecorator(new MCTSExperiment(true, false, false, 50), NoCache.class));
    str2exp.put("mcts100pnc", new CachingDecorator(new MCTSExperiment(true, false, false, 100), NoCache.class));


    str2exp.put("mcts2p", new MCTSExperiment(true, false, false, Math.sqrt(2)));
    str2exp.put("mcts5p", new MCTSExperiment(true, false, false, 5));
    str2exp.put("mcts10p", new MCTSExperiment(true, false, false, 10));
    str2exp.put("mcts20p", new MCTSExperiment(true, false, false, 20));
    str2exp.put("mcts50p", new MCTSExperiment(true, false, false, 50));
    str2exp.put("mcts100p", new MCTSExperiment(true, false, false, 100));

    str2exp.put("mcts2", new MCTSExperiment(false, false, false, Math.sqrt(2)));
    str2exp.put("mcts5", new MCTSExperiment(false, false, false, 5));
    str2exp.put("mcts10", new MCTSExperiment(false, false, false, 10));
    str2exp.put("mcts20", new MCTSExperiment(false, false, false, 20));
    str2exp.put("mcts50", new MCTSExperiment(false, false, false, 50));
    str2exp.put("mcts100", new MCTSExperiment(false, false, false, 100));

    str2exp.put("mcp", new MonteCarloExperiment(true));
    str2exp.put("mc", new MonteCarloExperiment(false));

    str2exp.put("exhaustive", new ExhaustiveExperiment());

    str2exp.put("rl250-05-05", new RLExperiment(true, false, false, 250, 0.5, 0.5));
    str2exp.put("rl100-05-05", new RLExperiment(true, false, false, 100, 0.5, 0.5));
    str2exp.put("rl10-05-05", new RLExperiment(true, false, false, 10, 0.5, 0.5));
    str2exp.put("rl100-01-01", new RLExperiment(true, false, false, 100, 0.1, 0.1));
    str2exp.put("rl100-09-09", new RLExperiment(true, false, false, 100, 0.9, 0.9));
    str2exp.put("rl100-09-01", new RLExperiment(true, false, false, 100, 0.9, 0.1));
    str2exp.put("rl100-01-09", new RLExperiment(true, false, false, 100, 0.1, 0.9));
    str2exp.put("rl1-01-01", new RLExperiment(true, false, false, 1, 0.1, 0.1));
    str2exp.put("rl1-05-05", new RLExperiment(true, false, false, 1, 0.5, 0.5));
  }

  public static void main(String[] args) throws AnalysisCreationException {
    if(args.length < 2 || args.length > 7) {
      printUsage();
      return;
    }
    int iterations = DEFAULT_ITERATIONS_PER_EXPERIMENT;
    File input = new File(args[0]);
    File outputFolder = null;
    int startInputSize = defaultStartInputSize;
    int endInputSize = defaultEndInputSize;
    int increment = defaultIncrement;

    List<Experiment> experiments = null;
    if(args.length == 6) {
      outputFolder = new File(args[1]);
      experiments = createExperimentsFromCLI(args[2]);
      startInputSize = Integer.parseInt(args[3]);
      endInputSize = Integer.parseInt(args[4]);
      increment = Integer.parseInt(args[5]);
      iterations = Integer.parseInt(args[6]);
    } else if(args.length == 2) {
      experiments = createDefaultExperiments();
      outputFolder = new File(args[1]);
    }

    assert outputFolder != null;


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

    performBatchProcessing(jpfConfigs, outputFolder, iterations, experiments, DEFAULT_SEED,
        startInputSize, endInputSize, increment);
  }

  private static List<Experiment> createExperimentsFromCLI(String arg) {
    List<Experiment> exps = new ArrayList<>();
    for(String strExp : arg.split(",")) {
      if(!str2exp.containsKey(strExp)) {
        throw new RuntimeException("Key not valid experiment: " + strExp);
      }
      exps.add(str2exp.get(strExp));
    }
    return exps;
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
    experiments.add(new MCTSExperiment(true, false, false, Math.sqrt(2)));
    experiments.add(new MCTSExperiment(true, false, false, 5));
    experiments.add(new MCTSExperiment(true, false, false, 10));
    experiments.add(new MCTSExperiment(true, false, false, 20));
    experiments.add(new MCTSExperiment(true, false, false, 50));
    experiments.add(new MCTSExperiment(true, false, false, 100));

    experiments.add(new MonteCarloExperiment(true));

    experiments.add(new RLExperiment(true, false, false, 250, 0.5, 0.5));
    experiments.add(new RLExperiment(true, false, false, 100, 0.5, 0.5));
    experiments.add(new RLExperiment(true, false, false, 10, 0.5, 0.5));
    experiments.add(new RLExperiment(true, false, false, 100, 0.1, 0.1));
    experiments.add(new RLExperiment(true, false, false, 100, 0.9, 0.9));
    experiments.add(new RLExperiment(true, false, false, 100, 0.9, 0.1));
    experiments.add(new RLExperiment(true, false, false, 100, 0.1, 0.9));
    experiments.add(new RLExperiment(true, false, false, 1, 0.1, 0.1));
    experiments.add(new RLExperiment(true, false, false, 1, 0.5, 0.5));


    /*
     * No pruning
     */
    experiments.add(new MCTSExperiment(false, false, false, Math.sqrt(2)));
    experiments.add(new MCTSExperiment(false, false, false, 5));
    experiments.add(new MCTSExperiment(false, false, false, 10));
    experiments.add(new MCTSExperiment(false, false, false, 20));
    experiments.add(new MCTSExperiment(false, false, false, 50));
    experiments.add(new MCTSExperiment(false, false, false, 100));

    // Monte Carlo experiment
    experiments.add(new MonteCarloExperiment(false));
    experiments.add(new ExhaustiveExperiment());

    return experiments;
  }

  public static void performBatchProcessing(Collection<File> jpfConfigs, File resultsFolder, int
      iterations, List<Experiment> experiments, long initSeed, int startInputSize, int
      endInputSize, int increment) throws
      AnalysisCreationException {
    Random rng = new Random(initSeed);

    Stopwatch stopwatch = Stopwatch.createStarted();

    for(File jpfFile : jpfConfigs) {
      logger.info("Processing jpf file: " + jpfFile.getName());

      //Seriously messed up ctors for Config class.
      Config conftmp = new Config(new String[] { jpfFile.getAbsolutePath() });
      String targetName = conftmp.getString("target");
      String outputFile = getOutputFile(jpfFile.getName(), resultsFolder);

      for (Experiment experiment : experiments) {
        int iter;
        int samplesPerExp;
        if(experiment instanceof ExhaustiveExperiment) {
          //set iter to 1 here if not running the incremental solving experiment
          iter = iterations;

          samplesPerExp = Integer.MAX_VALUE;
        } else {
          iter = iterations;
          samplesPerExp = SAMPLE_SIZE_PER_EXPERIMENT;
        }
        boolean exhaustiveContinue = true;

        for(int inputSize = startInputSize; inputSize <= endInputSize; inputSize += increment) {
          if(!exhaustiveContinue) {
            break;
          }

          for (int iteration = 1; iteration <= iter; iteration++) {
            logger.info("Processing jpf file " + jpfFile.getName() + " Experiment " + experiment
                .getName() + " iteration " + iteration);

            int seed = rng.nextInt();

            Config conf = new Config(new String[]{jpfFile.getAbsolutePath()});

            conf.setProperty("target.args", Integer.toString(inputSize));
            conf.setProperty(Options.SHOW_LIVE_STATISTICS, Boolean.toString(false));
            conf.setProperty(Options.SHOW_STATISTICS, Boolean.toString(false));

            SamplingAnalysis.Builder analysisBuilder = new SamplingAnalysis.Builder();
            AnalysisStrategy analysisStrategy = experiment.createAnalysisStrategy(conf, seed);

            //Add the statistics reporter
            SampleStatistics statistics = new SampleStatistics();
            analysisBuilder.addEventObserver(statistics);

            //Output dataset
            if (OUTPUT_DATASET) {
              String dataSetFileName = outputFile.replace(".csv", "_dataset.csv");
              RewardDataSetGenerator rwGen = new RewardDataSetGenerator(dataSetFileName);
              analysisBuilder.addEventObserver(rwGen);
            }


            if (experiment instanceof ExhaustiveExperiment) {
              analysisBuilder.addTerminationStrategy(new TimeBoundedTerminationStrategy
                  (exhaustiveTimeBound, TimeUnit.SECONDS));
            } else {
              analysisBuilder.addTerminationStrategy(
                  new SampleSizeTerminationStrategy(samplesPerExp));
            }


            SamplingAnalysis analysis =
                analysisBuilder.build(conf, analysisStrategy, experiment.getJPFFactory());

            analysis.run();

            writeStatisticsToFile(inputSize, statistics, iteration, seed, targetName,
                experiment.getName(), outputFile);

            // If the time bound is exceeded, just stop the analysis
            // The - 5 here is extremely ugly, but otherwise we would have to capture that the
            // bound was exceeded... and im lazy
            if (experiment instanceof ExhaustiveExperiment &&
                statistics.getTotalAnalysisTime() > exhaustiveTimeBound - 5) {
              exhaustiveContinue = false;
            }
          }
        }
      }
    }

    stopwatch.stop();

    logger.info("Batch processing done. Took " + stopwatch.elapsed(TimeUnit.SECONDS) + "s");
  }

  private static void writeStatisticsToFile(int inputSize, SampleStatistics statistics, int
      iteration, long seed, String target, String experimentName, String outputFile) {

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
            "inputSize," +
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
        .append(inputSize).append(',')
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
