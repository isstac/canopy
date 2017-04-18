/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
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

import edu.cmu.sv.isstac.sampling.exploration.Path;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 * Delegates everying to the sampling listener being wrapped. However, it ensures that the
 * analysis does not start before reaching the frontier node
 */
public class FrontierDecoratorListener extends PropertyListenerAdapter implements SamplingListener {
  private final SamplingAnalysisListener samplingListener;
  private final Path frontierNode;
  private final int frontierLength;

  public FrontierDecoratorListener(SamplingAnalysisListener samplingListener, Path frontierNode) {
    this.samplingListener = samplingListener;
    this.frontierNode = frontierNode;
    this.frontierLength = frontierNode.length();
  }

  @Override
  public final void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> cg) {
    //This is a weird condition, I know
    Path curr = new Path(cg);
    int currLength = curr.length();
    if(currLength <= this.frontierLength) {
      int idx = currLength;
      int choice = this.frontierNode.getChoice(idx);
      cg.select(choice);

    } else {
      //If we have passed the frontier, rely on how the sampling listener makes choices, e.g.,
      //using mcts
      samplingListener.choiceGeneratorAdvanced(vm, cg);
    }
  }

  @Override
  public void searchStarted(Search search) {
    samplingListener.searchStarted(search);
  }

  @Override
  public void searchFinished(Search search) {
    this.samplingListener.searchFinished(search);
  }

  @Override
  public void newSampleStarted(Search samplingSearch) {
    this.samplingListener.newSampleStarted(samplingSearch);
  }

  @Override
  public void stateAdvanced(Search search) {
    this.samplingListener.stateAdvanced(search);
  }

  @Override
  public void exceptionThrown(VM vm, ThreadInfo currentThread, ElementInfo thrownException) {
    this.samplingListener.exceptionThrown(vm, currentThread, thrownException);
  }

  @Override
  public void searchConstraintHit(Search search) {
    this.samplingListener.searchConstraintHit(search);
  }
}
