package edu.cmu.sv.isstac.sampling.exploration;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class AllChoicesStrategy implements ChoicesStrategy {

  @Override
  public ArrayList<Integer> getEligibleChoices(ChoiceGenerator<?> cg) {
    ArrayList<Integer> choices = new ArrayList<>();
    for(int i = 0; i < cg.getTotalNumberOfChoices(); i++)
      choices.add(i);
    return choices;
  }
}
