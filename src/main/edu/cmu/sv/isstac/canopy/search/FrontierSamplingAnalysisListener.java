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

package edu.cmu.sv.isstac.canopy.search;

import java.util.Collection;

import edu.cmu.sv.isstac.canopy.AnalysisStrategy;
import edu.cmu.sv.isstac.canopy.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.canopy.exploration.ChoicesStrategy;
import edu.cmu.sv.isstac.canopy.exploration.Path;
import edu.cmu.sv.isstac.canopy.exploration.cache.StateCache;
import edu.cmu.sv.isstac.canopy.quantification.PathQuantifier;
import edu.cmu.sv.isstac.canopy.reward.RewardFunction;
import edu.cmu.sv.isstac.canopy.termination.TerminationStrategy;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 * Decorates samplinganalysislistener with the capabilities of enforcing exploration down to a
 * frontier node
 */
public class FrontierSamplingAnalysisListener extends SamplingAnalysisListener {
  private final Path frontierNode;
  private final int frontierLength;

  public FrontierSamplingAnalysisListener(AnalysisStrategy analysisStrategy, RewardFunction rewardFunction,
                                          PathQuantifier pathQuantifier,
                                          TerminationStrategy terminationStrategy,
                                          ChoicesStrategy choicesStrategy,
                                          StateCache stateCache,
                                          Collection<AnalysisEventObserver> observers,
                                          Path frontierNode) {
    super(analysisStrategy, rewardFunction, pathQuantifier, terminationStrategy, choicesStrategy,
        stateCache, observers);
    this.frontierNode = frontierNode;
    this.frontierLength = frontierNode.length();
  }

  @Override
  public final void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
    //This could be a bit costly
    Path curr = new Path(cg);
    int currLength = curr.length();
    //If the current path is
    if(currLength <= this.frontierLength) {
      int idx = currLength - 1;
      int choice = this.frontierNode.getChoice(idx);
      cg.select(choice);

    } else {
      //If we have passed the frontier, rely on how the sampling listener makes choices, e.g.,
      //using mcts
      super.choiceGeneratorAdvanced(vm, cg);
    }
  }
}
