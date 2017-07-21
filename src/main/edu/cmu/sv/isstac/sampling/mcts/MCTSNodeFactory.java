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

package edu.cmu.sv.isstac.sampling.mcts;

import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.structure.FinalNode;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.NodeFactory;
import edu.cmu.sv.isstac.sampling.structure.NondeterministicNode;
import edu.cmu.sv.isstac.sampling.structure.PCNode;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;

import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isNondeterministicChoice;
import static edu.cmu.sv.isstac.sampling.structure.CGClassification.isPCNode;

/**
 * @author Kasper Luckow
 *
 */
public class MCTSNodeFactory implements NodeFactory<MCTSNode> {
  private static final Logger logger = JPFLogger.getLogger(MCTSNodeFactory.class.getName());
  
  @Override
  public MCTSNode create(MCTSNode parent, ChoiceGenerator<?> currentCG, int choice) {
    //If there is a shadow node already created, we return it here
    if(parent != null && parent.hasChildForChoice(choice)) {
      MCTSNode nxt = (MCTSNode)parent.getChild(choice);
      return nxt;
    }

    MCTSNode newNode = null;
    if(currentCG == null)
      newNode = new MCTSNode(parent, null, choice);
    else if(isSupportedChoiceGenerator(currentCG))
      newNode = new MCTSNode(parent, currentCG, choice);
    else {
      String msg = "Cannot create node for choicegenerators of type " + currentCG.getClass().getName();
      logger.severe(msg);
      throw new IllegalStateException(msg);
    }
    if(parent != null)
      parent.addChild(newNode);
    return newNode;
  }
  
  @Override
  public boolean isSupportedChoiceGenerator(ChoiceGenerator<?> cg) {
    return isPCNode(cg) || isNondeterministicChoice(cg);
  }
}
