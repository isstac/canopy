package edu.cmu.sv.isstac.sampling.quantification;

import org.antlr.runtime.RecognitionException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.Options;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.JPFLogger;
import modelcounting.analysis.Analyzer;
import modelcounting.analysis.ParallelAnalyzer;
import modelcounting.analysis.SequentialAnalyzer;
import modelcounting.analysis.SequentialAnalyzerBarvinok;
import modelcounting.domain.ProblemSetting;
import modelcounting.latte.LatteException;
import modelcounting.omega.exceptions.OmegaException;
import modelcounting.utils.Configuration;

/**
 * @author Kasper Luckow
 */
public class ModelCounterFactory {
  private static final Logger logger = JPFLogger.getLogger(ModelCounterFactory.class.getName());

  public static final String PROBLEMSETTINGS_CONF = Options.MODEL_COUNTING_PREFIX + ".problemsettings";

  //Kernels for Sequential and Barvinok
  public static final String KERNELS_CONF = Options.MODEL_COUNTING_PREFIX + "modelcounter.sequential" +
      ".kernels";
  public static final int KERNELS_DEF_CONF = 1;

  public static final String TMP_DIR_CONF = Options.MODEL_COUNTING_PREFIX + ".tmpDir";
  public static final String TMP_DIR_DEF_CONF = "/tmp/modelcounter";
  public static final String KEEP_TMP_DIR_CONF = TMP_DIR_CONF + ".keep";
  public static final boolean KEEP_TMP_DIR_DEF = false;

  public static final String OMEGA_PATH_CONF = Options.MODEL_COUNTING_PREFIX + ".omegaPath";
  public static final String OMEGA_PATH_DEF_CONF = "symbolic.reliability.omegaPath";

  public static final String LATTE_PATH_CONF = Options.MODEL_COUNTING_PREFIX + ".lattePath";
  public static final String LATTE_PATH_DEF_CONF = "symbolic.reliability.lattePath";

  //model counter type
  public static final String MODEL_COUNTER_TYPE = Options.MODEL_COUNTING_PREFIX + ".modelcounter.type";
  public static final String PARALLEL_MODEL_COUNTER_THREADS = Options.MODEL_COUNTING_PREFIX + ".modelcounter" +
      ".parallel.threads";
  public static final int PARALLEL_MODEL_COUNTER_THREADS_DEF = 4;
  public static final String PARALLEL_MODEL_COUNTER_CACHE_FILLER = Options.MODEL_COUNTING_PREFIX + ".modelcounter" +
      ".parallel.cachefiller";
  public static final int PARALLEL_MODEL_COUNTER_CACHE_FILLER_DEF = 80;

  public static final ModelCounterType MODEL_COUNTER_TYPE_DEF = ModelCounterType.SEQUENTIAL;



  //We use this map to cache instantiated model counters (per problem settings) for maximum reuse
  //and to harness the full potential of caching of counts
  private static Map<String, SPFModelCounter> modelCounters = new HashMap<>();

  public static SPFModelCounter getInstance(Config config) throws ModelCounterCreationException {

    SPFModelCounter modelCounter;
    if(config.hasValue(PROBLEMSETTINGS_CONF)) {
      String problemSettingsPath = config.getString(PROBLEMSETTINGS_CONF);
      // If we already constructed a model counter, just reuse it
      if(modelCounters.containsKey(problemSettingsPath)) {
        logger.info("Reusing model counter for problem settings file " + problemSettingsPath);
        return modelCounters.get(problemSettingsPath);
      }
      ProblemSetting problemSettings = null;
      try {
        problemSettings = ProblemSetting.loadFromFile(problemSettingsPath);
      } catch (IOException | RecognitionException e) {
        logger.severe(e.getMessage());
        throw new ModelCounterCreationException(e);
      }
      modelCounter = createModelCounterWithProblemSettings(config, problemSettings);

      // Cache the model counter instance
      modelCounters.put(problemSettingsPath, modelCounter);
    } else {
      //We use the model counter decorator to *lazily* create an model counter instance that
      //automatically generates uniform usage profiles.
      logger.info("Using lazy model counter that creates uniform UP from variables" +
          " of PC at model count invocation. This may not be what you want!");
      modelCounter = new UniformUPModelCounterDecorator(config);
    }

    return modelCounter;
  }

