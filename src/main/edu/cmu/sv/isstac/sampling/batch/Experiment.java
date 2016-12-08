package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.JPFFactory;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public interface Experiment {
  AnalysisStrategy createAnalysisStrategy(Config config, int seed) throws BatchProcessorException;
  JPFFactory getJPFFactory();
  String getName();
}
