package edu.cmu.sv.isstac.sampling;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.search.BacktrackingSamplingSearch;
import edu.cmu.sv.isstac.sampling.search.SamplingSearch;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Kasper Luckow
 */
public class JPFSamplerFactory implements JPFFactory {

  private static final Logger logger = JPFLogger.getLogger(JPFSamplerFactory.class.getName());

  @Override
  public JPF buildInstance(Config jpfConfig) {
    Class<?> samplingSearch;
    if(jpfConfig.getBoolean(Options.USE_BACKTRACKING_SEARCH,
        Options.DEFAULT_USE_BACKTRACKING_SEARCH)) {
      logger.info("Using backtracking-enabled sampling search");
      samplingSearch = BacktrackingSamplingSearch.class;
    } else {
      logger.info("Using sampling search without backtracking ability");
      samplingSearch = SamplingSearch.class;
    }

    //Substitute search object to use our sampler
    //There is no other way than using the string name of the class and rely
    //on the reflection in jpf-core...
    jpfConfig.setProperty("search.class", samplingSearch.getName());
    JPF jpf = new JPF(jpfConfig);
    return jpf;
  }
}
