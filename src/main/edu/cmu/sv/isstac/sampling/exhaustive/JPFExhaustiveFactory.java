package edu.cmu.sv.isstac.sampling.exhaustive;

import edu.cmu.sv.isstac.sampling.JPFFactory;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.DFSearch;
import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 */
public class JPFExhaustiveFactory implements JPFFactory {

  private final Class<? extends Search> searchClass;

  public JPFExhaustiveFactory(Class<? extends Search> searchClass) {
    this.searchClass = searchClass;
  }

  public JPFExhaustiveFactory() {
    this(DFSearch.class);
  }

  @Override
  public JPF buildInstance(Config jpfConfig) {
    //Substitute search object to use our sampler
    //There is no other way than using the string name of the class and rely
    //on the reflection in jpf-core...
    jpfConfig.setProperty("search.class", this.searchClass.getName());
    JPF jpf = new JPF(jpfConfig);
    return jpf;
  }
}
