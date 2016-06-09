package edu.cmu.sv.isstac.sampling.exploration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;

import edu.cmu.sv.isstac.sampling.structure.Node;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 * We can optimize this pruner if performance is sluggish. This is a quick and (likely very) slow implementation
 */
public class PruningChoicesStrategy extends PropertyListenerAdapter implements ChoicesStrategy {
  
  private Set<Path> pruned = Sets.newHashSet();
  
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
  
  public boolean isPruned(Node n) {
    Path p = new Path(n);
    return pruned.contains(p);
  }
  
  @Override
  public void searchConstraintHit(Search search) {
    ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
    performPruning(cg);
  }
  
  @Override
  public void stateAdvanced(Search search) {
    if(search.isEndState()) {
      ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
      performPruning(cg);
    }
  }

  @Override
  public void exceptionThrown(VM vm, ThreadInfo currentThread, ElementInfo thrownException) {
    ChoiceGenerator<?> cg = vm.getChoiceGenerator();
    performPruning(cg);
  }
  
  private void performPruning(ChoiceGenerator<?> cg) {
    Path p = new Path(cg);

    assert !pruned.contains(p);
    pruned.add(p);
    propagatePruning(p, cg);
  }
  
  private void propagatePruning(Path currentPath, ChoiceGenerator<?> currentCg) {
    //This is really expensive :/
    ChoiceGenerator<?> backwardsPruningCg = currentCg;
    Path parent = new Path(currentPath);
    while(backwardsPruningCg != null) {
      Collection<Path> siblings = new HashSet<>();
      siblings.add(parent);
      int curChoice = parent.removeLast();

      
      for(int child = 0; child < backwardsPruningCg.getTotalNumberOfChoices(); child++) {
        if(child == curChoice)
          continue;
        Path siblingPath = new Path(parent);
        siblingPath.addChoice(child);
        if(!pruned.contains(siblingPath)) {
          return;
        }
        siblings.add(siblingPath);
      }

      //We only need to keep parent, because all subtrees (children) are pruned
      for(Path sibling : siblings) {
        pruned.remove(sibling);
      }
      pruned.add(parent);
      backwardsPruningCg = backwardsPruningCg.getPreviousChoiceGenerator();
    }
  }

}
