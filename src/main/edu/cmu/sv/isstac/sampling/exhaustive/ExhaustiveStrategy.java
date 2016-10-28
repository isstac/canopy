package edu.cmu.sv.isstac.sampling.exhaustive;

import java.util.ArrayList;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.search.TerminationType;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class ExhaustiveStrategy implements AnalysisStrategy {
  @Override
  public void makeStateChoice(VM vm, ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices) { }

  @Override
  public void newSampleStarted(Search samplingSearch) { }

  @Override
  public void pathTerminated(TerminationType termType, long reward, long pathVolume, long
      amplifiedReward, Search searchState, boolean hasBeenExploredBefore) { }
}
