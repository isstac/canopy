package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.SamplingShell;
import edu.cmu.sv.isstac.sampling.mcts.MCTSShell;
import edu.cmu.sv.isstac.sampling.montecarlo.MonteCarloShell;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class MonteCarloExperiment implements Experiment {

  private static final int MAX_SAMPLES_NO_PRUNING = 2000;

  private final boolean pruning;

  public MonteCarloExperiment(boolean pruning) {
    this.pruning = pruning;
  }

  @Override
  public SamplingShell createShell(Config config, int seed) {
    config.setProperty(MonteCarloShell.PRUNING, Boolean.toString(this.pruning));
    if(!pruning) {
      config.setProperty(MonteCarloShell.MAX_SAMPLES_TERMINATION_STRAT, Integer
          .toString(MAX_SAMPLES_NO_PRUNING));
    }

    config.setProperty(MonteCarloShell.RNG_SEED, Integer.toString(seed));

    return new MonteCarloShell(config);
  }

  @Override
  public String getName() {
    return "MonteCarlo[pruning=" + this.pruning + "]";
  }
}
