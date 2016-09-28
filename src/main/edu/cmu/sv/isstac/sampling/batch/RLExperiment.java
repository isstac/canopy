package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.policies.SimulationPolicy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.reinforcement.ReinforcementLearningStrategy;
import edu.cmu.sv.isstac.sampling.reinforcement.Utils;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class RLExperiment implements Experiment {

  private final boolean pruning;
  private final boolean rewardAmplifcation;
  private final int samplesPerOptimization;
  private final double epsilon;
  private final double historyWeight;

  public RLExperiment(boolean pruning, boolean rewardAmplification, int samplesPerOptimization,
                      double epsilon, double historyWeight) {
    this.pruning = pruning;
    this.rewardAmplifcation = rewardAmplification;
    this.samplesPerOptimization = samplesPerOptimization;
    this.epsilon = epsilon;
    this.historyWeight = historyWeight;
  }

  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed) {
    config.setProperty(Options.USE_MODELCOUNT_AMPLIFICATION,
        Boolean.toString(this.rewardAmplifcation));
    config.setProperty(Options.RNG_SEED, Integer.toString(seed));

    try {
      SPFModelCounter modelCounter = ModelCounterFactory.getInstance(config);
      return new ReinforcementLearningStrategy(samplesPerOptimization, epsilon, historyWeight,
          modelCounter, seed);
    } catch (ModelCounterCreationException e) {
      throw new BatchProcessorException(e);
    }
  }

  @Override
  public String getName() {
    return "RL[" +
        "pruning=" + this.pruning + ";" +
        "rewardAmp=" + this.rewardAmplifcation + ";" +
        "samplesPerOptimization=" + this.samplesPerOptimization + ";" +
        "epsilon=" + this.epsilon + ";" +
        "history=" + this.historyWeight + "]";
  }
}
