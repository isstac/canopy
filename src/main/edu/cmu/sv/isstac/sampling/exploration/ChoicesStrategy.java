package edu.cmu.sv.isstac.sampling.exploration;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.vm.*;

/**
 * @author Kasper Luckow
 *
 */
public interface ChoicesStrategy {
  public ArrayList<Integer> getEligibleChoices(gov.nasa.jpf.vm.Path path, ChoiceGenerator<?> cg);
  public boolean hasTerminatedPathBeenExplored(gov.nasa.jpf.vm.Path path, ChoiceGenerator<?> cg);
}
