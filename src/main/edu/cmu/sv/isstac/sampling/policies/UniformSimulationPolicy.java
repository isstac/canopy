package edu.cmu.sv.isstac.sampling.policies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class UniformSimulationPolicy implements SimulationPolicy {

  private final Random rng;
  
  public UniformSimulationPolicy(long seed) {
    rng = new Random(seed);
  }
  
  @Override
  public int selectChoice(VM vm, ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices) {
    int idx = rng.nextInt(eligibleChoices.size());
    return eligibleChoices.get(idx);
  }
}
