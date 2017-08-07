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

package edu.cmu.sv.isstac.canopy.batch;

import edu.cmu.sv.isstac.canopy.AnalysisStrategy;
import edu.cmu.sv.isstac.canopy.JPFFactory;
import edu.cmu.sv.isstac.canopy.JPFSamplerFactory;
import edu.cmu.sv.isstac.canopy.Options;
import edu.cmu.sv.isstac.canopy.exploration.AllChoicesStrategy;
import edu.cmu.sv.isstac.canopy.montecarlo.MonteCarloStrategy;
import edu.cmu.sv.isstac.canopy.montecarlo.Utils;
import edu.cmu.sv.isstac.canopy.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class MonteCarloExperiment implements Experiment {

  private final boolean pruning;

  public MonteCarloExperiment(boolean pruning) {
    this.pruning = pruning;
  }

  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed) throws BatchProcessorException {
    if(!pruning) {
      config.setProperty(Options.CHOICES_STRATEGY, AllChoicesStrategy.class.getName());
    }

    config.setProperty(Options.RNG_SEED, Integer.toString(seed));

    //Never use model counting with monte carlo
    config.setProperty(Options.USE_MODELCOUNT_AMPLIFICATION, Boolean.toString(false));

    try {
      return new MonteCarloStrategy(Utils.createSimulationPolicy(config));
    } catch (ModelCounterCreationException e) {
      throw new BatchProcessorException(e);
    }
  }

  @Override
  public JPFFactory getJPFFactory() {
    return new JPFSamplerFactory();
  }

  @Override
  public String getName() {
    return "MonteCarlo[pruning=" + this.pruning + "]";
  }
}
