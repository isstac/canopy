package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.exploration.AllChoicesStrategy;
import edu.cmu.sv.isstac.sampling.montecarlo.MonteCarloShell;
import edu.cmu.sv.isstac.sampling.montecarlo.MonteCarloStrategy;
import edu.cmu.sv.isstac.sampling.montecarlo.Utils;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class MonteCarloExperiment implements Experiment {

  private final boolean pruning;

  public MonteCarloExperiment(boolean pruning) {
    this.pruning = pruning;
  }

  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed) throws BatchProcessorException {
    if(!pruning) {
      config.setProperty(Options.CHOICES_STRATEGY, AllChoicesStrategy.class.getName());
    }

    config.setProperty(Options.RNG_SEED, Integer.toString(seed));

    //Never use model counting with monte carlo
    config.setProperty(Options.USE_MODELCOUNT_AMPLIFICATION, Boolean.toString(false));

    try {
      return new MonteCarloStrategy(Utils.createSimulationPolicy(config));
    } catch (ModelCounterCreationException e) {
      throw new BatchProcessorException(e);
    }
  }

  @Override
  public String getName() {
    return "MonteCarlo";
  }
}
