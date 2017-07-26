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

import java.text.DecimalFormat;

import edu.cmu.sv.isstac.canopy.AnalysisStrategy;
import edu.cmu.sv.isstac.canopy.JPFFactory;
import edu.cmu.sv.isstac.canopy.JPFSamplerFactory;
import edu.cmu.sv.isstac.canopy.Options;
import edu.cmu.sv.isstac.canopy.exploration.AllChoicesStrategy;
import edu.cmu.sv.isstac.canopy.mcts.MCTSStrategy;
import edu.cmu.sv.isstac.canopy.mcts.SelectionPolicy;
import edu.cmu.sv.isstac.canopy.mcts.Utils;
import edu.cmu.sv.isstac.canopy.policies.SimulationPolicy;
import edu.cmu.sv.isstac.canopy.quantification.ModelCounterCreationException;
import gov.nasa.jpf.Config;

/**
 * @author Kasper Luckow
 */
public class MCTSExperiment implements Experiment {
  private static final int MAX_SAMPLES_NO_PRUNING = 2000;

  //TODO: pruning is currently always enabled. Make support for the non-pruning case
  private final boolean pruning;
  private final boolean rewardAmplifcation;
  private final boolean weightedSimulation;
  private final double biasparameter;

  public MCTSExperiment(boolean pruning, boolean rewardAmplification, boolean weightedSimulation,
   double biasparameter) {
    this.pruning = pruning;
    this.rewardAmplifcation = rewardAmplification;
    this.weightedSimulation = weightedSimulation;
    this.biasparameter = biasparameter;
  }

  @Override
  public AnalysisStrategy createAnalysisStrategy(Config config, int seed) {
    if(!pruning) {
      config.setProperty(Options.CHOICES_STRATEGY, AllChoicesStrategy.class.getName());
    }

    config.setProperty(Options.USE_MODELCOUNT_AMPLIFICATION,
        Boolean.toString(this.rewardAmplifcation));
    config.setProperty(Utils.USE_MODELCOUNT_WEIGHTED_SIMULATION,
        Boolean.toString(this.weightedSimulation));

    config.setProperty(Utils.UCT_BIAS, Double.toString(biasparameter));

    config.setProperty(Options.RNG_SEED, Integer.toString(seed));

    try {
      SimulationPolicy simPol = Utils.createSimulationPolicy(config);
      SelectionPolicy selPol = Utils.createSelectionPolicy(config);

      return new MCTSStrategy(selPol, simPol);
    } catch(ModelCounterCreationException e) {
      throw new BatchProcessorException(e);
    }
  }

  @Override
  public JPFFactory getJPFFactory() {
    return new JPFSamplerFactory();
  }

  @Override
  public String getName() {

    return "MCTS[pruning=" + this.pruning + ";rewardAmp=" + this.rewardAmplifcation + ";" +
        "weightSim=" + this.weightedSimulation + ";bias=" + new DecimalFormat("#.##").format(this
        .biasparameter) + "]";
  }
}
