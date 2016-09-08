package edu.cmu.sv.isstac.sampling.policies;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public interface SimulationPolicy {
  public int selectChoice(VM vm, ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices);
}
