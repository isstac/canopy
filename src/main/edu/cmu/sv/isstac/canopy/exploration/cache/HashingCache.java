/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
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

package edu.cmu.sv.isstac.canopy.exploration.cache;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.sv.isstac.canopy.exploration.Path;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
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

  @Override
  public boolean supportsPCOptimization() {
    //will fail on at least lawdb if pc optimization is set to true
    return false;
  }
}
