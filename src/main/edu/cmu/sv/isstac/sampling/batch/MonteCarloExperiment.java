package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.montecarlo.MonteCarloShell;
import edu.cmu.sv.isstac.sampling.montecarlo.MonteCarloStrategy;
import edu.cmu.sv.isstac.sampling.montecarlo.Utils;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class MonteCarloExperiment implements Experiment {

  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed) throws BatchProcessorException {
    config.setProperty(Options.RNG_SEED, Integer.toString(seed));

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
