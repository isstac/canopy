package edu.cmu.sv.isstac.sampling.analysis;

import edu.cmu.sv.isstac.sampling.mcts.MCTSNode;
import edu.cmu.sv.isstac.sampling.structure.Node;
import gov.nasa.jpf.search.Search;

/**
 * @author Kasper Luckow
 * I really don't like this. Just need to have it now. Refactor when time permits
 * Basically, this interface overloads the sampleDone event and also provides the node of the last
 * node in the MCTS tree... yes, ugly
 */
public interface MCTSEventObserver {

  public void sampleDone(MCTSNode lastNode);
}
