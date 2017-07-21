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

package edu.cmu.sv.isstac.sampling.batch;

import edu.cmu.sv.isstac.sampling.AnalysisStrategy;
import edu.cmu.sv.isstac.sampling.JPFFactory;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class BacktrackingDecorator implements Experiment {

  private final Experiment wrappee;
  private final boolean optimizeChoices;
  private final boolean useBacktracking;

  public BacktrackingDecorator(Experiment wrappee, boolean optimizeChoices, boolean
      useBacktracking) {
    this.wrappee = wrappee;
    this.optimizeChoices = optimizeChoices;
    this.useBacktracking = useBacktracking;
  }
  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed)
      throws BatchProcessorException {
    config.setProperty("symbolic.optimizechoices", Boolean.toString(optimizeChoices));
    config.setProperty("canopy.backtrackingsearch",
        Boolean.toString(useBacktracking));

    return this.wrappee.createAnalysisStrategy(config, seed);
  }

  @Override
  public JPFFactory getJPFFactory() {
    return this.wrappee.getJPFFactory();
  }

  @Override
  public String getName() {
    String wrappeeName = this.wrappee.getName();
    String newName = wrappeeName;
    if(wrappeeName.endsWith("]")) {
      newName = wrappeeName.substring(0, wrappeeName.lastIndexOf("]"));
    }
    newName += ";optchoices=" + this.optimizeChoices + ";backtrack=" + this.useBacktracking + "]";
    return newName;
  }
}
