package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.exploration.AllChoicesStrategy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.reinforcement.RLNode;
import edu.cmu.sv.isstac.sampling.reinforcement.RLNodeFactory;
import edu.cmu.sv.isstac.sampling.reinforcement.RLNodeFactoryMCDecorator;
import edu.cmu.sv.isstac.sampling.reinforcement.ReinforcementLearningStrategy;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class RLExperiment implements Experiment {

  private final boolean pruning;
  private final boolean rewardAmplifcation;
  private final boolean modelCountSubDomain;
  private final int samplesPerOptimization;
  private final double epsilon;
  private final double historyWeight;

  public RLExperiment(boolean pruning, boolean rewardAmplification, boolean modelCountSubDomain,
                      int samplesPerOptimization,
                      double epsilon, double historyWeight) {
    this.pruning = pruning;
    this.rewardAmplifcation = rewardAmplification;
    this.modelCountSubDomain = modelCountSubDomain;
    this.samplesPerOptimization = samplesPerOptimization;
    this.epsilon = epsilon;
    this.historyWeight = historyWeight;
  }

  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed) {
    if(!pruning) {
      config.setProperty(Options.CHOICES_STRATEGY, AllChoicesStrategy.class.getName());
    }

    config.setProperty(Options.USE_MODELCOUNT_AMPLIFICATION,
        Boolean.toString(this.rewardAmplifcation));
    config.setProperty(Options.RNG_SEED, Integer.toString(seed));

    NodeFactory<RLNode> factory;

    if(modelCountSubDomain) {
      try {
        SPFModelCounter modelCounter = ModelCounterFactory.getInstance(config);
        factory = new RLNodeFactory();
      } catch (ModelCounterCreationException e) {
        throw new BatchProcessorException(e);
      }
    } else {
      factory = new RLNodeFactory();
    }

    return new ReinforcementLearningStrategy(samplesPerOptimization, epsilon, historyWeight,
        factory, seed);

  }

  @Override
  public String getName() {
    return "RL[" +
        "pruning=" + this.pruning + ";" +
        "rewardAmp=" + this.rewardAmplifcation + ";" +
        "modelCountSubDomain=" + this.modelCountSubDomain + ";" +
        "samplesPerOptimization=" + this.samplesPerOptimization + ";" +
        "epsilon=" + this.epsilon + ";" +
        "history=" + this.historyWeight + "]";
  }
}
