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

package edu.cmu.sv.isstac.canopy.mcts;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.canopy.structure.Node;
import edu.cmu.sv.isstac.canopy.structure.NodeAdapter;
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
