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

package edu.cmu.sv.isstac.sampling.reward;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import edu.cmu.sv.isstac.sampling.search.SamplingListener;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class AllocationRewardFunction extends PropertyListenerAdapter implements RewardFunction,
    SamplingListener {

  private static final Logger LOGGER = JPFLogger.getLogger(AllocationRewardFunction.class.getName());

  private long cost = -4000;
  private boolean started = false;
  private Map<ChoiceGenerator<?>, Long> costMap = new HashMap<>();

  @Override
  public long computeReward(VM vm) {
    return cost;
  }

  @Override
  public void objectCreated(VM vm, ThreadInfo ti, ElementInfo ei) {
    if(started)
      this.cost += ei.getHeapSize();
  }

  @Override
  public void newSampleStarted(Search samplingSearch) {
    started = false;
    cost = 0;
    costMap.clear();
    //readFormation
  }

  @Override
  public void methodEntered(VM vm, ThreadInfo currentThread, MethodInfo enteredMethod) {
    String meth = enteredMethod.getBaseName();
    if(meth.contains("readFormation")) {
      started = true;
    }
  }

  @Override
  public void stateBacktracked(Search search) {
    if(started)
    this.cost = this.costMap.get(search.getVM().getChoiceGenerator());
  }

  @Override
  public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
    if(started)
    this.costMap.put(currentCG, this.cost);
  }
}