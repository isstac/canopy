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

package edu.cmu.sv.isstac.sampling.mcts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeAdapter;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class MCTSNode extends NodeAdapter {
  private static final Logger logger = JPFLogger.getLogger(MCTSNode.class.getName());

  //This flag is used to signal that the node (associated with the cg) is not part of the search
  // tree in mcts.
  private boolean isSearchTreeNode = false;

  public MCTSNode(Node parent, ChoiceGenerator<?> cg, int choice) {
    super(parent, cg, choice);
  }

  public boolean isSearchTreeNode() {
    return isSearchTreeNode;
  }

  public void setIsSearchTreeNode(boolean isSearchTreeNode) {
    this.isSearchTreeNode = isSearchTreeNode;
  }
}