  public static void cleanUpTempFiles(Config config) throws IOException {
    String tempFilesPath = config.getString(TMP_DIR_CONF, TMP_DIR_DEF_CONF);
    logger.info("Removing model counter tmp dir: " + tempFilesPath);
    FileUtils.deleteDirectory(new File(tempFilesPath));
  }

  static SPFModelCounter createModelCounterWithProblemSettings(Config config, ProblemSetting
      problemSettings) throws ModelCounterCreationException {
    Configuration configuration = getModelCounterConfig(config);

    int numOfKernels = getKernels(config);
    ModelCounterType modelCounterType = getModelCounterType(config);

    Analyzer analyzer;
    switch (modelCounterType) {
      case SEQUENTIAL:
        try {
          analyzer = new SequentialAnalyzer(configuration, problemSettings.getDomain(),
              problemSettings.getUsageProfile(), numOfKernels);
        } catch (LatteException | InterruptedException | OmegaException | RecognitionException
            e) {
          logger.severe(e.getMessage());
          throw new ModelCounterCreationException(e);
        }
        break;
      case PARALLEL:
        int threads = config.getInt(PARALLEL_MODEL_COUNTER_THREADS, PARALLEL_MODEL_COUNTER_THREADS_DEF);
        int cacheFillerPercentage = config.getInt(PARALLEL_MODEL_COUNTER_CACHE_FILLER,
            PARALLEL_MODEL_COUNTER_CACHE_FILLER_DEF);
        try {
          analyzer = new ParallelAnalyzer(configuration, problemSettings.getDomain(),
              problemSettings.getUsageProfile(), threads, cacheFillerPercentage, numOfKernels);
        } catch (ExecutionException | InterruptedException
            | RecognitionException | LatteException | OmegaException e) {
          logger.severe(e.getMessage());
          throw new ModelCounterCreationException(e);
        }
        break;
      case BARVINOK:
        try {
          analyzer = new SequentialAnalyzerBarvinok(configuration, problemSettings.getDomain(),
              problemSettings.getUsageProfile(), numOfKernels);
        } catch (LatteException | InterruptedException | OmegaException | RecognitionException
            e) {
          logger.severe(e.getMessage());
          throw new ModelCounterCreationException(e);
        }
        break;
      default:
        String msg = "Unsupported model counter type " + modelCounterType;
        logger.severe(msg);
        throw new ModelCounterCreationException(msg);
    }
    SPFModelCounter spfmodelCounter = new SPFModelCounterDecorator(analyzer);
    return spfmodelCounter;
  }

  private static Configuration getModelCounterConfig(Config config) {
    Configuration configuration = new Configuration();
    configuration.setTemporaryDirectory(config.getString(TMP_DIR_CONF, TMP_DIR_DEF_CONF));
    configuration.setOmegaExectutablePath(config.getString(OMEGA_PATH_CONF, config.getString
        (OMEGA_PATH_DEF_CONF)));
    configuration.setLatteExecutablePath(config.getString(LATTE_PATH_CONF, config.getString
        (LATTE_PATH_DEF_CONF)));
    return configuration;
  }

  private static ModelCounterType getModelCounterType(Config config) {
    if (config.hasValue(MODEL_COUNTER_TYPE)) {
      return ModelCounterType.valueOf(config.getString(MODEL_COUNTER_TYPE));
    } else {
      return MODEL_COUNTER_TYPE_DEF;
    }
  }

  private static int getKernels(Config config) {
    return config.getInt(KERNELS_CONF, KERNELS_DEF_CONF);
  }
}
