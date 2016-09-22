package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.SamplingShell;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;

/**
 * @author Kasper Luckow
 */
public interface Experiment {
  public SamplingShell createShell(Config config, int seed);
  public String getName();
}
