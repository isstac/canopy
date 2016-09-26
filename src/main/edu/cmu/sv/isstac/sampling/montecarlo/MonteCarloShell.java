package edu.cmu.sv.isstac.sampling.montecarlo;

import java.util.Random;

import edu.cmu.sv.isstac.sampling.AnalysisCreationException;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.SamplingAnalysis;
import edu.cmu.sv.isstac.sampling.SamplingShell;
import edu.cmu.sv.isstac.sampling.analysis.AbstractAnalysisProcessor;
import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.policies.CountWeightedSimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.search.SamplingSearch;
import edu.cmu.sv.isstac.sampling.exploration.AllChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.sampling.exploration.PruningChoicesStrategy;
import edu.cmu.sv.isstac.sampling.policies.UniformSimulationPolicy;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.reward.DepthRewardFunction;
import edu.cmu.sv.isstac.sampling.reward.RewardFunction;
import edu.cmu.sv.isstac.sampling.termination.NeverTerminateStrategy;
import edu.cmu.sv.isstac.sampling.termination.SampleSizeTerminationStrategy;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;

/**
 * @author Kasper Luckow
 *
 */
public class MonteCarloShell implements JPFShell {

  public static final String MC_CONF_PRFX = Options.SAMPLING_CONF_PREFIX + ".montecarlo";

  public static final String SIMULATION_POLICY = MC_CONF_PRFX + ".simulationpol";

  private final SamplingAnalysis samplingAnalysis;
  
  //ctor required for jpf shell
  public MonteCarloShell(Config config) throws ModelCounterCreationException,
      AnalysisCreationException {

    SimulationPolicy simulationPolicy = createSimulationPolicy(config);

    SamplingAnalysis.Builder samplingAnalysisBuilder = new SamplingAnalysis.Builder();

    this.samplingAnalysis = samplingAnalysisBuilder.build(config, new MonteCarloStrategy
        (simulationPolicy));
  }

  @Override
  public void start(String[] args) {
    samplingAnalysis.run();
  }

  private static SimulationPolicy createSimulationPolicy(Config conf)
      throws ModelCounterCreationException {
    if (conf.hasValue(SIMULATION_POLICY)) {
      return conf.getInstance(SIMULATION_POLICY, SimulationPolicy.class);
    }

    boolean useRandomSeed = conf.getBoolean(Options.RNG_RANDOM_SEED, Options.DEFAULT_RANDOM_SEED);

    SimulationPolicy simulationPolicy;
    if (useRandomSeed) {
      simulationPolicy = new UniformSimulationPolicy();
    } else {
      long seed = conf.getLong(Options.RNG_SEED, Options.DEFAULT_RNG_SEED);
      simulationPolicy = new UniformSimulationPolicy(seed);
    }
    return simulationPolicy;
  }

}
