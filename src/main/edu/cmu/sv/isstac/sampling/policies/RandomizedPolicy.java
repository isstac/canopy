package edu.cmu.sv.isstac.sampling.policies;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class RandomizedPolicy implements SimulationPolicy {

  private final Random rng;
  
  public RandomizedPolicy(long seed) {
    rng = new Random(seed);
  }

  public RandomizedPolicy() {
    rng = new Random();    
  }
  
  @Override
  public int selectChoice(ChoiceGenerator<?> cg, ArrayList<Integer> eligibleChoices) {
    int idx = rng.nextInt(eligibleChoices.size());
    return eligibleChoices.get(idx);
  }
}
