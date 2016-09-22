package edu.cmu.sv.isstac.sampling;

import edu.cmu.sv.isstac.sampling.search.SamplingSearch;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

/**
 * @author Kasper Luckow
 */
public class JPFSamplerFactory {

  //This setting can be used to disable sampling to exhaustively explore the tree (mostly for debugging...)
  public static final String EXHAUSTIVE_ANALYSIS = Options.SAMPLING_CONF_PREFIX + ".exhaustive";

  public static JPF create(Config jpfConfig) {
    if(!jpfConfig.getBoolean(EXHAUSTIVE_ANALYSIS, false)) {
      //Substitute search object to use our sampler
      //There is no other way than using the string name of the class and rely
      //on the reflection in jpf-core...
      jpfConfig.setProperty("search.class", SamplingSearch.class.getName());
    }

    JPF jpf = new JPF(jpfConfig);

    return jpf;
  }
}
