package edu.cmu.sv.isstac.sampling.quantification;

import org.antlr.runtime.RecognitionException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.mcts.MCTSShell;
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
  private static final Logger LOGGER = JPFLogger.getLogger(ModelCounterFactory.class.getName());

  public static final String MODEL_COUNTING_PREFIX = MCTSShell.MCTS_CONF_PRFX + ".modelcounting";

  public static final String PROBLEMSETTINGS_CONF = MODEL_COUNTING_PREFIX + ".problemsettings";

  //Kernels for Sequential and Barvinok
  public static final String KERNELS_CONF = MODEL_COUNTING_PREFIX + "modelcounter.sequential" +
      ".kernels";
  public static final int KERNELS_DEF_CONF = 1;

  //TODO: should fix these three. They are reused from other projects
  public static final String TMP_DIR_CONF = MODEL_COUNTING_PREFIX + ".tmpDir";
  public static final String TMP_DIR_DEF_CONF = "/tmp";

  public static final String OMEGA_PATH_CONF = MODEL_COUNTING_PREFIX + ".omegaPath";
  public static final String OMEGA_PATH_DEF_CONF = "symbolic.reliability.omegaPath";

  public static final String LATTE_PATH_CONF = MODEL_COUNTING_PREFIX + ".lattePath";
  public static final String LATTE_PATH_DEF_CONF = "symbolic.reliability.lattePath";

  //model counter type
  public static final String MODEL_COUNTER_TYPE = MODEL_COUNTING_PREFIX + ".modelcounter.type";
  public static final String PARALLEL_MODEL_COUNTER_THREADS = MODEL_COUNTING_PREFIX + ".modelcounter" +
      ".parallel.threads";
  public static final int PARALLEL_MODEL_COUNTER_THREADS_DEF = 4;
  public static final String PARALLEL_MODEL_COUNTER_CACHE_FILLER = MODEL_COUNTING_PREFIX + ".modelcounter" +
      ".parallel.cachefiller";
  public static final int PARALLEL_MODEL_COUNTER_CACHE_FILLER_DEF = 80;

  public static final ModelCounterType MODEL_COUNTER_TYPE_DEF = ModelCounterType.SEQUENTIAL;

  //TODO: Add option for cleaning up the gigantic load of temp files

  public static SPFModelCounter getInstance(Config config) throws ModelCounterCreationException {
    if(config.hasValue(PROBLEMSETTINGS_CONF)) {
      String problemSettingsPath = config.getString(PROBLEMSETTINGS_CONF);
      ProblemSetting problemSettings = null;
      try {
        problemSettings = ProblemSetting.loadFromFile(problemSettingsPath);
      } catch (IOException | RecognitionException e) {
        LOGGER.severe(e.getMessage());
        throw new ModelCounterCreationException(e);
      }

      return createModelCounterWithProblemSettings(config, problemSettings);

    } else {
      //We use the model counter decorator to *lazily* create an model counter instance that
      //automatically generates uniform usage profiles.
      return new UniformUPModelCounterDecorator(config);
    }
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
          LOGGER.severe(e.getMessage());
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
          LOGGER.severe(e.getMessage());
          throw new ModelCounterCreationException(e);
        }
        break;
      case BARVINOK:
        try {
          analyzer = new SequentialAnalyzerBarvinok(configuration, problemSettings.getDomain(),
              problemSettings.getUsageProfile(), numOfKernels);
        } catch (LatteException | InterruptedException | OmegaException | RecognitionException
            e) {
          LOGGER.severe(e.getMessage());
          throw new ModelCounterCreationException(e);
        }
        break;
      default:
        String msg = "Unsupported model counter type " + modelCounterType;
        LOGGER.severe(msg);
        throw new ModelCounterCreationException(msg);
    }
    return new SPFModelCounterDecorator(analyzer);
  }

  static Configuration getModelCounterConfig(Config config) {
    Configuration configuration = new Configuration();
    configuration.setTemporaryDirectory(config.getString(TMP_DIR_CONF, TMP_DIR_DEF_CONF));
    configuration.setOmegaExectutablePath(config.getString(OMEGA_PATH_CONF, config.getString
        (OMEGA_PATH_DEF_CONF)));
    configuration.setLatteExecutablePath(config.getString(LATTE_PATH_CONF, config.getString
        (LATTE_PATH_DEF_CONF)));
    return configuration;
  }

  static ModelCounterType getModelCounterType(Config config) {
    if (config.hasValue(MODEL_COUNTER_TYPE)) {
      return ModelCounterType.valueOf(config.getString(MODEL_COUNTER_TYPE));
    } else {
      return MODEL_COUNTER_TYPE_DEF;
    }
  }

  static int getKernels(Config config) {
    return config.getInt(KERNELS_CONF, KERNELS_DEF_CONF);
  }
}
