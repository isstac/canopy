package edu.cmu.sv.isstac.sampling;

import edu.cmu.sv.isstac.sampling.search.SamplingSearch;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

/**
 * @author Kasper Luckow
 */
public class JPFSamplerFactory implements JPFFactory {

  @Override
  public JPF buildInstance(Config jpfConfig) {
    //Substitute search object to use our sampler
    //There is no other way than using the string name of the class and rely
    //on the reflection in jpf-core...
    jpfConfig.setProperty("search.class", SamplingSearch.class.getName());
    JPF jpf = new JPF(jpfConfig);
    return jpf;
  }
}
