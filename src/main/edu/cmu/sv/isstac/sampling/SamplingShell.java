package edu.cmu.sv.isstac.sampling;

import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.termination.TerminationStrategy;
import gov.nasa.jpf.JPFShell;

/**
 * @author Kasper Luckow
 * TODO: this is super ugly. Get rid of this interface
 */
public interface SamplingShell extends JPFShell {
  public void addEventObserver(AnalysisEventObserver eventObserver);

  public void setTerminationStrategy(TerminationStrategy terminationStrategy);
}
