package edu.cmu.sv.isstac.sampling.montecarlo;

import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.policies.UniformSimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class Utils {

  public static final String MC_CONF_PRFX = Options.SAMPLING_CONF_PREFIX + ".montecarlo";

  public static final String SIMULATION_POLICY = MC_CONF_PRFX + ".simulationpol";

  public static SimulationPolicy createSimulationPolicy(Config conf)
      throws ModelCounterCreationException {
    if (conf.hasValue(SIMULATION_POLICY)) {
      return conf.getInstance(SIMULATION_POLICY, SimulationPolicy.class);
    }

    long seed = Options.getSeed(conf);
    return new UniformSimulationPolicy(seed);
  }

}
