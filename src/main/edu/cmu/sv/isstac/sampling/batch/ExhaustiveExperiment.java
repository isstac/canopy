package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.JPFFactory;
import edu.cmu.sv.isstac.sampling.JPFSamplerFactory;
import edu.cmu.sv.isstac.sampling.Options;
import edu.cmu.sv.isstac.sampling.exhaustive.ExhaustiveStrategy;
import edu.cmu.sv.isstac.sampling.exhaustive.JPFExhaustiveFactory;
import edu.cmu.sv.isstac.sampling.exploration.AllChoicesStrategy;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterCreationException;
import edu.cmu.sv.isstac.sampling.quantification.ModelCounterFactory;
import edu.cmu.sv.isstac.sampling.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.sampling.reinforcement.ReinforcementLearningStrategy;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class ExhaustiveExperiment implements Experiment {

  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed) {
    return new ExhaustiveStrategy();
  }

  @Override
  public JPFFactory getJPFFactory() {
    return new JPFExhaustiveFactory();
  }

  @Override
  public String getName() {
    return "Exhaustive";
  }
}
