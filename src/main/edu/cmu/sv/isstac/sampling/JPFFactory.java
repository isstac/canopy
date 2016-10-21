package edu.cmu.sv.isstac.sampling;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

/**
 * @author Kasper Luckow
 */
public interface JPFFactory {
  public JPF buildInstance(Config jpfConfig);
}
