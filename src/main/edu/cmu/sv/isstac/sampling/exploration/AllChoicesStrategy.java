package edu.cmu.sv.isstac.sampling.exploration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class AllChoicesStrategy implements ChoicesStrategy {

  private Set<Path> exploredPaths = new HashSet<>();

  @Override
  public ArrayList<Integer> getEligibleChoices(ChoiceGenerator<?> cg) {
    ArrayList<Integer> choices = new ArrayList<>();
    for(int i = 0; i < cg.getTotalNumberOfChoices(); i++)
      choices.add(i);
    return choices;
  }

  @Override
  public boolean hasTerminatedPathBeenExplored(Path path) {
    if(exploredPaths.contains(path)) {
      return true;
    } else {
      // We make some book keeping here to prevent the path from being explored again
      exploredPaths.add(path);
      return false;
    }
  }
}
