package edu.cmu.sv.isstac.sampling.search;

import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 */
public interface SamplingListener {
  void newSampleStarted(Search samplingSearch);
}
