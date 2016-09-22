package edu.cmu.sv.isstac.sampling.exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 * 
 */
public class PruningChoicesStrategy implements ChoicesStrategy, PruningStrategy {
  
  private Set<Path> pruned = Sets.newHashSet();

  private static PruningChoicesStrategy instance;

  public static PruningChoicesStrategy getInstance() {
    if(instance == null) {
      instance = new PruningChoicesStrategy();
    }
    return instance;
  }

  private PruningChoicesStrategy() { }

  @Override
  public ArrayList<Integer> getEligibleChoices(ChoiceGenerator<?> cg) {
    Path p = new Path(cg.getPreviousChoiceGenerator());
    
    ArrayList<Integer> eligibleChoices = new ArrayList<>();
    for(int choice = 0; choice < cg.getTotalNumberOfChoices(); choice++) {
      p.addChoice(choice);
      if(!pruned.contains(p)) {
        eligibleChoices.add(choice);
      }
      p.removeLast();
    }
    
    return eligibleChoices;
  }

  @Override
  public boolean isPruned(Path p) {
    return pruned.contains(p);
  }

  public boolean isPruned(ChoiceGenerator<?> cg) {
    Path p = new Path(cg);
    return isPruned(p);
  }

  private static final Path root = new Path(); //empty path
  @Override
  public boolean isFullyPruned() {
    return isPruned(root);
  }

  @Override
  public void performPruning(ChoiceGenerator<?> cg) {
    Path p = new Path(cg);

    assert !pruned.contains(p);
    pruned.add(p);
    propagatePruning(p, cg);
  }
  
  private void propagatePruning(Path currentPath, ChoiceGenerator<?> currentCg) {

    ChoiceGenerator<?> backwardsPruningCg = currentCg;
    Path parent = new Path(currentPath);
    while(backwardsPruningCg != null) {
      Collection<Path> children = new HashSet<>();
      //make one step up in the tree
      parent.removeLast();
      
      for(int child = 0; child < backwardsPruningCg.getTotalNumberOfChoices(); child++) {
        Path siblingPath = new Path(parent);
        siblingPath.addChoice(child);
        if(!pruned.contains(siblingPath)) {
          return;
        }
        children.add(siblingPath);
      }

      //We only need to keep parent, because all subtrees (children) are pruned
      for(Path child : children) {
        pruned.remove(child);
      }
      pruned.add(parent.copy());
      backwardsPruningCg = backwardsPruningCg.getPreviousChoiceGenerator();
    }
  }

}
