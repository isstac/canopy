package edu.cmu.sv.isstac.sampling;

import java.util.ArrayList;

import edu.cmu.sv.isstac.sampling.search.TerminationType;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public interface AnalysisStrategy {
  void makeStateChoice(VM vm, ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices);
  void newSampleStarted(Search samplingSearch);
  void pathTerminated(TerminationType termType, long reward, long pathVolume,
                      long amplifiedReward, Search searchState, boolean hasBeenExploredBefore);
}
