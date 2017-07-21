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

package edu.cmu.sv.isstac.sampling.search;

import org.apache.commons.collections15.map.HashedMap;

import java.util.Map;

import edu.cmu.sv.isstac.sampling.search.TerminationType;
import edu.cmu.sv.isstac.sampling.structure.Node;

/**
 * @author Kasper Luckow
 */
public class BackPropagator {

  @FunctionalInterface
  private interface RewardUpdater {
    void update(Node n, long reward);
  }

  private static final Map<TerminationType, RewardUpdater> rewardUpdaters = new HashedMap<>();
  static {
    rewardUpdaters.put(TerminationType.SUCCESS, (n, reward) -> n.getReward().incrementSucc(reward));
    rewardUpdaters.put(TerminationType.ERROR, (n, reward) -> n.getReward().incrementFail(reward));
    rewardUpdaters.put(TerminationType.CONSTRAINT_HIT, (n, reward) -> n.getReward().incrementGrey(reward));
  }

  public static void cumulativeRewardPropagation(Node node, long reward, long pathVolume,
                                                 TerminationType termType) {
    RewardUpdater updater = rewardUpdaters.get(termType);
    // Perform backup phase
    for(Node n = node; n != null; n = n.getParent()) {
      updater.update(n, reward);
      n.incVisitedNum(pathVolume);
    }
  }
}
