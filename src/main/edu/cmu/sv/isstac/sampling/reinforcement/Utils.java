package edu.cmu.sv.isstac.sampling.reinforcement;

import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.policies.UniformSimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class Utils {

  public static final String RL_CONF_PREFIX = Options.SAMPLING_CONF_PREFIX + ".rl";

  public static final String SAMPLES_PER_OPTIMIZATION = RL_CONF_PREFIX + ".samplesperoptimization";

  public static final String EPSILON = RL_CONF_PREFIX + ".epsilon";
  public static final String HISTORY = RL_CONF_PREFIX + ".history";

  // Some defaults
  public static final int DEFAULT_SAMPLES_PER_OPTIMIZATION = 100;
  public static final double DEFAULT_EPSILON = 0.5;
  public static final double DEFAULT_HISTORY = 0.5;
}
