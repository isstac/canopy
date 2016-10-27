package edu.cmu.sv.isstac.sampling.batch;

import java.text.DecimalFormat;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.exploration.AllChoicesStrategy;
import edu.cmu.sv.isstac.sampling.mcts.MCTSStrategy;
import edu.cmu.sv.isstac.sampling.mcts.SelectionPolicy;
import edu.cmu.sv.isstac.sampling.mcts.Utils;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class MCTSExperiment implements Experiment {
  private static final int MAX_SAMPLES_NO_PRUNING = 2000;

  //TODO: pruning is currently always enabled. Make support for the non-pruning case
  private final boolean pruning;
  private final boolean rewardAmplifcation;
  private final boolean weightedSimulation;
  private final double biasparameter;

  public MCTSExperiment(boolean pruning, boolean rewardAmplification, boolean weightedSimulation,
   double biasparameter) {
    this.pruning = pruning;
    this.rewardAmplifcation = rewardAmplification;
    this.weightedSimulation = weightedSimulation;
    this.biasparameter = biasparameter;
  }

  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed) {
    if(!pruning) {
      config.setProperty(Options.CHOICES_STRATEGY, AllChoicesStrategy.class.getName());
    }

    config.setProperty(Options.USE_MODELCOUNT_AMPLIFICATION,
        Boolean.toString(this.rewardAmplifcation));
    config.setProperty(Utils.USE_MODELCOUNT_WEIGHTED_SIMULATION,
        Boolean.toString(this.weightedSimulation));

    config.setProperty(Utils.UCT_BIAS, Double.toString(biasparameter));

    config.setProperty(Options.RNG_SEED, Integer.toString(seed));

    try {
      SimulationPolicy simPol = Utils.createSimulationPolicy(config);
      SelectionPolicy selPol = Utils.createSelectionPolicy(config);

      return new MCTSStrategy(selPol, simPol);
    } catch(ModelCounterCreationException e) {
      throw new BatchProcessorException(e);
    }
  }

  @Override
  public String getName() {

    return "MCTS[pruning=" + this.pruning + ";rewardAmp=" + this.rewardAmplifcation + ";" +
        "weightSim=" + this.weightedSimulation + ";bias=" + new DecimalFormat("#.##").format(this
        .biasparameter) + "]";
  }
}
