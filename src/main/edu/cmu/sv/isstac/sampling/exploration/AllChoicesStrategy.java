package edu.cmu.sv.isstac.sampling.exploration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class AllChoicesStrategy implements ChoicesStrategy {

  private static final Logger logger = JPFLogger.getLogger(AllChoicesStrategy.class.getName());

  private Trie<Boolean> exploredPaths = new Trie<>();

  @Override
  public ArrayList<Integer> getEligibleChoices(gov.nasa.jpf.vm.Path path, ChoiceGenerator<?> cg) {
    ArrayList<Integer> choices = new ArrayList<>();
    for(int i = 0; i < cg.getTotalNumberOfChoices(); i++)
      choices.add(i);
    return choices;
  }

  @Override
  public boolean hasTerminatedPathBeenExplored(gov.nasa.jpf.vm.Path path, ChoiceGenerator<?> cg) {
    int lastChoice = cg.getProcessedNumberOfChoices() - 1;

    if(exploredPaths.contains(path, lastChoice)) {
      return true;
    } else {
      // We make some book keeping here to prevent the path from being explored again
      exploredPaths.put(path, lastChoice, Boolean.TRUE);
      return false;
    }
  }
}
