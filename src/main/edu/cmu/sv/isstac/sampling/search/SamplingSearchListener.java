package edu.cmu.sv.isstac.sampling.search;

import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 */
public interface SamplingSearchListener {
  void newSampleStarted(Search samplingSearch);
  void sampleTerminated(Search samplingSearch);
}
