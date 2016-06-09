package edu.cmu.sv.isstac.sampling.exploration;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public interface ChoicesStrategy {
  public ArrayList<Integer> getEligibleChoices(ChoiceGenerator<?> cg);
}
