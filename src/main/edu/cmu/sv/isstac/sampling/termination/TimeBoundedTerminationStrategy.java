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

package edu.cmu.sv.isstac.sampling.termination;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 *
 */
public class TimeBoundedTerminationStrategy implements TerminationStrategy {

  private final Stopwatch stopwatch;
  private final TimeUnit timeUnit;
  private final long timeBound;
  
  public TimeBoundedTerminationStrategy(long timeBound, TimeUnit unit) {
    this.timeUnit = unit;
    this.timeBound = timeBound;
    this.stopwatch = Stopwatch.createStarted();
  }

  @Override
  public boolean terminate(VM vm, SamplingResult currentResult) {
    // Ugly that we rely on deprecated api because of messy guava dependencies
    return stopwatch.elapsed(timeUnit) >= timeBound;
  }
}
