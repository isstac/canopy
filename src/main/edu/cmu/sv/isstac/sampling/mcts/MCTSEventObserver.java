package edu.cmu.sv.isstac.sampling.mcts;

import edu.cmu.sv.isstac.sampling.SamplingResult;

/**
 * @author Kasper Luckow
 *
 */
public interface MCTSEventObserver {
  public void mctsAnalysisDone(SamplingResult result);
}
