package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.SamplingShell;
import edu.cmu.sv.isstac.sampling.mcts.MCTSShell;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;

/**
 * @author Kasper Luckow
 */
public class MCTSExperiment implements Experiment {
  private static final int MAX_SAMPLES_NO_PRUNING = 2000;

  private final boolean pruning;
  private final boolean rewardAmplifcation;
  private final boolean weightedSimulation;

  public MCTSExperiment(boolean pruning, boolean rewardAmplification, boolean weightedSimulation) {
    this.pruning = pruning;
    this.rewardAmplifcation = rewardAmplification;
    this.weightedSimulation = weightedSimulation;
  }

  @Override
  public SamplingShell createShell(Config config, int seed) {
    config.setProperty(MCTSShell.PRUNING, Boolean.toString(this.pruning));
    if(!pruning) {
      config.setProperty(MCTSShell.MAX_SAMPLES_TERMINATION_STRAT, Integer
          .toString(MAX_SAMPLES_NO_PRUNING));
    }
    config.setProperty(MCTSShell.USE_MODELCOUNT_AMPLIFICATION,
        Boolean.toString(this.rewardAmplifcation));
    config.setProperty(MCTSShell.USE_MODELCOUNT_WEIGHTED_SIMULATION,
        Boolean.toString(this.weightedSimulation));

    config.setProperty(MCTSShell.SHOW_LIVE_STATISTICS, Boolean.toString(false));

    config.setProperty(MCTSShell.RNG_SEED, Integer.toString(seed));

    return new MCTSShell(config);
  }

  @Override
  public String getName() {
    return "MCTS[pruning=" + this.pruning + ";rewardAmp=" + this.rewardAmplifcation + ";" +
        "weightSim=" + this.weightedSimulation + "]";
  }
}
