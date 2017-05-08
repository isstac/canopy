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

import java.util.HashSet;
import java.util.Set;

import edu.cmu.sv.isstac.sampling.exploration.Path;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class HashingCache implements StateCache {
  private final Set<Path> stateCache;

  private long misses = 0;
  private long hits = 0;

  public HashingCache(int initialCapacity,
                      float loadFactor) {
    this.stateCache = new HashSet<>(initialCapacity, loadFactor);
  }

  public HashingCache() {
    this.stateCache = new HashSet<>();
  }

  public HashingCache(Set<Path> initCache) {
    this.stateCache = new HashSet<>(initCache);
  }

  @Override
  public void addState(VM vm) {
    PCChoiceGenerator[] pcs = vm.getChoiceGeneratorsOfType(PCChoiceGenerator.class);
    for (int i = pcs.length - 1; i >= 0; i--) {
      PCChoiceGenerator cg = pcs[i];

      //This could be expensive for long paths (i.e. many CGs)
      if (!stateCache.contains(cg)) {
        stateCache.add(new Path(cg));
      } else {
        // This is a small trick and an optimization. Note that we are adding the CGs to the
        // cache starting from the *end* of the path. If the path
        // of the current cg is in the cache, then, by definition, we must have added
        // any prefix of the CG to the cache as well, so we can break here
        break;
      }
    }
  }

  @Override
  public boolean isStateCached(VM vm) {
    // We shouldn't have to generate the path all the time, but unfortunately,
    // PCChoicegenerators do not have a unique id we can use. For each sample,
    // pcchoicegenerators are also replaced so we cannot check references.
    // The current way of caching could be very inefficient for deep paths, potentially defying
    // its purpose
    boolean hit = this.stateCache.contains(new Path(vm.getChoiceGenerator()));
    if (hit)
      hits++;
    else
      misses++;
    return hit;
  }
}
