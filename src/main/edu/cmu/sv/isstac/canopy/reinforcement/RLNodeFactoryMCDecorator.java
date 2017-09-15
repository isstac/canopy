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

package edu.cmu.sv.isstac.canopy.reinforcement;

import edu.cmu.sv.isstac.canopy.quantification.SPFModelCounter;
import edu.cmu.sv.isstac.canopy.structure.NodeCreationException;
import edu.cmu.sv.isstac.canopy.structure.NodeFactory;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import modelcounting.analysis.exceptions.AnalysisException;

/**
 * @author Kasper Luckow
 * Decorates all RL nodes with subdomain (mc) computation.
 * TODO: Don't like the use of the type parameter here. Should be fixed after experiments
 */
public class RLNodeFactoryMCDecorator implements NodeFactory<RLNode> {

  private final RLNodeFactory decoratee = new RLNodeFactory();
  private final SPFModelCounter modelCounter;

  public RLNodeFactoryMCDecorator(SPFModelCounter modelCounter) {
    this.modelCounter = modelCounter;
  }

  @Override
  public RLNode create(RLNode parent, ChoiceGenerator<?> currentCG, int choice)
      throws NodeCreationException {
    if(currentCG != null) {
      PCChoiceGenerator pccg = currentCG.
          getPreviousChoiceGeneratorOfType(PCChoiceGenerator.class);
      PathCondition pc;
      if (pccg != null) {
        pc = pccg.getCurrentPC();
      } else {
        pc = new PathCondition();
      }
      long subdomainSize;
      try {
        subdomainSize = this.modelCounter.countPointsOfPC(pc).longValue();
      } catch (AnalysisException e) {
        throw new NodeCreationException(e);
      }
      return new MCRLNode(parent, currentCG, choice, subdomainSize);
    } else {
      // This should only happen when the node we are creating is a final node
      // in which case the domain size does not matter anyway
      return new MCRLNode(parent, currentCG, choice, -1);
    }
  }

  @Override
  public boolean isSupportedChoiceGenerator(ChoiceGenerator<?> cg) {
    return this.decoratee.isSupportedChoiceGenerator(cg);
  }
}
