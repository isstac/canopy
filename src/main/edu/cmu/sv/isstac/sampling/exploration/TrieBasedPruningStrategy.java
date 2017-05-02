/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.cmu.sv.isstac.sampling.exploration;

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 * 
 */
public class TrieBasedPruningStrategy implements ChoicesStrategy, PruningStrategy {

  private Set<Path> pruned = Sets.newHashSet();

  private static TrieBasedPruningStrategy instance;

  public static TrieBasedPruningStrategy getInstance() {
    if(instance == null) {
      instance = new TrieBasedPruningStrategy();
    }
    return instance;
  }

  private TrieBasedPruningStrategy() { }

  public void reset() {
    this.pruned.clear();
  }

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
  public boolean hasTerminatedPathBeenExplored(Path path) {
    // We just return false here, because, by construction, a path can never be explored multiple
    // times when using pruning. The "safe" way of doing this would be to actually consult the
    // pruned set, but we would potentially have to check all path prefixes, because, in its
    // current implementation, we only keep the prefix path in the set when the subtree (i.e. all
    // suffixes of that prefix) are pruned. In other words, it is slightly more efficient to
    // return false here
    return false;
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

      //If backtracking is not used, we can optimize how pruning information is kept:
      // We only need to keep parent, because all subtrees (children) are pruned.
      // *However*, when backtracking is used we can end up in a state where parents are pruned
      // but we are currently asking for whether some other cg in a subtree of a pruned node has
      // non-pruned children. In that case, if we removed this information using the code below,
      // the backtracker would incorrectly select a choice that actually was pruned.
//      for(Path child : children) {
//        pruned.remove(child);
//      }
      pruned.add(parent.copy());
      backwardsPruningCg = backwardsPruningCg.getPreviousChoiceGenerator();
    }
  }

}
