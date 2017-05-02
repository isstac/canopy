/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cmu.sv.isstac.sampling.exploration.cache;

import edu.cmu.sv.isstac.sampling.exploration.Trie;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Path;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class TrieCache implements StateCache {

  private Trie<Boolean> trie = new Trie<>();
  private int hits;
  private int misses;

  @Override
  public void add(VM vm) {
    int lastChoice = getLastChoiceOfPath(vm);
    trie.add(vm.getPath(), lastChoice, true);
  }

  @Override
  public boolean contains(VM vm) {
    Path p = vm.getPath();
    int lastChoice = getLastChoiceOfPath(vm);
    boolean hit = this.trie.contains(p, lastChoice);
    if(hit)
      hits++;
    else
      misses++;
    return hit;
  }
  //This is so disgusting, but it appears that the path object for some reason does not contain
  // the last choice of the final CG in the tree.
  // There are two solutions to fix this:
  // 1) Rely on Path from JPF which is efficient since it is a data structure that is maintained
  // during the search and just append the last choice to it
  // 2) build up a new path object from scratch by using the *last* cg obtained from the vm, and
  // then iteratively building the path by traversal up to the root cg. While this is a bit more
  // "clean", it also means that constructing the paths will be super costly for deep paths.
  //
  // The following solution uses 1) from above

  private int getLastChoiceOfPath(VM vm) {

    //BIG FAT WARNING:
    //This is in general UNSAFE to do,
    //because there is NO guarantee that choices are selected
    //incrementally! However, there does not seem to be another
    //way of obtaining a lightweight representation of the path
    //i.e. a sequence of decisions (represented by ints)
    //I think it is safe for ThreadChoiceFromSet (currently our only nondeterministic choice)
    //and PCChoiceGenerator
    ChoiceGenerator<?> lastCg = vm.getChoiceGenerator();
    return lastCg.getProcessedNumberOfChoices() - 1;
  }
}
