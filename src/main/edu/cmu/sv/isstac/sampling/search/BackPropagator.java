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
