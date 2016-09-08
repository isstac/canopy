package edu.cmu.sv.isstac.sampling.quantification;

import org.antlr.runtime.RecognitionException;

import java.io.IOException;

import edu.cmu.sv.isstac.sampling.mcts.MCTSShell;
import gov.nasa.jpf.Config;
import modelcounting.analysis.Analyzer;
import modelcounting.analysis.SequentialAnalyzer;
import modelcounting.domain.ProblemSetting;
import modelcounting.latte.LatteException;
import modelcounting.omega.exceptions.OmegaException;
import modelcounting.utils.Configuration;

/**
 * @author Kasper Luckow
 */
public class AnalyzerFactory {
  public static final String MODEL_COUNTING_PREFIX = MCTSShell.MCTS_CONF_PRFX + ".modelcounting";

  public static final String PROBLEMSETTINGS_CONF = MODEL_COUNTING_PREFIX + ".problemsettings";
  public static final String KERNELS_CONF = MODEL_COUNTING_PREFIX + ".kernels";
  public static final int KERNELS_DEF_CONF = 1;

  //TODO: should fix these three. They are reused from other projects
  public static final String TMP_DIR_CONF = MODEL_COUNTING_PREFIX + ".tmpDir";
  public static final String TMP_DIR_DEF_CONF = "symbolic.reliability.tmpDir";

  public static final String OMEGA_PATH_CONF = MODEL_COUNTING_PREFIX + ".omegaPath";
  public static final String OMEGA_PATH_DEF_CONF = "symbolic.reliability.omegaPath";

  public static final String LATTE_PATH_CONF = MODEL_COUNTING_PREFIX + ".lattePath";
  public static final String LATTE_PATH_DEF_CONF = "symbolic.reliability.lattePath";


  public static Analyzer create(Config config) throws IOException, RecognitionException,
      InterruptedException, OmegaException, LatteException {
    if(!config.hasValue(PROBLEMSETTINGS_CONF)) {
      throw new ModelCountingException("Problem settings not set. Please set with: " +
          PROBLEMSETTINGS_CONF);
    }
    String problemSettingsPath = config.getString(PROBLEMSETTINGS_CONF);
    ProblemSetting problemSettings = ProblemSetting.loadFromFile(problemSettingsPath);

    Configuration configuration = new Configuration();
    configuration.setTemporaryDirectory(config.getString(TMP_DIR_CONF, config.getString
        (TMP_DIR_DEF_CONF)));
    configuration.setOmegaExectutablePath(config.getString(OMEGA_PATH_CONF, config.getString
        (OMEGA_PATH_DEF_CONF)));
    configuration.setLatteExecutablePath(config.getString(LATTE_PATH_CONF, config.getString
        (LATTE_PATH_DEF_CONF)));

    int numOfKernels = config.getInt(KERNELS_CONF, KERNELS_DEF_CONF);

    //TODO: There is also a parallel implementation of the Analyzer interface
    return new SequentialAnalyzer(configuration, problemSettings.getDomain(),
        problemSettings.getUsageProfile(), numOfKernels);
  }
}
