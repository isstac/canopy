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

package edu.cmu.sv.isstac.sampling.structure;

import java.util.logging.Logger;

import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import static edu.cmu.sv.isstac.sampling.structure.CGClassification.*;

/**
 * @author Kasper Luckow
 *
 */
public class DefaultNodeFactory implements NodeFactory<Node> {
  private static final Logger logger = JPFLogger.getLogger(DefaultNodeFactory.class.getName());
  
  @Override
  public Node create(Node parent, ChoiceGenerator<?> currentCG, int choice) {
    Node newNode = null;
    if(currentCG == null)
      newNode = new FinalNode(parent, choice);
    else if(isPCNode(currentCG))
      newNode = new PCNode(parent, currentCG, choice);
    else if(isNondeterministicChoice(currentCG))
      newNode = new NondeterministicNode(parent, currentCG, choice);
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
