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

package edu.cmu.sv.isstac.sampling.search.cache;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.sv.isstac.sampling.exploration.Path;
import gov.nasa.jpf.vm.ChoiceGenerator;

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

  @Override
  public void add(ChoiceGenerator<?> cg) {
    this.stateCache.add(new Path(cg));
  }

  @Override
  public boolean contains(ChoiceGenerator<?> cg) {
    // We shouldn't have to generate the path all the time, but unfortunately,
    // PCChoicegenerators do not have a unique id we can use. For each sample,
    // pcchoicegenerators are also replaced so we cannot check references.
    // The current way of caching could be very inefficient for deep paths, potentially defying
    // its purpose
    boolean hit = this.stateCache.contains(new Path(cg));
    if(hit)
      hits++;
    else
      misses++;
    return hit;
  }
}
