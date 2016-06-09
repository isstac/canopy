package edu.cmu.sv.isstac.sampling.structure;

import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;

/**
 * @author Kasper Luckow
 *
 */
public class CGClassification {
  public static boolean isPCNode(ChoiceGenerator<?> choiceGenerator) {
    //More cases?
    return (choiceGenerator instanceof PCChoiceGenerator);
  }

  public static boolean isNondeterministicChoice(ChoiceGenerator<?> choiceGenerator) {
    //More cases?
    return (choiceGenerator instanceof ThreadChoiceFromSet);
  }
}
