package edu.cmu.sv.isstac.sampling.exploration;

import java.util.ArrayList;
import java.util.logging.Logger;

import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class AllChoicesStrategy implements ChoicesStrategy {

  private static final Logger logger = JPFLogger.getLogger(AllChoicesStrategy.class.getName());

  private Trie exploredPaths = new Trie();

  @Override
  public ArrayList<Integer> getEligibleChoices(gov.nasa.jpf.vm.Path path, ChoiceGenerator<?> cg) {
    ArrayList<Integer> choices = new ArrayList<>();
    for(int i = 0; i < cg.getTotalNumberOfChoices(); i++)
      choices.add(i);
    return choices;
  }

  @Override
  public boolean hasTerminatedPathBeenExplored(gov.nasa.jpf.vm.Path path, ChoiceGenerator<?> cg) {
    //int lastChoice = cg.getProcessedNumberOfChoices() - 1;

    if(exploredPaths.contains(path)) {
      return true;
    } else {
      // We make some book keeping here to prevent the path from being explored again
      exploredPaths.setFlag(path, true);
      return false;
    }
  }
}
